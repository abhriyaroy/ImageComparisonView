package com.zebrostudio.imagecomparisonview

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View.MeasureSpec.*
import android.view.ViewTreeObserver
import android.widget.ImageView


private const val SPLIT_VERTICALLY = 0
private const val SPLIT_AT_MIDDLE_DEFAULT_ENUM_VALUE = 2
private const val SPLIT_AT_MIDDLE = 0.5F
private const val SPLIT_AT_ONE_THIRD = 0.25F
private const val SPLIT_AT_TWO_THIRD = 0.75F

class ImageComparisonView : ImageView {

    private lateinit var paint: Paint
    private lateinit var dividerPaint: Paint
    private lateinit var preRect: Rect
    private lateinit var postRect: Rect
    private var splitOrientation = SPLIT_VERTICALLY
    private var splitAt = SPLIT_AT_MIDDLE
    private var desiredWidth: Int? = null
    private var desiredHeight: Int? = null
    private var initialBitmap: Bitmap? = null
    private var resultBitmap: Bitmap? = null
    private var dividerVisibility = false

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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (desiredHeight != null && desiredWidth != null) {
            setMeasuredDimension(
                measureDimension(desiredWidth!!, widthMeasureSpec),
                measureDimension(desiredHeight!!, heightMeasureSpec)
            )
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (initialBitmap != null && resultBitmap != null) {
            canvas.drawBitmap(initialBitmap, preRect, preRect, paint)
            canvas.drawBitmap(resultBitmap, postRect, postRect, paint)
        }
    }

    fun setImages(before: Drawable, after: Drawable) {
        setImages((before as BitmapDrawable).bitmap, (after as BitmapDrawable).bitmap)
    }

    fun setImages(before: Drawable, after: Int) {
        setImages((before as BitmapDrawable).bitmap, (BitmapFactory.decodeResource(context.resources, after)))
    }

    fun setImages(before: Drawable, after: Bitmap) {
        setImages((before as BitmapDrawable).bitmap, after)
    }

    fun setImages(before: Int, after: Int) {
        setImages(
            BitmapFactory.decodeResource(context.resources, before),
            BitmapFactory.decodeResource(context.resources, after)
        )
    }

    fun setImages(before: Int, after: Drawable) {
        setImages(BitmapFactory.decodeResource(context.resources, before), (after as BitmapDrawable).bitmap)
    }

    fun setImages(before: Int, after: Bitmap) {
        setImages(BitmapFactory.decodeResource(context.resources, before), after)
    }

    fun setImages(before: Bitmap, after: Drawable) {
        setImages(before, (after as BitmapDrawable).bitmap)
    }

    fun setImages(before: Bitmap, after: Int) {
        setImages(before, BitmapFactory.decodeResource(context.resources, after))
    }

    fun setImages(before: Bitmap, after: Bitmap) {
        super.setImageBitmap(after)
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                initialBitmap = scaleBitmap(before)
                resultBitmap = scaleBitmap(after)
            }
        })
    }

    fun setResultDimensions(desiredWidth: Int, desiredHeight: Int) {
        this.desiredWidth = desiredWidth + paddingLeft + paddingRight
        this.desiredHeight = desiredHeight + paddingTop + paddingBottom
        invalidate()
    }

    fun showDivider(flag: Boolean) {
        dividerVisibility = flag
    }

    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        var result: Int
        val specMode = getMode(measureSpec)
        val specSize = getSize(measureSpec)

        if (specMode == EXACTLY) {
            result = specSize
        } else {
            result = desiredSize
            if (specMode == AT_MOST) {
                result = Math.min(result, specSize)
            }
        }

        if (result < desiredSize) {
            Log.e("ChartView", "The view is too small, the content might get cut")
        }
        return result
    }

    private fun scaleBitmap(bitmap: Bitmap): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }
}