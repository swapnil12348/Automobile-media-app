import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.math.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun AudiInspiredGauge(
    modifier: Modifier = Modifier,
    value: Float = 0f,
    onValueChange: (Float) -> Unit,
    min: Float = 0f,
    max: Float = 140f,
    unit: String = "km/h",
    centerText: String = "P",
    gaugeColor: Color = Color(0xFFD50000), // Audi Red
    backgroundColor: Color = Color(0xFF1A1A1A), // Dark Gray
    textColor: Color = Color.White
) {
    val textMeasurer = rememberTextMeasurer()

    val animatedValue by animateFloatAsState(
        targetValue = value.coerceIn(min, max),
        animationSpec = tween(500)
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val touchAngle = (Math.toDegrees(
                        atan2(
                            (offset.y - center.y).toDouble(),
                            (offset.x - center.x).toDouble()
                        )
                    ).toFloat() + 360) % 360

                    if (touchAngle in 170f..350f) {
                        val newValue = min + (touchAngle - 170) / 200 * (max - min)
                        onValueChange(newValue)
                    }
                }
            }
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = minOf(size.width, size.height) / 2f * 0.8f

        drawGaugeBackground(center, radius, backgroundColor)
        drawGaugeArc(center, radius, animatedValue, min, max, gaugeColor)
        drawTicks(center, radius, textColor)
        drawCenterText(center, radius, centerText, gaugeColor, textMeasurer)
        drawRange(center, radius, min, max, textColor, textMeasurer)
        drawNeedle(center, radius, animatedValue, min, max, gaugeColor)
        drawValue(center, radius, animatedValue, unit, textColor, textMeasurer)
    }
}

private fun DrawScope.drawGaugeBackground(
    center: Offset,
    radius: Float,
    backgroundColor: Color
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF2A2A2A), backgroundColor),
            center = center,
            radius = radius
        ),
        center = center,
        radius = radius
    )
}

private fun DrawScope.drawGaugeArc(
    center: Offset,
    radius: Float,
    value: Float,
    min: Float,
    max: Float,
    gaugeColor: Color
) {
    val sweepAngle = 200 * (value - min) / (max - min)
    drawArc(
        color = gaugeColor,
        startAngle = 170f,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = radius * 0.05f)
    )
}

private fun DrawScope.drawTicks(
    center: Offset,
    radius: Float,
    textColor: Color
) {
    for (i in 0..28) {
        val angle = 170f + 10f * i
        val startRadius = if (i % 2 == 0) radius * 0.8f else radius * 0.85f
        val endRadius = radius * 0.9f
        val strokeWidth = if (i % 2 == 0) 3f else 1.5f

        val startX = center.x + (startRadius * cos(Math.toRadians(angle.toDouble()))).toFloat()
        val startY = center.y + (startRadius * sin(Math.toRadians(angle.toDouble()))).toFloat()
        val endX = center.x + (endRadius * cos(Math.toRadians(angle.toDouble()))).toFloat()
        val endY = center.y + (endRadius * sin(Math.toRadians(angle.toDouble()))).toFloat()

        drawLine(
            color = textColor,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = strokeWidth
        )
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawCenterText(
    center: Offset,
    radius: Float,
    centerText: String,
    gaugeColor: Color,
    textMeasurer: TextMeasurer
) {
    val textStyle = TextStyle(
        color = gaugeColor,
        fontSize = (radius * 0.3f).sp,
        fontWeight = FontWeight.Bold
    )

    val textLayoutResult = textMeasurer.measure(centerText, textStyle)
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(
            center.x - textLayoutResult.size.width / 2f,
            center.y - textLayoutResult.size.height / 2f
        )
    )
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawRange(
    center: Offset,
    radius: Float,
    min: Float,
    max: Float,
    textColor: Color,
    textMeasurer: TextMeasurer
) {
    val textStyle = TextStyle(
        color = textColor,
        fontSize = (radius * 0.12f).sp
    )

    val minText = textMeasurer.measure(min.toInt().toString(), textStyle)
    val maxText = textMeasurer.measure(max.toInt().toString(), textStyle)

    drawText(
        textLayoutResult = minText,
        topLeft = Offset(
            center.x - radius * 0.8f - minText.size.width / 2f,
            center.y + radius * 0.9f - minText.size.height / 2f
        )
    )

    drawText(
        textLayoutResult = maxText,
        topLeft = Offset(
            center.x + radius * 0.8f - maxText.size.width / 2f,
            center.y + radius * 0.9f - maxText.size.height / 2f
        )
    )
}

private fun DrawScope.drawNeedle(
    center: Offset,
    radius: Float,
    value: Float,
    min: Float,
    max: Float,
    gaugeColor: Color
) {
    val angle = 170f + 200f * (value - min) / (max - min)
    val needleLength = radius * 0.7f
    val needleWidth = radius * 0.04f

    val path = Path().apply {
        moveTo(center.x, center.y)
        lineTo(
            center.x + needleLength * cos(Math.toRadians(angle.toDouble())).toFloat(),
            center.y + needleLength * sin(Math.toRadians(angle.toDouble())).toFloat()
        )
        lineTo(
            center.x + needleWidth * cos(Math.toRadians((angle + 90).toDouble())).toFloat(),
            center.y + needleWidth * sin(Math.toRadians((angle + 90).toDouble())).toFloat()
        )
        lineTo(
            center.x + needleWidth * cos(Math.toRadians((angle - 90).toDouble())).toFloat(),
            center.y + needleWidth * sin(Math.toRadians((angle - 90).toDouble())).toFloat()
        )
        close()
    }

    drawPath(
        path = path,
        color = gaugeColor,
        style = androidx.compose.ui.graphics.drawscope.Fill
    )
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawValue(
    center: Offset,
    radius: Float,
    value: Float,
    unit: String,
    textColor: Color,
    textMeasurer: TextMeasurer
) {
    val textStyle = TextStyle(
        color = textColor,
        fontSize = (radius * 0.2f).sp
    )

    val text = "${value.toInt()} $unit"
    val textLayoutResult = textMeasurer.measure(text, textStyle)

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(
            center.x - textLayoutResult.size.width / 2f,
            center.y + radius * 0.6f - textLayoutResult.size.height / 2f
        )
    )
}