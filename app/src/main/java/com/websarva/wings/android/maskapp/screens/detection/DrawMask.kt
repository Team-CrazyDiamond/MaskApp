package com.websarva.wings.android.maskapp.screens.detection

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import android.util.Size
import android.view.View
import androidx.camera.core.ImageProxy
import androidx.fragment.R
import androidx.fragment.app.findFragment
import kotlinx.android.synthetic.main.fragment_detection.view.*
import kotlinx.android.synthetic.main.fragment_start.view.*
import kotlin.math.abs
import kotlin.math.absoluteValue


@SuppressLint("ViewConstructor")
class DrawMask(
    context: Context,
    private val rect: Rect,
    color: Int,
    val previewSize: Size,
    val analysisImage: ImageProxy,
    val text: String
) : View(context) {

    private val paint: Paint = Paint()

    init {
        paint.color = color
        paint.strokeWidth = 5f
        paint.style = Paint.Style.STROKE
        paint.textSize = 50f
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var scalePlusX: Float = if (rect.left < (analysisImage.width - rect.width()) / 2f) {
            val ratio = ((analysisImage.width - rect.width()) / 2f - rect.left) / (analysisImage.width / 2f)
            120f * ratio
        } else {
            val ratio = ((analysisImage.width - rect.width()) / 2f - rect.left) / (analysisImage.width / 2f)
            600 * ratio
        }

        val _scaleY = previewSize.height.toFloat() / analysisImage.height.toFloat() + 0.7f
        val _scaleX = previewSize.width.toFloat() / analysisImage.width.toFloat()

        val left = rect.left.toFloat() * _scaleX - scalePlusX
        val top = rect.top.toFloat() * _scaleY
        val right = rect.right.toFloat() * _scaleX - scalePlusX
        val bottom = rect.bottom.toFloat() * _scaleY

        canvas.drawRect(left, top, right, bottom, paint)
        canvas.drawText(text, left, top - 20, paint)
    }

}