import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun AutoDashboard(
    modifier: Modifier = Modifier,
    gear: String = "P",
    speed: Float = 0f,
    rpm: Float = 0f,
    range: Int = 500,
    temperature: Float = 20f,
    time: String = "19:00",
    batteryLevel: Int = 85,
    outsideTemp: Float = 22f,
    engineTemp: Int = 90,
    tirePressure: Map<String, Float> = mapOf(
        "FL" to 2.3f,
        "FR" to 2.3f,
        "RL" to 2.2f,
        "RR" to 2.2f
    )
) {
    val density = LocalDensity.current

    // Enhanced animations
    val animatedSpeed by animateFloatAsState(
        targetValue = speed,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "speed"
    )

    val animatedRpm by animateFloatAsState(
        targetValue = rpm,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "rpm"
    )

    val animatedBattery by animateIntAsState(
        targetValue = batteryLevel,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "battery"
    )

    // Pulse animations
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val gearPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gearPulse"
    )

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .padding(16.dp)
    ) {
        // Top Status Bar
        TopStatusBar(
            time = time,
            temperature = temperature,
            batteryLevel = animatedBattery
        )

        // Main Content
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Gear Display
            GearDisplay(
                gear = gear,
                scale = gearPulse
            )

            // Main Gauges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Speed and RPM gauges
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    val width = size.width
                    val height = size.height
                    val radius = min(width, height) * 0.35f

                    // Enhanced gauge backgrounds
                    drawGaugeBackground(
                        centerX = width * 0.3f,
                        centerY = height * 0.5f,
                        radius = radius,
                        rotation = rotationAngle,
                        glowIntensity = glowPulse
                    )

                    drawGaugeBackground(
                        centerX = width * 0.7f,
                        centerY = height * 0.5f,
                        radius = radius,
                        rotation = rotationAngle + 180f,
                        glowIntensity = glowPulse
                    )

                    // Main gauges
                    drawGauge(
                        centerX = width * 0.3f,
                        centerY = height * 0.5f,
                        radius = radius,
                        minValue = 0f,
                        maxValue = 260f,
                        currentValue = animatedSpeed,
                        majorTicks = 20,
                        label = "SPEED",
                        unit = "km/h",
                        needleGlow = glowPulse,
                        density = density,
                        accentColor = Color(0xFF00E5FF)
                    )

                    drawGauge(
                        centerX = width * 0.7f,
                        centerY = height * 0.5f,
                        radius = radius,
                        minValue = 0f,
                        maxValue = 8000f,
                        currentValue = animatedRpm,
                        majorTicks = 8,
                        label = "RPM",
                        unit = "x1000",
                        needleGlow = glowPulse,
                        density = density,
                        accentColor = Color(0xFFFF4081)
                    )
                }
            }

            // Bottom Status Bars
            BottomStatusBars(
                range = range,
                engineTemp = engineTemp,
                tirePressure = tirePressure,
                outsideTemp = outsideTemp
            )
        }
    }
}

@Composable
private fun TopStatusBar(
    time: String,
    temperature: Float,
    batteryLevel: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = time,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            onTextLayout = {}
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Battery Canvas remains the same
            Text(
                text = "$batteryLevel%",
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                onTextLayout = {}
            )
        }

        Text(
            text = "${temperature.toInt()}°C",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            onTextLayout = {}
        )
    }
}
@Composable
private fun GearDisplay(
    gear: String,
    scale: Float
) {
    Surface(
        modifier = Modifier
            .scale(scale)
            .alpha(0.9f),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF2C2C2C)
    ) {
        Text(
            text = gear,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            onTextLayout = {}
        )
    }
}
@Composable
private fun BottomStatusBars(
    range: Int,
    engineTemp: Int,
    tirePressure: Map<String, Float>,
    outsideTemp: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Range indicator
        StatusCard(
            label = "RANGE",
            value = "$range km",
            icon = {
                Canvas(modifier = Modifier.size(24.dp)) {
                    drawCircle(
                        color = Color(0xFF00E676),
                        radius = size.minDimension / 3
                    )
                }
            }
        )

        // Engine temperature
        StatusCard(
            label = "ENGINE TEMP",
            value = "$engineTemp°C",
            icon = {
                Canvas(modifier = Modifier.size(24.dp)) {
                    drawCircle(
                        color = if (engineTemp < 110) Color(0xFF00E676) else Color(0xFFFF5252),
                        radius = size.minDimension / 3
                    )
                }
            }
        )

        // Tire pressure status
        StatusCard(
            label = "TIRES",
            value = "OK",
            icon = {
                Canvas(modifier = Modifier.size(24.dp)) {
                    val isOk = tirePressure.values.all { it in 2.0f..2.5f }
                    drawCircle(
                        color = if (isOk) Color(0xFF00E676) else Color(0xFFFF5252),
                        radius = size.minDimension / 3
                    )
                }
            }
        )

        // Outside temperature
        StatusCard(
            label = "OUTSIDE",
            value = "${outsideTemp.toInt()}°C",
            icon = {
                Canvas(modifier = Modifier.size(24.dp)) {
                    drawCircle(
                        color = Color(0xFF00E676),
                        radius = size.minDimension / 3
                    )
                }
            }
        )
    }
}

