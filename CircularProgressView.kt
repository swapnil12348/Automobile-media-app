import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun AudiInspiredDashboard(
    modifier: Modifier = Modifier,
    gear: String = "P",
    speed: Int = 0,
    range: Int = 500,
    temperature: Float = 20.0f,
    time: String = "19:00",
    onSelectionClick: () -> Unit = {},
    onDisplaysClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val textMeasurer = rememberTextMeasurer()

    val animatedSpeed by animateFloatAsState(
        targetValue = speed.toFloat(),
        animationSpec = tween(500)
    )

    val animatedNeedle by animateFloatAsState(
        targetValue = speed.toFloat(),
        animationSpec = tween(500)
    )

    Box(
        modifier = modifier
            .background(Color.Black)
            .aspectRatio(16f/9f)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw top bar
            drawTopBar(textMeasurer, time, temperature)

            // Draw main gauges
            drawGearGauge(textMeasurer, gear)
            drawSpeedGauge(textMeasurer, animatedSpeed, animatedNeedle)

            // Draw range
            drawRange(textMeasurer, range)

            // Draw bottom bar
            drawBottomBar(textMeasurer)
        }
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawTopBar(
    textMeasurer: TextMeasurer,
    time: String,
    temperature: Float
) {
    // Time
    val timeStyle = TextStyle(
        color = Color.White,
        fontSize = (size.height * 0.05f).sp,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light
    )

    drawText(
        textMeasurer = textMeasurer,
        text = time,
        style = timeStyle,
        topLeft = Offset(size.width * 0.05f, size.height * 0.03f)
    )

    // Temperature gauge
    val centerX = size.width * 0.9f
    val centerY = size.height * 0.08f
    val radius = size.height * 0.04f

    drawCircle(
        color = Color.White,
        radius = radius,
        center = Offset(centerX, centerY),
        style = Stroke(width = 3f)
    )

    val angle = (temperature - 10) * 6
    val endX = centerX + radius * cos(Math.toRadians(angle.toDouble())).toFloat()
    val endY = centerY + radius * sin(Math.toRadians(angle.toDouble())).toFloat()

    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY),
        end = Offset(endX, endY),
        strokeWidth = 2f
    )

    val tempText = "${temperature.toInt()}°C"
    val tempStyle = TextStyle(
        color = Color.White,
        fontSize = (size.height * 0.03f).sp,
        textAlign = TextAlign.Center
    )

    val tempMeasured = textMeasurer.measure(tempText, tempStyle)
    drawText(
        textLayoutResult = tempMeasured,
        topLeft = Offset(
            centerX - tempMeasured.size.width / 2,
            centerY + radius + size.height * 0.01f
        )
    )
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawGearGauge(
    textMeasurer: TextMeasurer,
    gear: String
) {
    val centerX = size.width * 0.25f
    val centerY = size.height * 0.5f
    val radius = min(size.width, size.height) * 0.2f

    drawCircle(
        color = Color(0xFFD50000),
        radius = radius,
        center = Offset(centerX, centerY),
        style = Stroke(width = 5f)
    )

    val gearStyle = TextStyle(
        color = Color.White,
        fontSize = (size.height * 0.15f).sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center
    )

    val gearMeasured = textMeasurer.measure(gear, gearStyle)
    drawText(
        textLayoutResult = gearMeasured,
        topLeft = Offset(
            centerX - gearMeasured.size.width / 2,
            centerY - gearMeasured.size.height / 2
        )
    )
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawSpeedGauge(
    textMeasurer: TextMeasurer,
    speed: Float,
    needleAngle: Float
) {
    val centerX = size.width * 0.75f
    val centerY = size.height * 0.5f
    val radius = min(size.width, size.height) * 0.2f

    // Main circle
    drawCircle(
        color = Color(0xFFD50000),
        radius = radius,
        center = Offset(centerX, centerY),
        style = Stroke(width = 5f)
    )

    // Speed arc
    val rect = Rect(
        offset = Offset(centerX - radius, centerY - radius),
        size = Size(radius * 2, radius * 2)
    )
    val sweepAngle = 270 * speed / 260
    drawArc(
        color = Color(0xFFD50000),
        startAngle = 135f,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = rect.topLeft,
        size = rect.size,
        style = Stroke(width = 5f)
    )

    // Needle
    val needleLength = radius * 0.8f
    val needleAngleRad = Math.toRadians((135 + 270 * needleAngle / 260).toDouble())
    val needleEndX = centerX + needleLength * cos(needleAngleRad).toFloat()
    val needleEndY = centerY + needleLength * sin(needleAngleRad).toFloat()

    drawLine(
        color = Color.White,
        start = Offset(centerX, centerY),
        end = Offset(needleEndX, needleEndY),
        strokeWidth = 3f
    )

    // Speed text
    val speedStyle = TextStyle(
        color = Color.White,
        fontSize = (size.height * 0.1f).sp,
        textAlign = TextAlign.Center
    )

    val speedMeasured = textMeasurer.measure(speed.toInt().toString(), speedStyle)
    drawText(
        textLayoutResult = speedMeasured,
        topLeft = Offset(
            centerX - speedMeasured.size.width / 2,
            centerY - speedMeasured.size.height / 2 + size.height * 0.05f
        )
    )

    // Unit text
    val unitStyle = TextStyle(
        color = Color.White,
        fontSize = (size.height * 0.04f).sp,
        textAlign = TextAlign.Center
    )

    val unitMeasured = textMeasurer.measure("km/h", unitStyle)
    drawText(
        textLayoutResult = unitMeasured,
        topLeft = Offset(
            centerX - unitMeasured.size.width / 2,
            centerY + size.height * 0.08f
        )
    )
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawRange(
    textMeasurer: TextMeasurer,
    range: Int
) {
    val labelStyle = TextStyle(
        color = Color.White,
        fontSize = (size.height * 0.06f).sp,
        textAlign = TextAlign.Center
    )

    val labelMeasured = textMeasurer.measure("Range", labelStyle)
    drawText(
        textLayoutResult = labelMeasured,
        topLeft = Offset(
            size.width * 0.5f - labelMeasured.size.width / 2,
            size.height * 0.75f
        )
    )

    val valueStyle = TextStyle(
        color = Color.White,
        fontSize = (size.height * 0.1f).sp,
        textAlign = TextAlign.Center
    )

    val valueMeasured = textMeasurer.measure("$range km", valueStyle)
    drawText(
        textLayoutResult = valueMeasured,
        topLeft = Offset(
            size.width * 0.5f - valueMeasured.size.width / 2,
            size.height * 0.85f
        )
    )
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawBottomBar(
    textMeasurer: TextMeasurer
) {
    val textStyle = TextStyle(
        color = Color.White,
        fontSize = (size.height * 0.04f).sp,
        textAlign = TextAlign.Center
    )

    val bottomY = size.height * 0.95f

    listOf(
        "Selection" to 0.2f,
        "e-Displays" to 0.5f,
        "Settings" to 0.8f
    ).forEach { (text, xRatio) ->
        val measured = textMeasurer.measure(text, textStyle)
        drawText(
            textLayoutResult = measured,
            topLeft = Offset(
                size.width * xRatio - measured.size.width / 2,
                bottomY
            )
        )
    }
}