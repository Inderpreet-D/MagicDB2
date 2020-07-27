package com.dragynslayr.magicdb2.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

class Overlay : SurfaceView, SurfaceHolder.Callback {

    private lateinit var borderPaint: Paint
    private lateinit var textPaint: Paint
    private lateinit var border: Rect
    private lateinit var scanned: String

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        holder.setFormat(PixelFormat.TRANSLUCENT)
        holder.addCallback(this)
        initDrawing()
    }

    private fun initDrawing() {
        borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 10f

        val dimensions = resources.displayMetrics
        val width = dimensions.widthPixels
        val height = dimensions.heightPixels
        val left = (width * 0.05).toInt()
        val right = width - left
        val top = (height * 0.0375).toInt()
        val bottom = height - top
        border = Rect(left, top, right, bottom)

        textPaint = Paint(Paint.LINEAR_TEXT_FLAG)
        textPaint.style = Paint.Style.FILL_AND_STROKE
        textPaint.strokeWidth = 2f
        textPaint.color = Color.WHITE
        textPaint.textSize = 45f
    }

    private fun setText(newText: String = "") {
        scanned = newText
        invalidate()
    }

    fun update(scanned: String) {
        setText(scanned)
    }

    fun reset() {
        setText()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        draw()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder?) {}

    override fun surfaceCreated(holder: SurfaceHolder?) {
        setWillNotDraw(false)
        draw()
    }

    private fun draw() {
        val canvas = holder.lockCanvas()
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        borderPaint.color = if (scanned != "") {
            Color.GREEN
        } else {
            Color.RED
        }
        canvas.drawRect(border, borderPaint)

        val xPos = (resources.displayMetrics.widthPixels - textPaint.measureText(scanned)) / 2f
        canvas.drawText(scanned, xPos, 150f, textPaint)

        holder.unlockCanvasAndPost(canvas)
    }
}