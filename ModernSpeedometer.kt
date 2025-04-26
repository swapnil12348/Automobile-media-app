package com.example.automobilemediaapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ModernSpeedometer(
    currentSpeed: Float,
    modifier: Modifier = Modifier,
    minSpeed: Float = 0f,
    maxSpeed: Float = 260f,
    unit: String = "km/h",
    speedometerColor: Color = Color.Cyan,
    warningThreshold: Float = 200f
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = min(size.width, size.height) / 2f * 0.8f

        drawSpeedometerBackground(centerX, centerY, radius)
        drawSpeedometerArc(centerX, centerY, radius, currentSpeed, minSpeed, maxSpeed, speedometerColor)
        drawTicks(centerX, centerY, radius, minSpeed, maxSpeed)
        drawSpeed(centerX, centerY, radius, currentSpeed, warningThreshold)
        drawUnit(centerX, centerY, radius, unit)
    }
}

private fun DrawScope.drawSpeedometerBackground(centerX: Float, centerY: Float, radius: Float) {
    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(Color.Black, Color.DarkGray),
            start = Offset(centerX - radius, centerY - radius),
            end = Offset(centerX + radius, centerY + radius)
        ),
        radius = radius,
        center = Offset(centerX, centerY)
    )
}

private fun DrawScope.drawSpeedometerArc(
    centerX: Float,
    centerY: Float,
    radius: Float,
    currentSpeed: Float,
    minSpeed: Float,
    maxSpeed: Float,
    speedometerColor: Color
) {
    val sweepAngle = 270 * (currentSpeed - minSpeed) / (maxSpeed - minSpeed)
    drawArc(
        color = speedometerColor,
        startAngle = 135f,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset(centerX - radius, centerY - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = radius * 0.12f)
    )
}

private fun DrawScope.drawTicks(
    centerX: Float,
    centerY: Float,
    radius: Float,
    minSpeed: Float,
    maxSpeed: Float
) {
    for (i in 0..26 step 2) {
        val angle = 135f + 10.38f * i
        val radians = Math.toRadians(angle.toDouble()).toFloat()
        val startX = centerX + (radius * 0.75f * cos(radians))
        val startY = centerY + (radius * 0.75f * sin(radians))
        val endX = centerX + (radius * 0.85f * cos(radians))
        val endY = centerY + (radius * 0.85f * sin(radians))

        drawLine(
            color = Color.White,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 2f
        )

        if (i % 4 == 0) {
            val textX = centerX + (radius * 0.7f * cos(radians))
            val textY = centerY + (radius * 0.7f * sin(radians))
            val speedText = ((minSpeed + i * (maxSpeed - minSpeed) / 26).toInt()).toString()
            
            drawContext.canvas.nativeCanvas.drawText(
                speedText,
                textX,
                textY,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = radius * 0.1f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

private fun DrawScope.drawSpeed(
    centerX: Float,
    centerY: Float,
    radius: Float,
    currentSpeed: Float,
    warningThreshold: Float
) {
    drawContext.canvas.nativeCanvas.drawText(
        String.format("%.0f", currentSpeed),
        centerX,
        centerY,
        android.graphics.Paint().apply {
            color = if (currentSpeed >= warningThreshold) 
                android.graphics.Color.RED else 
                android.graphics.Color.CYAN
            textSize = radius * 0.4f
            textAlign = android.graphics.Paint.Align.CENTER
        }
    )
}

private fun DrawScope.drawUnit(
    centerX: Float,
    centerY: Float,
    radius: Float,
    unit: String
) {
    drawContext.canvas.nativeCanvas.drawText(
        unit,
        centerX,
        centerY + radius * 0.25f,  // Position the unit text below the speed
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = radius * 0.15f  // Smaller text size than the speed
            textAlign = android.graphics.Paint.Align.CENTER
        }
    )
}