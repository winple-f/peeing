package com.prostaterehab.app.ui.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var labels: List<String> = emptyList()
    private var values: List<Float> = emptyList()
    private var maxValue = 0f

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2196F3")
        strokeWidth = 6f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(50, 33, 150, 243)
        style = Paint.Style.FILL
    }
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2196F3")
        style = Paint.Style.FILL
    }
    private val pointStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 24f
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#757575")
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 22f
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#212121")
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E0E0")
        strokeWidth = 2f
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F0F0F0")
        strokeWidth = 1f
    }

    fun setData(labels: List<String>, values: List<Float>) {
        this.labels = labels
        this.values = values
        this.maxValue = if (values.isNotEmpty()) (values.max() + 1) else 5f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (values.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()
        val padLeft = 48f
        val padRight = 24f
        val padTop = 40f
        val padBottom = 48f
        val chartW = w - padLeft - padRight
        val chartH = h - padTop - padBottom

        val steps = 4
        for (i in 0..steps) {
            val y = padTop + chartH * i / steps
            canvas.drawLine(padLeft, y, w - padRight, y, gridPaint)
            val axisValue = (maxValue * (steps - i) / steps).toInt()
            canvas.drawText(axisValue.toString(), padLeft - 8f, y + 8f, labelPaint.apply { textAlign = Paint.Align.RIGHT })
        }

        val stepX = if (labels.size > 1) chartW / (labels.size - 1) else chartW

        val path = Path()
        val fillPath = Path()
        for (i in values.indices) {
            val x = padLeft + i * stepX
            val y = padTop + chartH * (1 - values[i] / maxValue)
            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, padTop + chartH)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        if (values.isNotEmpty()) {
            fillPath.lineTo(padLeft + (values.size - 1) * stepX, padTop + chartH)
            fillPath.close()
            canvas.drawPath(fillPath, fillPaint)
        }
        canvas.drawPath(path, linePaint)

        for (i in values.indices) {
            val x = padLeft + i * stepX
            val y = padTop + chartH * (1 - values[i] / maxValue)
            canvas.drawCircle(x, y, 8f, pointPaint)
            canvas.drawCircle(x, y, 8f, pointStrokePaint)

            canvas.drawText(values[i].toInt().toString(), x, y - 16f, valuePaint)

            if (i < labels.size) {
                val labelY = padTop + chartH + 32f
                if (i == 0 || i == labels.size - 1 || i == labels.size / 2) {
                    canvas.drawText(labels[i], x, labelY, labelPaint.apply { textAlign = Paint.Align.CENTER })
                }
            }
        }

        labelPaint.textAlign = Paint.Align.CENTER
    }
}
