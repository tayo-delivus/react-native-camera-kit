package com.rncamerakit.barcode

import android.content.Context
import android.graphics.*
import android.view.View
import androidx.annotation.ColorInt

import com.rncamerakit.R
import kotlin.math.max
import kotlin.math.min

class BarcodeFrame(context: Context) : View(context) {
    private var borderPaint: Paint = Paint()
    private var laserPaint: Paint = Paint()
    var frameRect: Rect = Rect()

    private var frameWidth = 0
    private var frameHeight = 0
    private var borderMargin = 0
    private var previousFrameTime = System.currentTimeMillis()
    private var laserY = 0

    private fun init(context: Context) {
        borderPaint = Paint()
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = STROKE_WIDTH.toFloat()
        laserPaint.style = Paint.Style.STROKE
        laserPaint.strokeWidth = STROKE_WIDTH.toFloat()
        borderMargin = context.resources.getDimensionPixelSize(R.dimen.border_length)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 여백을 줄 값 (픽셀 단위, 필요에 따라 조정)
                val margin = 40

                // 사용할 수 있는 높이 (여백 제외)
                val availableHeight = measuredHeight - margin * 2
                // 2:1 비율이면, 원하는 너비는 availableHeight의 2배
                val desiredFrameWidth = availableHeight * 2

                // 사용할 수 있는 너비 (여백 제외)
                val availableWidth = measuredWidth - margin * 2

                // 만약 원하는 너비가 사용할 수 있는 너비보다 크면, 너비에 맞춰서 높이를 재계산
                val (frameWidth, frameHeight) = if (desiredFrameWidth > availableWidth) {
                    val adjustedWidth = availableWidth
                    val adjustedHeight = adjustedWidth / 2  // 2:1 비율 유지
                    Pair(adjustedWidth, adjustedHeight)
                } else {
                    Pair(desiredFrameWidth, availableHeight)
                }

                // 중앙 정렬: 전체 영역(measuredWidth, measuredHeight)에서 프레임을 가운데 배치
                frameRect.left = (measuredWidth - frameWidth) / 2
                frameRect.top = (measuredHeight - frameHeight) / 2
                frameRect.right = frameRect.left + frameWidth
                frameRect.bottom = frameRect.top + frameHeight

                // 프레임 크기를 내부 변수에 저장 (필요 시)
                this.frameWidth = frameWidth
                this.frameHeight = frameHeight
    }

    override fun onDraw(canvas: Canvas) {
        val timeElapsed = System.currentTimeMillis() - previousFrameTime
        super.onDraw(canvas)
        drawBorder(canvas)
        drawLaser(canvas, timeElapsed)
        previousFrameTime = System.currentTimeMillis()
        this.invalidate(frameRect)
    }

    private fun drawBorder(canvas: Canvas) {
        canvas.drawLine(frameRect.left.toFloat(), frameRect.top.toFloat(), frameRect.left.toFloat(), (frameRect.top + borderMargin).toFloat(), borderPaint)
        canvas.drawLine(frameRect.left.toFloat(), frameRect.top.toFloat(), (frameRect.left + borderMargin).toFloat(), frameRect.top.toFloat(), borderPaint)
        canvas.drawLine(frameRect.left.toFloat(), frameRect.bottom.toFloat(), frameRect.left.toFloat(), (frameRect.bottom - borderMargin).toFloat(), borderPaint)
        canvas.drawLine(frameRect.left.toFloat(), frameRect.bottom.toFloat(), (frameRect.left + borderMargin).toFloat(), frameRect.bottom.toFloat(), borderPaint)
        canvas.drawLine(frameRect.right.toFloat(), frameRect.top.toFloat(), (frameRect.right - borderMargin).toFloat(), frameRect.top.toFloat(), borderPaint)
        canvas.drawLine(frameRect.right.toFloat(), frameRect.top.toFloat(), frameRect.right.toFloat(), (frameRect.top + borderMargin).toFloat(), borderPaint)
        canvas.drawLine(frameRect.right.toFloat(), frameRect.bottom.toFloat(), frameRect.right.toFloat(), (frameRect.bottom - borderMargin).toFloat(), borderPaint)
        canvas.drawLine(frameRect.right.toFloat(), frameRect.bottom.toFloat(), (frameRect.right - borderMargin).toFloat(), frameRect.bottom.toFloat(), borderPaint)
    }

    private fun drawLaser(canvas: Canvas, timeElapsed: Long) {
        if (laserY > frameRect.bottom || laserY < frameRect.top) laserY = frameRect.top
        canvas.drawLine((frameRect.left + STROKE_WIDTH).toFloat(), laserY.toFloat(), (frameRect.right - STROKE_WIDTH).toFloat(), laserY.toFloat(), laserPaint)
        laserY += (timeElapsed / ANIMATION_SPEED).toInt()
    }

    fun setFrameColor(@ColorInt borderColor: Int) {
        borderPaint.color = borderColor
    }

    fun setLaserColor(@ColorInt laserColor: Int) {
        laserPaint.color = laserColor
    }

    companion object {
        private const val STROKE_WIDTH = 5
        private const val ANIMATION_SPEED = 4
    }

    init {
        init(context)
    }
}
