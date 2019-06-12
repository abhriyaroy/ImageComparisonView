package com.zebrostudio.imagecomparisonview

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ViewTreeObserver
import android.widget.ImageView


private const val SPLIT_VERTICALLY = 0
private const val SPLIT_AT_MIDDLE_DEFAULT_ENUM_VALUE = 2
private const val SPLIT_AT_MIDDLE = 0.5F
private const val SPLIT_AT_ONE_THIRD = 0.25F
private const val SPLIT_AT_TWO_THIRD = 0.75F

class ImageComparisonView : ImageView {

    private lateinit var paint: Paint
    private lateinit var preRect: Rect
    private lateinit var postRect: Rect
    private var splitOrientation = SPLIT_VERTICALLY
    private var splitAt = SPLIT_AT_MIDDLE
    private var desiredWidth: Int? = null
    private var desiredHeight: Int? = null
    private var bitmapBeforeProcessing: Bitmap? = null
    private var bitmapAfterProcessing: Bitmap? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        init(context, attributeSet)
    }

    private fun init(context: Context, attributeSet: AttributeSet?) {
        attributeSet?.let {
            val typedArray =
                context.obtainStyledAttributes(it, R.styleable.ImageComparisonView)
            splitOrientation = typedArray.getInt(R.styleable.ImageComparisonView_split_alignment, SPLIT_VERTICALLY)
            splitAt =
                when (typedArray.getInt(R.styleable.ImageComparisonView_split_at, SPLIT_AT_MIDDLE_DEFAULT_ENUM_VALUE)) {
                    0 -> SPLIT_AT_ONE_THIRD
                    1 -> SPLIT_AT_TWO_THIRD
                    else -> SPLIT_AT_MIDDLE
                }
            typedArray.recycle()
        }

        scaleType = ScaleType.FIT_XY
        paint = Paint(Paint.ANTI_ALIAS_FLAG)

        this.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (splitOrientation == SPLIT_VERTICALLY) {
                    preRect = Rect(0, 0, (width * splitAt).toInt(), height)
                    postRect = Rect((width * splitAt).toInt(), 0, width, height)
                } else {
                    preRect = Rect(0, 0, width, (height * splitAt).toInt())
                    postRect = Rect(0, (height * splitAt).toInt(), width, height)
                }
            }
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bitmapBeforeProcessing != null && bitmapAfterProcessing != null) {
            canvas.drawBitmap(bitmapBeforeProcessing, preRect, preRect, paint)
            canvas.drawBitmap(bitmapAfterProcessing, postRect, postRect, paint)
        }
    }

    fun setImageDrawables(before: Drawable, after: Drawable) {
        super.setImageDrawable(after)
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                ((before as BitmapDrawable).bitmap).let { firstBitmap ->
                    ((after as BitmapDrawable).bitmap).let { secondBitmap ->
                        bitmapBeforeProcessing = scaleBitmap(firstBitmap)
                        bitmapAfterProcessing = scaleBitmap(secondBitmap)
                    }
                }
            }
        })
    }

    fun setImageResources(before: Int, after: Int) {
        super.setImageResource(after)
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                (BitmapFactory.decodeResource(context.resources, before)).let { firstBitmap ->
                    (BitmapFactory.decodeResource(context.resources, after)).let { secondBitmap ->
                        bitmapBeforeProcessing = scaleBitmap(firstBitmap)
                        bitmapAfterProcessing = scaleBitmap(secondBitmap)
                    }
                }
            }

        })
    }

    fun setImageBitmaps(before: Bitmap, after: Bitmap) {
        super.setImageBitmap(after)
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                bitmapBeforeProcessing = scaleBitmap(before)
                bitmapAfterProcessing = scaleBitmap(after)
            }
        })
    }

    fun setResultDimensions(desiredWidth: Int, desiredHeight: Int) {
        this.desiredWidth = desiredWidth
        this.desiredHeight = desiredHeight
    }

    private fun scaleBitmap(bitmap: Bitmap): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, desiredWidth ?: width, desiredHeight ?: height, false)
    }
}