@Composable
private fun StatusCard(
    label: String,
    value: String,
    icon: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2C2C2C))
            .padding(8.dp),
        color = Color(0xFF2C2C2C)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            icon()
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                onTextLayout = {}
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                onTextLayout = {}
            )
        }
    }
}
// Rest of the helper functions remain the same...

private fun DrawScope.drawGaugeBackground(
    centerX: Float,
    centerY: Float,
    radius: Float,
    rotation: Float,
    glowIntensity: Float
) {
    // Rotating gradient background
    rotate(rotation, Offset(centerX, centerY)) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF2C2C2C),
                    Color(0xFF1A1A1A).copy(alpha = 0f)
                ),
                center = Offset(centerX, centerY),
                radius = radius * 1.2f
            ),
            center = Offset(centerX, centerY),
            radius = radius * 1.2f
        )
    }

    // Glow effect
    drawCircle(
        color = Color.White.copy(alpha = 0.1f * glowIntensity),
        center = Offset(centerX, centerY),
        radius = radius * 1.1f,
        style = Stroke(
            width = radius * 0.2f,
            cap = StrokeCap.Round
        )
    )
}




private fun DrawScope.drawGauge(
    centerX: Float,
    centerY: Float,
    radius: Float,
    minValue: Float,
    maxValue: Float,
    currentValue: Float,
    majorTicks: Int,
    label: String,
    unit: String,
    needleGlow: Float,
    density: Density,
    accentColor: Color
) {
    val startAngle = 150f
    val sweepAngle = 240f
    val angleRange = sweepAngle / (maxValue - minValue)
    val currentAngle = startAngle + (currentValue - minValue) * angleRange

    // Draw gauge background arc
    drawArc(
        color = Color(0xFF2C2C2C),
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        style = Stroke(width = radius * 0.1f),
        size = Size(radius * 2, radius * 2),
        topLeft = Offset(centerX - radius, centerY - radius)
    )

    // Draw progress arc
    drawArc(
        color = accentColor,
        startAngle = startAngle,
        sweepAngle = (currentValue - minValue) * angleRange,
        useCenter = false,
        style = Stroke(width = radius * 0.1f),
        size = Size(radius * 2, radius * 2),
        topLeft = Offset(centerX - radius, centerY - radius)
    )

    // Draw needle
    val needleLength = radius * 0.8f
    val needleAngleRad = Math.toRadians(currentAngle.toDouble())
    val needleEnd = Offset(
        x = centerX + (needleLength * cos(needleAngleRad)).toFloat(),
        y = centerY + (needleLength * sin(needleAngleRad)).toFloat()
    )

    drawLine(
        color = accentColor,
        start = Offset(centerX, centerY),
        end = needleEnd,
        strokeWidth = radius * 0.05f
    )

    // Draw center circle
    drawCircle(
        color = accentColor,
        radius = radius * 0.1f,
        center = Offset(centerX, centerY)
    )

    // Draw value text
    with(density) {
        drawContext.canvas.nativeCanvas.apply {
            drawText(
                currentValue.toInt().toString(),
                centerX,
                centerY + radius * 0.5f,
                android.graphics.Paint().apply {
                    textSize = radius * 0.2f
                    textAlign = android.graphics.Paint.Align.CENTER
                    color = android.graphics.Color.WHITE
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    AutoDashboard(
        modifier = Modifier.fillMaxSize(),
        gear = "D",
        speed = 120f,
        rpm = 3000f,
        range = 450,
        temperature = 22f,
        time = "20:30",
        batteryLevel = 85,
        engineTemp = 90,
        outsideTemp = 22f,
        tirePressure = mapOf(
            "FL" to 2.3f,
            "FR" to 2.3f,
            "RL" to 2.2f,
            "RR" to 2.2f
        )
    )
}