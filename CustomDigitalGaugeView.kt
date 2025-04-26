import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DigitalGauge(
    speed: Float,
    rpm: Float,
    gear: Int,
    fuelLevel: Float,
    modifier: Modifier = Modifier
) {
    var showingSpeed by remember { mutableStateOf(true) }
    var showingRpm by remember { mutableStateOf(true) }

    // Animation states
    val speedAnimated by animateFloatAsState(
        targetValue = speed,
        animationSpec = tween(500, easing = FastOutSlowInEasing)
    )

    val rpmAnimated by animateFloatAsState(
        targetValue = rpm,
        animationSpec = tween(500, easing = FastOutSlowInEasing)
    )

    // Constants
    val maxSpeed = 260f
    val maxRpm = 8000f
    val normalColor = Color.Green
    val warningColor = Color.Yellow
    val criticalColor = Color.Red

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    showingSpeed = !showingSpeed
                    showingRpm = !showingRpm
                }
            }
    ) {
        // Draw background gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Black, Color.DarkGray)
            )
        )

        if (showingSpeed) {
            drawGauge(
                centerX = size.width * 0.25f,
                centerY = size.height * 0.5f,
                value = speedAnimated,
                maxValue = maxSpeed,
                label = "SPEED",
                unit = "km/h",
                normalColor = normalColor,
                warningColor = warningColor,
                criticalColor = criticalColor
            )
        }

        if (showingRpm) {
            drawGauge(
                centerX = size.width * 0.75f,
                centerY = size.height * 0.5f,
                value = rpmAnimated,
                maxValue = maxRpm,
                label = "RPM",
                unit = "x1000",
                normalColor = normalColor,
                warningColor = warningColor,
                criticalColor = criticalColor
            )
        }

        // Draw gear indicator
        drawGearIndicator(gear, size.width * 0.5f, size.height * 0.9f)

        // Draw fuel gauge
        drawFuelGauge(fuelLevel, size.width, size.height)
    }
}

private fun DrawScope.drawGauge(
    centerX: Float,
    centerY: Float,
    value: Float,
    maxValue: Float,
    label: String,
    unit: String,
    normalColor: Color,
    warningColor: Color,
    criticalColor: Color
) {
    val radius = size.height * 0.35f

    // Determine color based on value
    val gaugeColor = when {
        value >= maxValue * 0.9f -> criticalColor
        value >= maxValue * 0.7f -> warningColor
        else -> normalColor
    }

    // Draw gauge arc
    drawArc(
        color = gaugeColor,
        startAngle = 135f,
        sweepAngle = 270f,
        useCenter = false,
        topLeft = Offset(centerX - radius, centerY - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = 20f)
    )

    // Draw tick marks and labels
    for (i in 0..5) {
        val angle = Math.toRadians(135.0 + 54.0 * i)
        val tickValue = (maxValue * i / 5).toInt()
        val x = (centerX + cos(angle) * radius).toFloat()
        val y = (centerY + sin(angle) * radius).toFloat()

        drawLine(
            color = gaugeColor,
            start = Offset(centerX, centerY),
            end = Offset(x, y),
            strokeWidth = 2f
        )

        // Draw tick value
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 30f
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawText(tickValue.toString(), x, y, paint)
        }
    }

    // Draw needle
    val needleAngle = Math.toRadians(135.0 + 270.0 * value / maxValue)
    val needleLength = radius * 0.8f
    drawLine(
        color = gaugeColor,
        start = Offset(centerX, centerY),
        end = Offset(
            (centerX + cos(needleAngle) * needleLength).toFloat(),
            (centerY + sin(needleAngle) * needleLength).toFloat()
        ),
        strokeWidth = 3f
    )

    // Draw value and unit using native canvas for text
    drawContext.canvas.nativeCanvas.apply {
        val valuePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 60f
            textAlign = android.graphics.Paint.Align.CENTER
        }

        val unitPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.LTGRAY
            textSize = 30f
            textAlign = android.graphics.Paint.Align.CENTER
        }

        drawText(String.format("%.0f", value), centerX, centerY + radius * 0.4f, valuePaint)
        drawText(unit, centerX, centerY + radius * 0.6f, unitPaint)
    }
}

private fun DrawScope.drawGearIndicator(gear: Int, x: Float, y: Float) {
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 40f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        drawText("Gear: $gear", x, y, paint)
    }
}

private fun DrawScope.drawFuelGauge(fuelLevel: Float, width: Float, height: Float) {
    val fuelGaugeLeft = width * 0.1f
    val fuelGaugeTop = height * 0.8f
    val fuelGaugeRight = width * 0.9f
    val fuelGaugeBottom = height * 0.85f

    // Draw fuel gauge outline
    drawRect(
        color = Color.White,
        topLeft = Offset(fuelGaugeLeft, fuelGaugeTop),
        size = Size(fuelGaugeRight - fuelGaugeLeft, fuelGaugeBottom - fuelGaugeTop),
        style = Stroke(width = 10f)
    )

    // Draw fuel level
    drawRect(
        color = Color.White,
        topLeft = Offset(fuelGaugeLeft, fuelGaugeTop),
        size = Size(
            (fuelGaugeRight - fuelGaugeLeft) * fuelLevel / 100,
            fuelGaugeBottom - fuelGaugeTop
        )
    )

    // Draw fuel percentage text
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 40f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        drawText("Fuel: ${fuelLevel.toInt()}%", width * 0.5f, fuelGaugeBottom + 30f, paint)
    }
}

// Example usage:
@Composable
fun DigitalGaugePreview() {
    DigitalGauge(
        speed = 120f,
        rpm = 3000f,
        gear = 4,
        fuelLevel = 75f,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
}