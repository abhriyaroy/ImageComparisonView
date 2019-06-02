package com.zebrostudio.imagecomparisonview

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.util.AttributeSet
import android.view.ViewTreeObserver
import android.widget.ImageView


private const val SPLIT_VERTICALLY = 0
private const val MINIMUM_WIDTH = 0

class ImageComparisonView : ImageView {

    private lateinit var prePaint: Paint
    private lateinit var postPaint: Paint
    private lateinit var preRect: Rect
    private lateinit var postRect: Rect
    private var splitOrientation = SPLIT_VERTICALLY
    private var bitmapBeforeProcessing: Bitmap? = null
    private var bitmapAfterProcessing: Bitmap? = null

    constructor(context: Context) : super(context) {
        init(context, null, 0, 0)
    }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        init(context, attributeSet, 0, 0)
    }

    @TargetApi(LOLLIPOP)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleRes: Int) : super(
        context,
        attributeSet,
        defStyleRes
    ) {
        init(context, attributeSet, defStyleRes, 0)
    }

    private fun init(context: Context, attributeSet: AttributeSet?, defStyleRes: Int, defStyleInt: Int) {
        attributeSet?.let {
            val typedArray =
                context.obtainStyledAttributes(it, R.styleable.ImageComparisonView, defStyleInt, defStyleInt)
            splitOrientation = typedArray.getInt(R.styleable.ImageComparisonView_split, SPLIT_VERTICALLY)
            typedArray.recycle()
        }

        prePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        prePaint.style = Paint.Style.FILL
        prePaint.color = resources.getColor(R.color.test)
        postPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        postPaint.color = resources.getColor(R.color.black)

        this.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (splitOrientation == SPLIT_VERTICALLY) {
                    val resolvedWidth = resolveWidth(width)
                    preRect = Rect(0, 0, resolvedWidth / 2, height)
                    postRect = Rect(resolvedWidth / 2, 0, resolvedWidth, height)
                } else {
                    val resolvedHeight = resolveHeight(height)
                    preRect = Rect(0, 0, width, resolvedHeight / 2)
                    postRect = Rect(0, height / 2, width, resolvedHeight)
                }
            }
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmapBeforeProcessing!!, preRect, preRect, prePaint)
        canvas.drawBitmap(bitmapAfterProcessing!!, postRect, postRect, postPaint)
    }

    fun setImageDrawables(before: Drawable, after: Drawable) {
        bitmapBeforeProcessing = (before as BitmapDrawable).bitmap
        bitmapAfterProcessing = (after as BitmapDrawable).bitmap
        super.setImageDrawable(after)
    }

    fun setImageResources(before: Int, after: Int) {
        bitmapBeforeProcessing = BitmapFactory.decodeResource(context.resources, before)
        bitmapAfterProcessing = BitmapFactory.decodeResource(context.resources, after)
        super.setImageResource(after)
    }

    fun setImageBitmaps(before: Bitmap, after: Bitmap) {
        bitmapBeforeProcessing = before
        bitmapAfterProcessing = after
        super.setImageBitmap(after)
    }

    private fun resolveWidth(width: Int): Int {
        return getMinimumViewWidth().let {
            if (width > it) {
                width
            } else {
                it
            }
        }
    }

    private fun resolveHeight(height: Int): Int {
        return getMinimumViewHeight().let {
            if (height > it) {
                height
            } else {
                it
            }
        }
    }

    private fun getMinimumViewWidth(): Int {
        return (10 * Resources.getSystem().displayMetrics.density).toInt()
    }

    private fun getMinimumViewHeight(): Int {
        return (10 * Resources.getSystem().displayMetrics.density).toInt()
    }
}