package com.prostaterehab.app.ui.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import java.util.Calendar

class HeatMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class DayData(
        val day: Int,
        val score: Int,
        val inMonth: Boolean
    )

    private var dayDataList: List<DayData> = emptyList()
    private var maxScore = 0

    private val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }
    private val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 26f
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#757575")
    }
    private val rect = RectF()
    private val weekdays = arrayOf("日", "一", "二", "三", "四", "五", "六")

    fun setData(data: List<DayData>, max: Int) {
        dayDataList = data
        maxScore = max
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val padding = 16f
        val headerHeight = 36f
        val availableW = w - padding * 2
        val availableH = h - headerHeight - padding * 2
        val cellSize = Math.min(availableW / 7f, availableH / 6f)
        val startX = (w - cellSize * 7) / 2
        val startY = padding + headerHeight

        for (i in weekdays.indices) {
            val cx = startX + cellSize * i + cellSize / 2
            canvas.drawText(weekdays[i], cx, padding + 26f, headerPaint)
        }

        for (index in dayDataList.indices) {
            val row = index / 7
            val col = index % 7
            val left = startX + col * cellSize + 2f
            val top = startY + row * cellSize + 2f
            val right = left + cellSize - 4f
            val bottom = top + cellSize - 4f
            rect.set(left, top, right, bottom)

            val dayData = dayDataList[index]
            if (!dayData.inMonth) {
                continue
            }

            if (dayData.score > 0 && maxScore > 0) {
                val ratio = dayData.score.toFloat() / maxScore.toFloat()
                val alpha = (60 + ratio * 195).toInt().coerceIn(0, 255)
                cellPaint.color = Color.argb(alpha, 255, 193, 7)
                canvas.drawRoundRect(rect, 6f, 6f, cellPaint)
            } else {
                cellPaint.color = Color.parseColor("#F0F0F0")
                canvas.drawRoundRect(rect, 6f, 6f, cellPaint)
            }

            val cx = left + (right - left) / 2
            val cy = top + (bottom - top) / 2 + 10f
            textPaint.color = if (dayData.score > 0 && maxScore > 0) {
                val ratio = dayData.score.toFloat() / maxScore.toFloat()
                if (ratio > 0.5f) Color.WHITE else Color.parseColor("#212121")
            } else {
                Color.parseColor("#BDBDBD")
            }
            canvas.drawText(dayData.day.toString(), cx, cy, textPaint)
        }
    }
}
