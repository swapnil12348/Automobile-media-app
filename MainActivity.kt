// MainActivity.kt
package com.example.automobilemediaapp

import android.os.Bundle
import android.util.Log
import android.util.LruCache
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sqrt
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.collectAsState
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.automobilemediaapp.VehiclePhysics.calculateAcceleration
import com.example.automobilemediaapp.VehiclePhysics.calculateNextGear
import com.example.automobilemediaapp.VehiclePhysics.calculateRPM
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.average
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds


private const val BRAKING_THRESHOLD = 5.0f
private const val HARD_BRAKING_THRESHOLD = 10.0f
private const val OPTIMAL_ENGINE_TEMP = 90

// Extension functions should be at file level


data class VehicleAnalytics(
    val avgSpeed: Double = 0.0,
    val maxSpeed: Float = 0f,
    val avgRpm: Double = 0.0,
    val maxRpm: Float = 0f,
    val avgEngineTemp: Double = 0.0,
    val minBatteryLevel: Int = 0,
    val avgAcceleration: Double = 0.0,
    val maxAcceleration: Float = 0f,
    val avgRoadCondition: Double = 0.0
)

@Serializable
data class HourlyAnalytics(
    val hour: String = "",
    val avgSpeed: Float = 0f,
    val avgBatteryLevel: Float = 0f
)

data class VehicleEntity(
    val timestamp: Long,
    val speed: Float,
    val rpm: Float,
    val gear: String,
    val range: Int,
    val batteryLevel: Int,
    val engineTemp: Int,
    val outsideTemp: Float,
    val tirePressureFL: Float,
    val tirePressureFR: Float,
    val tirePressureRL: Float,
    val tirePressureRR: Float,
    val acceleration: Float,
    val accelerationForce: Float,
    val roadCondition: Float,
    val tireWear: Float,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double
)
// Enhanced Vehicle State
data class DiagnosticsState(
    val engineHealth: Float = 1.0f,
    val batteryHealth: Float = 1.0f,
    val tireHealth: Map<TirePosition, Float> = TirePosition.entries.associateWith { 1.0f },
    val transmissionHealth: Float = 1.0f,
    val overallPerformance: Float = 1.0f,
    val lastDiagnosticsRun: Long = 0L,
    val alerts: List<DiagnosticAlert> = emptyList()
)

data class DiagnosticAlert(
    val severity: AlertSeverity,
    val component: VehicleComponent,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
enum class AlertSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}
enum class TirePosition {
    FRONT_LEFT, FRONT_RIGHT, REAR_LEFT, REAR_RIGHT
}

data class VehicleData(
    val timestamp: Long = System.currentTimeMillis(),
    val speed: Float = 0f,
    val rpm: Float = 0f,
    val gear: String = "",
    val range: Int = 0,
    val batteryLevel: Int = 0,
    val engineTemp: Int = 0,
    val outsideTemp: Float = 0f,
    val tirePressureFL: Float = 0f,
    val tirePressureFR: Float = 0f,
    val tirePressureRL: Float = 0f,
    val tirePressureRR: Float = 0f,
    val acceleration: Float = 0f,
    val accelerationForce: Float = 0f,
    val roadCondition: Float = 0f,
    val tireWear: Float = 0f,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null
) {
    // Required empty constructor for Firebase
    constructor() : this(System.currentTimeMillis())
}




sealed class AnalyticsState {
    data object Loading : AnalyticsState()
    data object Success : AnalyticsState()
    data class Error(val message: String) : AnalyticsState()
}

// Advanced Analytics Components
data class MaintenanceRecommendation(
    val severity: MaintenanceSeverity,
    val component: VehicleComponent,
    val recommendation: String,
    val timeToMaintenance: Duration,
    val confidence: Float,
    val diagnosticData: Map<String, Any>
)

enum class MaintenanceSeverity {
    URGENT, HIGH, MEDIUM, LOW, NORMAL
}
enum class VehicleComponent {
    ENGINE, BATTERY, TIRE_FL, TIRE_FR, TIRE_RL, TIRE_RR, TRANSMISSION, OVERALL, COOLING_SYSTEM, BRAKES, TIRES
}
// DiagnosticsManager.kt
// DiagnosticsManager.kt


class DiagnosticsManager(
    private val scope: CoroutineScope,
    private val repository: FirebaseVehicleRepository
) {
    private val _diagnosticEvents = MutableSharedFlow<DiagnosticEvent>()
    val diagnosticEvents = _diagnosticEvents.asSharedFlow()

    // Cache for diagnostic results to avoid unnecessary recalculations
    private val diagnosticCache = ConcurrentHashMap<String, DiagnosticResult>()

    init {
        startPeriodicHealthCheck()
    }

    private fun startPeriodicHealthCheck() {
        scope.launch {
            while (isActive) {
                try {
                    repository.getVehicleDataStream(TimeFrame.LAST_HOUR.duration)
                        .collect { vehicleDataList ->
                            vehicleDataList.lastOrNull()?.let { data ->
                                performHealthCheck(data)
                            }
                        }
                } catch (e: Exception) {
                    Log.e("DiagnosticsManager", "Health check failed", e)
                }
                delay(HEALTH_CHECK_INTERVAL)
            }
        }
    }

    suspend fun performHealthCheck(vehicleData: VehicleEntity): DiagnosticResult {
        val cacheKey = "${vehicleData.timestamp}-${vehicleData.engineTemp}-${vehicleData.batteryLevel}"

        return diagnosticCache.getOrPut(cacheKey) {
            coroutineScope {
                val engineHealth = checkEngineHealth(
                    temperature = vehicleData.engineTemp.toFloat(),
                    rpm = vehicleData.rpm
                )

                val batteryHealth = checkBatteryHealth(vehicleData.batteryLevel.toFloat())
                val tireHealth = checkTireHealth(vehicleData.tireWear)
                val transmissionHealth = checkTransmissionHealth(
                    currentGear = vehicleData.gear.toIntOrNull() ?: 1,
                    speed = vehicleData.speed,
                    rpm = vehicleData.rpm
                )

                val alerts = generateAlerts(vehicleData)

                DiagnosticResult(
                    timestamp = vehicleData.timestamp,
                    engineHealth = engineHealth,
                    batteryHealth = batteryHealth,
                    tireHealth = tireHealth,
                    transmissionHealth = transmissionHealth,
                    alerts = alerts,
                    overallHealth = calculateOverallHealth(
                        engineHealth,
                        batteryHealth,
                        tireHealth.values.average().toFloat(),
                        transmissionHealth
                    )
                )
            }
        }
    }

    suspend fun checkEngineHealth(temperature: Float, rpm: Float): Float = coroutineScope {
        val tempHealth = calculateTemperatureHealth(temperature)
        val rpmHealth = calculateRPMHealth(rpm)
        (tempHealth + rpmHealth) / 2f
    }

    suspend fun checkBatteryHealth(charge: Float): Float = coroutineScope {
        when {
            charge >= 80f -> 1.0f
            charge >= 50f -> 0.8f
            charge >= 20f -> 0.5f
            charge >= 10f -> 0.2f
            else -> 0.1f
        }
    }

    suspend fun checkTireHealth(tireWear: Float): Map<TirePosition, Float> = coroutineScope {
        TirePosition.entries.associateWith {
            calculateTireHealth(tireWear)
        }
    }

    suspend fun checkTransmissionHealth(currentGear: Int, speed: Float, rpm: Float): Float = coroutineScope {
        val optimalGear = calculateOptimalGear(speed, rpm)
        if (currentGear == optimalGear) 1.0f else 0.8f
    }

    suspend fun generateAlerts(vehicleData: VehicleEntity): List<DiagnosticAlert> = coroutineScope {
        val alerts = mutableListOf<DiagnosticAlert>()

        // Check engine temperature
        if (vehicleData.engineTemp > CRITICAL_ENGINE_TEMP) {
            alerts.add(DiagnosticAlert(
                severity = AlertSeverity.CRITICAL,
                component = VehicleComponent.ENGINE,
                message = "Critical engine temperature: ${vehicleData.engineTemp}°C",
                timestamp = System.currentTimeMillis()
            ))
        }

        // Check battery
        when (vehicleData.batteryLevel) {
            in 0..10 -> alerts.add(DiagnosticAlert(
                severity = AlertSeverity.CRITICAL,
                component = VehicleComponent.BATTERY,
                message = "Critical battery level: ${vehicleData.batteryLevel}%",
                timestamp = System.currentTimeMillis()
            ))
            in 11..20 -> alerts.add(DiagnosticAlert(
                severity = AlertSeverity.HIGH,
                component = VehicleComponent.BATTERY,
                message = "Low battery level: ${vehicleData.batteryLevel}%",
                timestamp = System.currentTimeMillis()
            ))
        }

        // Check tire wear
        if (vehicleData.tireWear < CRITICAL_TIRE_WEAR) {
            alerts.add(DiagnosticAlert(
                severity = AlertSeverity.HIGH,
                component = VehicleComponent.TIRES,
                message = "Significant tire wear detected",
                timestamp = System.currentTimeMillis()
            ))
        }

        alerts
    }

    private fun calculateTemperatureHealth(temperature: Float): Float {
        return when {
            temperature <= OPTIMAL_ENGINE_TEMP -> 1.0f
            temperature <= WARNING_ENGINE_TEMP -> 0.7f
            temperature <= CRITICAL_ENGINE_TEMP -> 0.3f
            else -> 0.1f
        }
    }

    private fun calculateRPMHealth(rpm: Float): Float {
        return when {
            rpm <= OPTIMAL_RPM -> 1.0f
            rpm <= WARNING_RPM -> 0.7f
            rpm <= CRITICAL_RPM -> 0.3f
            else -> 0.1f
        }
    }

    private fun calculateTireHealth(wear: Float): Float {
        return wear.coerceIn(0f, 1f)
    }

    private fun calculateOptimalGear(speed: Float, rpm: Float): Int {
        return when {
            speed < 20 -> 1
            speed < 40 -> 2
            speed < 60 -> 3
            speed < 80 -> 4
            else -> 5
        }
    }

    private fun calculateOverallHealth(
        engineHealth: Float,
        batteryHealth: Float,
        tireHealth: Float,
        transmissionHealth: Float
    ): Float {
        return (engineHealth * ENGINE_WEIGHT +
                batteryHealth * BATTERY_WEIGHT +
                tireHealth * TIRE_WEIGHT +
                transmissionHealth * TRANSMISSION_WEIGHT) /
                (ENGINE_WEIGHT + BATTERY_WEIGHT + TIRE_WEIGHT + TRANSMISSION_WEIGHT)
    }

    companion object {
        private const val HEALTH_CHECK_INTERVAL = 1000L // 1 second
        private const val OPTIMAL_ENGINE_TEMP = 90f
        private const val WARNING_ENGINE_TEMP = 100f
        private const val CRITICAL_ENGINE_TEMP = 110f
        private const val OPTIMAL_RPM = 3000f
        private const val WARNING_RPM = 5000f
        private const val CRITICAL_RPM = 6000f
        private const val CRITICAL_TIRE_WEAR = 0.2f

        // Health calculation weights
        private const val ENGINE_WEIGHT = 0.4f
        private const val BATTERY_WEIGHT = 0.3f
        private const val TIRE_WEIGHT = 0.2f
        private const val TRANSMISSION_WEIGHT = 0.1f
    }
}

// DiagnosticResult.kt
data class DiagnosticResult(
    val timestamp: Long,
    val engineHealth: Float,
    val batteryHealth: Float,
    val tireHealth: Map<TirePosition, Float>,
    val transmissionHealth: Float,
    val alerts: List<DiagnosticAlert>,
    val overallHealth: Float
)

// DiagnosticEvent.kt
sealed class DiagnosticEvent {
    data class Alert(val alert: DiagnosticAlert) : DiagnosticEvent()
    data class HealthUpdate(val diagnosticResult: DiagnosticResult) : DiagnosticEvent()
    data class Error(val message: String, val exception: Exception? = null) : DiagnosticEvent()
}

private class PhysicsCalculator {
    fun updateVehicleState(state: VehicleState, deltaTime: Float, random: Random) {
        updateDrivingState(state, random)
        updatePhysics(state, deltaTime)
        updateSystems(state, deltaTime)
    }

    private fun updateDrivingState(state: VehicleState, random: Random) {
        if (random.nextFloat() < 0.05f) {
            state.accelerating = random.nextBoolean()
            state.braking = !state.accelerating && random.nextBoolean()
        }

        if (random.nextFloat() < 0.01f) {
            state.roadCondition = (0.7f + random.nextFloat() * 0.3f).coerceIn(0.7f, 1.0f)
        }
    }

    private fun updatePhysics(state: VehicleState, deltaTime: Float) {
        state.acceleration = calculateAcceleration(
            state.speed,
            state.accelerating,
            state.braking,
            state.roadCondition,
            state.tireWear
        )

        state.speed = (state.speed + state.acceleration * deltaTime)
            .coerceIn(0f, VehiclePhysics.MAX_SPEED)

        state.gear = calculateNextGear(state.gear, state.speed, state.rpm)
        state.rpm = calculateRPM(state.speed, state.gear.toString(), state.accelerating)
    }

    private fun updateSystems(state: VehicleState, deltaTime: Float) {
        updateEngineTemperature(state, deltaTime)
        updateTireWear(state, deltaTime)
        updateBatteryAndRange(state, deltaTime)
    }

    private fun updateEngineTemperature(state: VehicleState, deltaTime: Float) {
        val rpmLoad = (state.rpm - VehiclePhysics.IDLE_RPM) /
                (VehiclePhysics.MAX_RPM - VehiclePhysics.IDLE_RPM)
        val speedLoad = state.speed / VehiclePhysics.MAX_SPEED
        val totalLoad = (rpmLoad + speedLoad) * 0.5f

        state.engineTemperature = (state.engineTemperature +
                (totalLoad * 5f - (state.engineTemperature - 85f) * 0.1f) * deltaTime)
            .coerceIn(85f, 105f)
    }

    private fun updateTireWear(state: VehicleState, deltaTime: Float) {
        state.tireWear = (state.tireWear - VehiclePhysics.TIRE_WEAR_RATE *
                state.speed * deltaTime * (if (state.braking) 2f else 1f))
            .coerceIn(0.5f, 1.0f)
    }

    private fun updateBatteryAndRange(state: VehicleState, deltaTime: Float) {
        val powerDraw = (state.speed * 0.001f +
                state.acceleration * 0.0005f +
                state.rpm * 0.0001f) * deltaTime

        state.batteryCharge = (state.batteryCharge -
                powerDraw / VehiclePhysics.BATTERY_EFFICIENCY)
            .coerceIn(0f, 100f)

        state.range = (state.batteryCharge * 5).toInt()
    }
}

private class VehicleDataGenerator {
    private val weatherEffect = FloatArray(360) {
        sin(it * Math.PI / 180).toFloat() * 2f
    }

    fun generateVehicleData(state: VehicleState, timestamp: Long): VehicleData {
        val weatherIndex = ((timestamp / 10000) % 360).toInt()
        val tirePressureVariation = 0.05f * (Random.nextFloat() - 0.5f)

        return VehicleData(
            timestamp = timestamp,
            speed = state.speed,
            rpm = state.rpm,
            gear = state.gear.toString(),
            range = state.range,
            batteryLevel = state.batteryCharge.toInt(),
            engineTemp = state.engineTemperature.roundToInt(),
            outsideTemp = 22f + weatherEffect[weatherIndex],
            tirePressureFL = 2.3f + tirePressureVariation,
            tirePressureFR = 2.3f + tirePressureVariation,
            tirePressureRL = 2.3f + tirePressureVariation,
            tirePressureRR = 2.3f + tirePressureVariation,
            acceleration = state.acceleration,
            accelerationForce = state.speed * state.speed * VehiclePhysics.DRAG_COEFFICIENT,
            roadCondition = state.roadCondition,
            tireWear = state.tireWear,
            latitude = 37.7749 + (Random.nextDouble() - 0.5) * 0.01,
            longitude = -122.4194 + (Random.nextDouble() - 0.5) * 0.01,
            altitude = Random.nextDouble() * 10
        )
    }
}



// Stable state holder for physics calculations
@Stable
class PhysicsState(
    initialSpeed: Float,
    initialRPM: Float
) {
    var speed by mutableStateOf(initialSpeed)
        private set
    var rpm by mutableStateOf(initialRPM)
        private set

    fun update(drivingState: DriveState) {
        speed = when (drivingState) {
            DriveState.ACCELERATING -> (speed + 2f).coerceIn(0f, 200f)
            DriveState.BRAKING -> (speed - 3f).coerceIn(0f, 200f)
            DriveState.COASTING -> (speed - 1f).coerceIn(0f, 200f)
        }

        rpm = when (drivingState) {
            DriveState.ACCELERATING -> (rpm + 100f).coerceIn(800f, 6500f)
            DriveState.BRAKING -> (rpm - 150f).coerceIn(800f, 6500f)
            DriveState.COASTING -> (rpm - 50f).coerceIn(800f, 6500f)
        }
    }
}

enum class DriveState {
    ACCELERATING, BRAKING, COASTING
}

@Composable
fun ModernCircularGauge(
    value: Float,
    maxValue: Float,
    title: String,
    unit: String,
    modifier: Modifier = Modifier,
    accentColor: Color,
    animationDuration: Int = 1000,
    previousValue: Float? = null
) {
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val progress = value.coerceIn(0f, maxValue) / maxValue

    // Cache calculations
    val sweepAngle = remember(animatedProgress) {
        (animatedProgress * 270f).coerceIn(0f, 270f)
    }

    val startAngle = remember { 135f }
    val thickness = remember { 24.dp }

    LaunchedEffect(value) {
        animate(
            initialValue = animatedProgress,
            targetValue = progress,
            animationSpec = tween(
                durationMillis = animationDuration,
                easing = FastOutSlowInEasing
            )
        ) { value, _ ->
            animatedProgress = value
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(Color(0xFF2A2A2A), RoundedCornerShape(24.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background arc
            drawArc(
                color = Color(0xFF3A3A3A),
                startAngle = startAngle,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(thickness.toPx(), cap = StrokeCap.Round),
                size = size
            )

            // Progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.5f),
                        accentColor
                    ),
                    center = center
                ),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(thickness.toPx(), cap = StrokeCap.Round),
                size = size
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 14.sp,
                    letterSpacing = 2.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${value.roundToInt()}",
                    style = TextStyle(
                        fontSize = 48.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )

                if (previousValue != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    TrendIndicator(currentValue = value, previousValue = previousValue)
                }
            }
            Text(
                text = unit,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            )
        }
    }
}

@Composable
fun MainDashboard(
    initialVehicleState: VehicleState,
    vehicleMetrics: VehicleMetrics?,
    modifier: Modifier = Modifier
) {
    var isAccelerating by remember { mutableStateOf(false) }
    var isBraking by remember { mutableStateOf(false) }

    val physicsState = remember { PhysicsState(initialVehicleState.speed, initialVehicleState.rpm) }

    // Combine driving states
    val drivingState = remember {
        derivedStateOf {
            when {
                isAccelerating && !isBraking -> DriveState.ACCELERATING
                isBraking && !isAccelerating -> DriveState.BRAKING
                else -> DriveState.COASTING
            }
        }
    }

    LaunchedEffect(drivingState.value) {
        while (true) {
            physicsState.update(drivingState.value)
            delay(100)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Gauges Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ModernCircularGauge(
                value = physicsState.speed,
                maxValue = 200f,
                title = "SPEED",
                unit = "km/h",
                modifier = Modifier.weight(1f),
                accentColor = Color(0xFF00E5FF)
            )

            ModernCircularGauge(
                value = physicsState.rpm,
                maxValue = 8000f,
                title = "RPM",
                unit = "rpm",
                modifier = Modifier.weight(1f),
                accentColor = Color(0xFFFF4081)
            )
        }

        // Status Grid with throttled updates
        vehicleMetrics?.currentData?.let { data ->
            key(data.hashCode()) {
                StatusGrid(
                    vehicleData = data,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Control Buttons with debounced input
        ControlButtons(
            onAccelerate = { isAccelerating = it },
            onBrake = { isBraking = it },
            isAccelerating = isAccelerating,
            isBraking = isBraking
        )
    }
}

@Composable
private fun ControlButtons(
    onAccelerate: (Boolean) -> Unit,
    onBrake: (Boolean) -> Unit,
    isAccelerating: Boolean,
    isBraking: Boolean
) {
    var lastUpdateTime by remember { mutableLongStateOf(0L) }
    val minUpdateInterval = 16L // ~60fps

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onAccelerate(!isAccelerating) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isAccelerating) Color(0xFF00E5FF)
                else Color(0xFF2C2C2C)
            ),
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastUpdateTime >= minUpdateInterval) {
                                when (event.type) {
                                    PointerEventType.Press -> onAccelerate(true)
                                    PointerEventType.Release -> onAccelerate(false)
                                }
                                lastUpdateTime = currentTime
                            }
                        }
                    }
                }
        ) {
            AccelerateButtonContent()
        }

        Button(
            onClick = { onBrake(!isBraking) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isBraking) Color(0xFFFF4081)
                else Color(0xFF2C2C2C)
            ),
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastUpdateTime >= minUpdateInterval) {
                                when (event.type) {
                                    PointerEventType.Press -> onBrake(true)
                                    PointerEventType.Release -> onBrake(false)
                                }
                                lastUpdateTime = currentTime
                            }
                        }
                    }
                }
        ) {
            BrakeButtonContent()
        }
    }
}

@Composable
private fun AccelerateButtonContent() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = "Accelerate",
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = "Accelerate",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun BrakeButtonContent() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Brake",
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = "Brake",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun TrendIndicator(
    currentValue: Float,
    previousValue: Float,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = when {
            currentValue > previousValue -> Icons.Default.KeyboardArrowUp
            currentValue < previousValue -> Icons.Default.KeyboardArrowDown
            else -> Icons.Default.Remove
        },
        contentDescription = "Trend",
        tint = when {
            currentValue > previousValue -> Color.Green
            currentValue < previousValue -> Color.Red
            else -> Color.Gray
        },
        modifier = modifier.size(24.dp)
    )
}




@Composable
fun StatusCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF2A2A2A)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = Color.Gray
                )
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value,
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.Gray
                    ),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}



@Composable
fun TopBar(
    showAnalytics: Boolean,
    onToggleAnalytics: () -> Unit,
    selectedTimeFrame: TimeFrame,
    onTimeFrameSelected: (TimeFrame) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggleAnalytics) {
            Icon(
                imageVector = if (showAnalytics) Icons.Default.Close else Icons.Default.Analytics,
                contentDescription = "Toggle Analytics",
                tint = Color.White
            )
        }

        TimeFrameSelector(
            selectedTimeFrame = selectedTimeFrame,
            onTimeFrameSelected = onTimeFrameSelected
        )
    }
}




@Composable
private fun HealthCard(
    title: String,
    health: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator(
                progress = health,
                modifier = Modifier.size(64.dp),
                color = when {
                    health > 0.7f -> Color(0xFF4CAF50)
                    health > 0.4f -> Color(0xFFFFB74D)
                    else -> Color(0xFFE57373)
                }
            )
        }
    }
}
enum class Screen {
    DASHBOARD,
    ANALYTICS,
    DIAGNOSTICS,
    SETTINGS
}

@Composable
private fun TimeFrameSelector(
    selectedTimeFrame: TimeFrame,  // Changed from currentTimeFrame
    onTimeFrameSelected: (TimeFrame) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(TimeFrame.entries.toTypedArray()) { timeFrame ->
            TimeFrameChip(
                timeFrame = timeFrame,
                selected = timeFrame == selectedTimeFrame,  // Updated to match new parameter name
                onSelected = { onTimeFrameSelected(timeFrame) }
            )
        }
    }
}
@Stable
private object ChartColors {
    val Speed = Color(0xFF00E5FF)
    val RPM = Color(0xFFFF4081)
    val Acceleration = Color(0xFF4CAF50)
    val Battery = Color(0xFFFFEB3B)
    val EngineTemp = Color(0xFFFF9800)
    val RoadCondition = Color(0xFF9C27B0)
}

@Stable
private data class ChartData(
    val values: List<Float>,
    val maxValue: Float,
    val minValue: Float
)

@Composable
fun EnhancedAnalyticsChart(
    data: List<VehicleData>,
    selectedMetric: VehicleMetric,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val interactionSource = remember { MutableInteractionSource() }
    var hoveredPoint by remember { mutableStateOf<Pair<Float, Float>?>(null) }

    val metricColor = remember(selectedMetric) {
        when (selectedMetric) {
            VehicleMetric.SPEED -> ChartColors.Speed
            VehicleMetric.RPM -> ChartColors.RPM
            VehicleMetric.ACCELERATION -> ChartColors.Acceleration
            VehicleMetric.BATTERY -> ChartColors.Battery
            VehicleMetric.ENGINE_TEMP -> ChartColors.EngineTemp
            VehicleMetric.ROAD_CONDITION -> ChartColors.RoadCondition
        }
    }

    val chartData by remember(data, selectedMetric) {
        derivedStateOf {
            val values = when (selectedMetric) {
                VehicleMetric.SPEED -> data.map { it.speed }
                VehicleMetric.RPM -> data.map { it.rpm }
                VehicleMetric.ACCELERATION -> data.map { it.acceleration }
                VehicleMetric.ROAD_CONDITION -> data.map { it.roadCondition }
                VehicleMetric.BATTERY -> data.map { it.batteryLevel.toFloat().coerceIn(0f, 100f) }
                VehicleMetric.ENGINE_TEMP -> data.map { it.engineTemp.toFloat() }
            }

            val (minValue, maxValue) = when (selectedMetric) {
                VehicleMetric.BATTERY -> Pair(0f, 100f)
                VehicleMetric.ENGINE_TEMP -> Pair(0f, values.maxOrNull()?.coerceAtLeast(90f) ?: 90f)
                VehicleMetric.SPEED -> Pair(0f, values.maxOrNull()?.coerceAtLeast(120f) ?: 120f)
                VehicleMetric.RPM -> Pair(0f, values.maxOrNull()?.coerceAtLeast(8000f) ?: 8000f)
                else -> Pair(
                    values.minOrNull() ?: 0f,
                    values.maxOrNull() ?: 0f
                )
            }

            ChartData(
                values = values,
                maxValue = maxValue,
                minValue = minValue
            )
        }
    }

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(data, selectedMetric) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(500)
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF2C2C2C)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .hoverable(interactionSource)
        ) {
            // Draw grid and axes
            drawGrid(
                maxValue = chartData.maxValue,
                minValue = chartData.minValue,
                textMeasurer = textMeasurer
            )

            // Draw the animated line chart
            drawAnimatedLine(
                data = chartData.values,
                maxValue = chartData.maxValue,
                minValue = chartData.minValue,
                width = size.width,
                height = size.height,
                progress = animatedProgress.value,
                color = metricColor,
                onPointHover = { x, y -> hoveredPoint = x to y }
            )

            // Draw tooltip if point is hovered
            hoveredPoint?.let { (x, y) ->
                drawTooltip(
                    point = Offset(x, y),
                    value = y,
                    metric = selectedMetric,
                    textMeasurer = textMeasurer
                )
            }
        }
    }
}

private fun DrawScope.drawGrid(
    maxValue: Float,
    minValue: Float,
    textMeasurer: TextMeasurer
) {
    val gridLines = 5
    val range = (maxValue - minValue)

    // Draw horizontal grid lines and labels
    for (i in 0..gridLines) {
        val y = size.height - (i.toFloat() / gridLines) * size.height
        val value = minValue + (i.toFloat() / gridLines) * range

        drawLine(
            color = Color.White.copy(alpha = 0.1f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx()
        )

        // Draw value label
        drawText(
            textMeasurer = textMeasurer,
            text = value.roundToInt().toString(),
            style = TextStyle(
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            ),
            topLeft = Offset(-35f, y - 8f)
        )
    }

    // Draw vertical grid lines
    val timeSteps = 6
    for (i in 0..timeSteps) {
        val x = (i.toFloat() / timeSteps) * size.width
        drawLine(
            color = Color.White.copy(alpha = 0.1f),
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private fun DrawScope.drawAnimatedLine(
    data: List<Float>,
    maxValue: Float,
    minValue: Float,
    width: Float,
    height: Float,
    progress: Float,
    color: Color,
    onPointHover: (Float, Float) -> Unit
) {
    if (data.isEmpty()) return

    val path = Path()
    val range = (maxValue - minValue).coerceAtLeast(0.1f)

    val horizontalPadding = width * 0.05f
    val verticalPadding = height * 0.05f
    val drawableWidth = width - (2 * horizontalPadding)
    val drawableHeight = height - (2 * verticalPadding)

    // Calculate control points for smooth curve
    val points = (data.indices.take((data.size * progress).toInt() + 1))
        .map { index ->
            Offset(
                x = horizontalPadding + (index.toFloat() / (data.size - 1)) * drawableWidth,
                y = verticalPadding + (drawableHeight - ((data[index] - minValue) / range) * drawableHeight)
            )
        }

    // Draw smooth curve
    if (points.size > 1) {
        path.moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            val p0 = points[i - 1]
            val p1 = points[i]

            // Calculate control points for cubic bezier curve
            val controlPoint1 = Offset(
                x = p0.x + (p1.x - p0.x) / 2,
                y = p0.y
            )
            val controlPoint2 = Offset(
                x = p0.x + (p1.x - p0.x) / 2,
                y = p1.y
            )

            path.cubicTo(
                controlPoint1.x, controlPoint1.y,
                controlPoint2.x, controlPoint2.y,
                p1.x, p1.y
            )
        }
    }

    // Draw the line with a gradient
    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(
                color,
                color.copy(alpha = 0.5f)
            )
        ),
        style = Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Draw points with animation and hover detection
    points.forEachIndexed { index, offset ->
        drawCircle(
            color = color,
            radius = 3.dp.toPx(),
            center = offset
        )

        // Check for hover and trigger callback
        if (index < data.size) {
            onPointHover(offset.x, data[index])
        }
    }
}

private fun DrawScope.drawTooltip(
    point: Offset,
    value: Float,
    metric: VehicleMetric,
    textMeasurer: TextMeasurer
) {
    val tooltipText = when (metric) {
        VehicleMetric.SPEED -> "${value.roundToInt()} km/h"
        VehicleMetric.RPM -> "${value.roundToInt()} RPM"
        VehicleMetric.ACCELERATION -> "${value.roundToInt()} m/s²"
        VehicleMetric.BATTERY -> "${value.roundToInt()}%"
        VehicleMetric.ENGINE_TEMP -> "${value.roundToInt()}°C"
        VehicleMetric.ROAD_CONDITION -> "${value.roundToInt()}"
    }

    val tooltipPadding = 8.dp.toPx()
    val tooltipRadius = 4.dp.toPx()

    drawRoundRect(
        color = Color.Black.copy(alpha = 0.8f),
        topLeft = Offset(
            point.x - tooltipPadding * 2,
            point.y - tooltipPadding * 4
        ),
        size = androidx.compose.ui.geometry.Size(
            tooltipPadding * 8,
            tooltipPadding * 3
        ),
        cornerRadius = CornerRadius(tooltipRadius, tooltipRadius)
    )

    drawText(
        textMeasurer = textMeasurer,
        text = tooltipText,
        style = TextStyle(
            color = Color.White,
            fontSize = 12.sp
        ),
        topLeft = Offset(
            point.x - tooltipPadding,
            point.y - tooltipPadding * 3.5f
        )
    )
}

@Composable
fun StatusGrid(
    vehicleData: VehicleData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatusGridItem(
            data = StatusGridItemData(
                title = "RANGE",
                value = vehicleData.range.toString(),
                unit = "km",
                icon = Icons.Default.Speed
            ),
            modifier = Modifier.weight(1f)
        )
        StatusGridItem(
            data = StatusGridItemData(
                title = "BATTERY",
                value = vehicleData.batteryLevel.toString(),
                unit = "%",
                icon = Icons.Default.BatteryStd
            ),
            modifier = Modifier.weight(1f)
        )
        StatusGridItem(
            data = StatusGridItemData(
                title = "TEMP",
                value = vehicleData.engineTemp.toString(),
                unit = "°C",
                icon = Icons.Default.Thermostat
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Stable
private data class StatusGridItemData(
    val title: String,
    val value: String,
    val unit: String,
    val icon: ImageVector
)

@Composable
private fun StatusGridItem(
    data: StatusGridItemData,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF2A2A2A)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = data.icon,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = data.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    letterSpacing = 1.sp,
                    color = Color.Gray
                )
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = data.value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = data.unit,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    ),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
fun EnhancedAnalyticsSummary(
    stats: VehicleStatistics,
    selectedMetric: VehicleMetric
) {
    val summaryData = remember(stats, selectedMetric) {
        when (selectedMetric) {
            VehicleMetric.SPEED -> SummaryData(
                leftCard = InfoCardData("Avg Speed", "${stats.averageSpeed.roundToInt()} km/h"),
                rightCard = InfoCardData("Max Speed", "${stats.maxSpeed.roundToInt()} km/h")
            )
            VehicleMetric.RPM -> SummaryData(
                leftCard = InfoCardData("Avg RPM", "${stats.averageRPM.roundToInt()}"),
                rightCard = InfoCardData("Max RPM", "${stats.maxRPM.roundToInt()}")
            )
            VehicleMetric.ACCELERATION -> SummaryData(
                leftCard = InfoCardData("Avg Accel", "${String.format("%.1f", stats.averageAcceleration)} m/s²"),
                rightCard = InfoCardData("Max Accel", "${String.format("%.1f", stats.maxAcceleration)} m/s²")
            )
            VehicleMetric.BATTERY -> SummaryData(
                leftCard = InfoCardData("Battery Drain", "${stats.averageBatteryDrain.roundToInt()}%"),
                rightCard = InfoCardData("Efficiency", "${stats.efficiency.roundToInt()}%")
            )
            VehicleMetric.ENGINE_TEMP -> SummaryData(
                leftCard = InfoCardData("Avg Temp", "${stats.average.roundToInt()}°C"),
                rightCard = InfoCardData("Max Temp", "${stats.max.roundToInt()}°C")
            )
            VehicleMetric.ROAD_CONDITION -> SummaryData(
                leftCard = InfoCardData("Road Quality", "${String.format("%.1f", stats.average)}/10"),
                rightCard = InfoCardData("Min Quality", "${String.format("%.1f", stats.min)}/10")
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        InfoCard(
            data = summaryData.leftCard,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        InfoCard(
            data = summaryData.rightCard,
            modifier = Modifier.weight(1f)
        )
    }
}

@Stable
private data class InfoCardData(
    val title: String,
    val value: String
)

@Stable
private data class SummaryData(
    val leftCard: InfoCardData,
    val rightCard: InfoCardData
)

@Composable
private fun InfoCard(
    data: InfoCardData,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF2C2C2C)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = data.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = data.value,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }
    }
}
@Composable
fun DashboardScreen(viewModel: VehicleViewModel = viewModel()) {
    var showAnalytics by remember { mutableStateOf(false) }
    var selectedTimeFrame by remember { mutableStateOf(TimeFrame.LAST_HOUR) }

    val vehicleMetrics by viewModel.vehicleMetrics.collectAsState(initial = null)
    val vehicleState by viewModel.vehicleState.collectAsState()

    LaunchedEffect(vehicleState) {
        viewModel.updateVehicleState(vehicleState.toVehicleData())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .padding(16.dp)
    ) {
        TopBar(
            showAnalytics = showAnalytics,
            onToggleAnalytics = { showAnalytics = !showAnalytics },
            selectedTimeFrame = selectedTimeFrame,
            onTimeFrameSelected = { selectedTimeFrame = it }
        )

        if (showAnalytics) {
            AnalyticsScreen(
                viewModel = viewModel,
                selectedTimeFrame = selectedTimeFrame
            )
        } else {
            MainDashboard(
                initialVehicleState = vehicleState,
                vehicleMetrics = vehicleMetrics,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Stable
private data class DiagnosticsScreenState(
    val vehicleState: VehicleState,
    val diagnosticsAlerts: List<DiagnosticAlert>
)

@Composable
private fun HealthStatusGrid(diagnosticsState: DiagnosticsState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HealthCard(
            title = "Engine",
            health = diagnosticsState.engineHealth,
            modifier = Modifier.weight(1f)
        )
        HealthCard(
            title = "Battery",
            health = diagnosticsState.batteryHealth,
            modifier = Modifier.weight(1f)
        )
        HealthCard(
            title = "Transmission",
            health = diagnosticsState.transmissionHealth,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AlertsList(alerts: List<DiagnosticAlert>) {
    Text(
        text = "Active Alerts",
        style = MaterialTheme.typography.titleMedium,
        color = Color.White
    )
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = alerts,
            key = { alert -> alert.timestamp }
        ) { alert ->
            AlertCard(alert = alert)
        }
    }
}


@Composable
fun AnalyticsScreen(
    viewModel: VehicleViewModel,
    selectedTimeFrame: TimeFrame
) {
    val analytics by viewModel.analyticsState.collectAsState()
    val vehicleMetrics by viewModel.vehicleMetrics.collectAsState()
    var selectedMetric by remember { mutableStateOf(VehicleMetric.SPEED) }
    var historicalData by remember { mutableStateOf<List<VehicleData>>(emptyList()) }

    LaunchedEffect(selectedTimeFrame) {
        viewModel.repository.getVehicleDataStream(selectedTimeFrame.duration)
            .map { entityList -> entityList.map { it.toVehicleData() } }
            .collect { dataList ->
                historicalData = dataList
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        MetricSelector(
            selectedMetric = selectedMetric,
            onMetricSelected = { selectedMetric = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (analytics) {
            is AnalyticsState.Success -> {
                AnalyticsContent(
                    vehicleMetrics = vehicleMetrics,
                    historicalData = historicalData,
                    selectedMetric = selectedMetric
                )
            }
            is AnalyticsState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = Color(0xFF00E5FF)
                )
            }
            is AnalyticsState.Error -> {
                Text(
                    text = (analytics as AnalyticsState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun MetricSelector(
    selectedMetric: VehicleMetric,
    onMetricSelected: (VehicleMetric) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        VehicleMetric.values().forEach { metric ->
            TextButton(
                onClick = { onMetricSelected(metric) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (selectedMetric == metric)
                        Color(0xFF00E5FF) else Color.White
                )
            ) {
                Text(metric.displayName)
            }
        }
    }
}

@Composable
private fun AnalyticsContent(
    vehicleMetrics: VehicleMetrics?,
    historicalData: List<VehicleData>,
    selectedMetric: VehicleMetric
) {
    vehicleMetrics?.analytics?.let { analytics ->
        EnhancedAnalyticsSummary(
            stats = analytics.toVehicleStatistics(),
            selectedMetric = selectedMetric
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (historicalData.isNotEmpty()) {
        EnhancedAnalyticsChart(
            data = historicalData,
            selectedMetric = selectedMetric,
            modifier = Modifier.height(250.dp)
        )
    }
}


@Composable
private fun SettingsCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}
@Stable
private data class SettingsScreenState(
    val vehicleState: VehicleState,
    val displaySettings: DisplaySettings,
    val unitSettings: UnitSettings,
    val vehiclePreferences: VehiclePreferences
)

@Composable
private fun DrivingModeSection(
    currentMode: DrivingMode,
    onModeSelected: (DrivingMode) -> Unit
) {
    SettingsCard(title = "Driving Mode") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DrivingMode.values().forEach { mode ->
                Button(
                    onClick = { onModeSelected(mode) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentMode == mode)
                            Color(0xFF00E5FF) else Color(0xFF404040)
                    )
                ) {
                    Text(mode.name)
                }
            }
        }
    }
}
@Composable
private fun DisplaySettingsSection(
    settings: DisplaySettings,
    onSettingsChanged: (DisplaySettings) -> Unit
) {
    SettingsCard(title = "Display") {
        Column {
            BrightnessControl(
                brightness = settings.brightness,
                onBrightnessChanged = { newBrightness ->
                    onSettingsChanged(settings.copy(brightness = newBrightness))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            NightModeToggle(
                nightMode = settings.nightMode,
                onNightModeChanged = { newNightMode ->
                    onSettingsChanged(settings.copy(nightMode = newNightMode))
                }
            )
        }
    }
}
@Composable
private fun BrightnessControl(
    brightness: Float,
    onBrightnessChanged: (Float) -> Unit
) {
    Column {
        Text(
            text = "Brightness",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
        Slider(
            value = brightness,
            onValueChange = onBrightnessChanged,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF00E5FF),
                activeTrackColor = Color(0xFF00E5FF)
            ),
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
@Composable
private fun NightModeToggle(
    nightMode: Boolean,
    onNightModeChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Night Mode",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
        Switch(
            checked = nightMode,
            onCheckedChange = onNightModeChanged,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00E5FF),
                checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun DiagnosticsScreen(
    viewModel: VehicleViewModel,
    modifier: Modifier = Modifier
) {
    val vehicleState by viewModel.vehicleState.collectAsState()
    val diagnosticsAlerts by viewModel.diagnosticsAlerts.collectAsState()

    val screenState by remember(vehicleState, diagnosticsAlerts) {
        derivedStateOf {
            DiagnosticsScreenState(
                vehicleState = vehicleState,
                diagnosticsAlerts = diagnosticsAlerts
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .padding(16.dp)
    ) {
        Text(
            text = "Diagnostics",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        HealthStatusGrid(diagnosticsState = screenState.vehicleState.diagnosticsState)

        Spacer(modifier = Modifier.height(24.dp))

        if (screenState.diagnosticsAlerts.isNotEmpty()) {
            AlertsList(alerts = screenState.diagnosticsAlerts)
        }
    }
}



@Composable
private fun AlertCard(
    alert: DiagnosticAlert,
    modifier: Modifier = Modifier
) {
    val alertColor = remember(alert.severity) {
        when (alert.severity) {
            AlertSeverity.CRITICAL -> Color(0xFFB71C1C)
            AlertSeverity.HIGH -> Color(0xFFF57F17)
            AlertSeverity.MEDIUM -> Color(0xFFFFA000)
            AlertSeverity.LOW -> Color(0xFF2E7D32)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = alertColor)
    ) {
        AlertContent(alert = alert)
    }
}

@Composable
private fun AlertContent(alert: DiagnosticAlert) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (alert.component) {
                    VehicleComponent.ENGINE -> Icons.Rounded.Warning
                    VehicleComponent.BATTERY -> Icons.Rounded.BatteryAlert
                    VehicleComponent.TRANSMISSION -> Icons.Rounded.Error
                    else -> Icons.Rounded.Info
                },
                contentDescription = "Alert icon for ${alert.component}",
                tint = Color.White
            )

            Column {
                Text(
                    text = alert.component.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Text(
            text = formatTimestamp(alert.timestamp),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun VehicleApp(viewModel: VehicleViewModel, modifier: Modifier = Modifier) {
    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when (currentScreen) {
                Screen.DASHBOARD -> DashboardScreen(viewModel)
                Screen.ANALYTICS -> AnalyticsScreen(
                    viewModel = viewModel,
                    selectedTimeFrame = TimeFrame.LAST_HOUR
                )
                Screen.DIAGNOSTICS -> DiagnosticsScreen(viewModel)
                Screen.SETTINGS -> TODO()
            }
        }

        NavigationBar(currentScreen, onScreenSelected = { currentScreen = it })
    }
}

@Composable
private fun NavigationBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF1A1A1A),
        contentColor = Color.White
    ) {
        Screen.values().forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = remember(screen) {
                            when (screen) {
                                Screen.DASHBOARD -> Icons.Default.Dashboard
                                Screen.ANALYTICS -> Icons.Default.Analytics
                                Screen.DIAGNOSTICS -> Icons.Default.Build
                                Screen.SETTINGS -> Icons.Default.Settings
                            }
                        },
                        contentDescription = screen.name
                    )
                },
                label = { Text(screen.name) },
                selected = currentScreen == screen,
                onClick = { onScreenSelected(screen) }
            )
        }
    }
}


@Composable
private fun TimeFrameChip(
    timeFrame: TimeFrame,
    selected: Boolean,
    onSelected: () -> Unit
) {
    val chipColors = MaterialTheme.colorScheme
    val borderColor by remember(selected, chipColors) {
        derivedStateOf {
            if (selected) chipColors.primary
            else chipColors.onSurface.copy(alpha = 0.12f)
        }
    }

    Surface(
        modifier = Modifier.clickable(onClick = onSelected),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) chipColors.primary else chipColors.surface,
        border = BorderStroke(width = 1.dp, color = borderColor)
    ) {
        Text(
            text = timeFrame.label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (selected) chipColors.onPrimary else chipColors.onSurface
        )
    }
}


// Extension functions
private fun VehicleState.toVehicleData() = VehicleData(
    speed = speed,
    rpm = rpm,
    gear = gear.toString(),
    range = range,
    batteryLevel = batteryCharge.toInt(),
    engineTemp = engineTemperature.toInt(),
    acceleration = acceleration,
    roadCondition = roadCondition,
    tireWear = tireWear
)
object MaintenanceAnalyzer {
    private const val MAX_DISCHARGE_RATE = 5.0

    // Cache severity thresholds
    private val severityThresholds = arrayOf(0.3f, 0.5f, 0.7f, 0.9f)
    private val severityLevels = arrayOf(
        MaintenanceSeverity.URGENT,
        MaintenanceSeverity.HIGH,
        MaintenanceSeverity.MEDIUM,
        MaintenanceSeverity.LOW,
        MaintenanceSeverity.NORMAL
    )

    fun analyze(
        vehicleData: List<VehicleData>,
        diagnosticsState: DiagnosticsState
    ): MaintenanceRecommendation {
        if (vehicleData.isEmpty()) {
            return createNormalRecommendation()
        }

        // Calculate all component scores in a single pass
        val stats = calculateVehicleStats(vehicleData)

        val componentScores = mutableMapOf<VehicleComponent, Float>().apply {
            // Engine score
            put(
                VehicleComponent.ENGINE,
                calculateEngineScore(stats.tempVariation, stats.rpmPattern, diagnosticsState.engineHealth)
            )

            // Battery score
            put(
                VehicleComponent.BATTERY,
                calculateBatteryScore(stats.dischargeCurve, diagnosticsState.batteryHealth)
            )

            // Tire score
            put(
                VehicleComponent.TIRES,
                diagnosticsState.tireHealth.values.average().toFloat()
            )
        }

        val criticalComponent = componentScores.minByOrNull { it.value }
            ?: return createNormalRecommendation()

        return createRecommendation(
            criticalComponent.key,
            criticalComponent.value,
            vehicleData,
            diagnosticsState,
            stats
        )
    }

    private data class VehicleStats(
        val tempVariation: Float,
        val rpmPattern: Float,
        val dischargeCurve: Float
    )

    private fun calculateVehicleStats(data: List<VehicleData>): VehicleStats {
        var sumTemp = 0.0
        var sumTempSquared = 0.0
        var sumBatteryDiff = 0.0
        var rpmPatternScore = 0.0
        val n = data.size.toDouble()

        // Single pass for all needed statistics
        var prevBatteryLevel = data.first().batteryLevel
        data.forEach { vehicleData ->
            // Temperature stats
            val temp = vehicleData.engineTemp.toDouble()
            sumTemp += temp
            sumTempSquared += temp * temp

            // Battery discharge
            val batteryDiff = prevBatteryLevel - vehicleData.batteryLevel
            sumBatteryDiff += batteryDiff
            prevBatteryLevel = vehicleData.batteryLevel

            // RPM pattern analysis (simplified to avoid multiple passes)
            rpmPatternScore += analyzeRPMInstant()
        }

        val avgTemp = sumTemp / n
        val tempVariance = (sumTempSquared / n) - (avgTemp * avgTemp)

        return VehicleStats(
            tempVariation = sqrt(tempVariance).toFloat(),
            rpmPattern = (rpmPatternScore / n).toFloat(),
            dischargeCurve = (sumBatteryDiff / (n - 1)).toFloat()
        )
    }

    private fun calculateEngineScore(
        tempVariation: Float,
        rpmPattern: Float,
        engineHealth: Float
    ): Float =
        (engineHealth + (1 - tempVariation / 100) + rpmPattern) / 3

    private fun calculateBatteryScore(
        dischargeCurve: Float,
        batteryHealth: Float
    ): Float =
        (batteryHealth + (1 - (dischargeCurve / MAX_DISCHARGE_RATE).toFloat())) / 2

    private fun createRecommendation(
        component: VehicleComponent,
        score: Float,
        data: List<VehicleData>,
        diagnosticsState: DiagnosticsState,
        stats: VehicleStats
    ): MaintenanceRecommendation {
        val severityIndex = severityThresholds.indexOfFirst { score < it }
        val severity = if (severityIndex == -1) MaintenanceSeverity.NORMAL else severityLevels[severityIndex]

        return MaintenanceRecommendation(
            severity = severity,
            component = component,
            recommendation = generateRecommendation(component, severity, data),
            timeToMaintenance = calculateTimeToMaintenance(score),
            confidence = calculateConfidence(data.size),
            diagnosticData = collectDiagnosticData(component, data, diagnosticsState)
        )
    }

    private fun createNormalRecommendation() = MaintenanceRecommendation(
        severity = MaintenanceSeverity.NORMAL,
        component = VehicleComponent.ENGINE,
        recommendation = "All systems operating normally",
        timeToMaintenance = Duration.ofDays(30),
        confidence = 1.0f,
        diagnosticData = emptyMap()
    )

    // Simplified RPM analysis to avoid multiple passes
    private fun analyzeRPMInstant(): Double {
        // Implement simplified single-point RPM analysis
        return 0.0 // Replace with actual implementation
    }
}

fun <T : Number> List<T>.standardDeviation(): Double {
    if (this.isEmpty()) return 0.0

    // Convert to Double to ensure precise calculations
    val doubleList = this.map { it.toDouble() }

    // Calculate mean
    val mean = doubleList.average()

    // Calculate variance (average of squared differences from mean)
    val variance = doubleList.map { (it - mean) * (it - mean) }.average()

    // Return square root of variance
    return sqrt(variance)
}


object DrivingEfficiencyCalculator {
    private const val MAX_EFFICIENT_ACCELERATION = 3.0f
    private const val OPTIMAL_SPEED = 80.0f
    private const val MAX_SPEED_VARIATION = 20.0f
    private const val BRAKING_THRESHOLD = 5.0f
    private const val MAX_BRAKING_EVENTS = 10
    private const val DEFAULT_SCORE = 1.0f

    private val modeWeights = mapOf(
        DrivingMode.ECO to Triple(0.4f, 0.4f, 0.2f),
        DrivingMode.NORMAL to Triple(0.33f, 0.34f, 0.33f),
        DrivingMode.SPORT to Triple(0.3f, 0.3f, 0.4f),
        DrivingMode.PERFORMANCE to Triple(0.2f, 0.2f, 0.6f)
    )

    suspend fun calculate(vehicleData: List<VehicleEntity>, drivingMode: DrivingMode): Float =
        withContext(Dispatchers.Default) {
            try {
                if (vehicleData.isEmpty()) {
                    return@withContext DEFAULT_SCORE
                }

                val stats = calculateStats(vehicleData)

                val accelerationScore = calculateAccelerationScore(stats.avgAcceleration, stats.maxAcceleration)
                val speedScore = calculateSpeedScore(stats.avgSpeed, stats.speedVariance)
                val brakingScore = calculateBrakingScore(stats.brakingEvents)

                val weights = modeWeights[drivingMode] ?: modeWeights[DrivingMode.NORMAL]!!
                weightedAverage(
                    accelerationScore to weights.first,
                    speedScore to weights.second,
                    brakingScore to weights.third
                )
            } catch (e: Exception) {
                // Log error if needed
                DEFAULT_SCORE
            }
        }

    private data class VehicleStats(
        val avgAcceleration: Float,
        val maxAcceleration: Float,
        val avgSpeed: Float,
        val speedVariance: Float,
        val brakingEvents: Int
    ) {
        companion object {
            val EMPTY = VehicleStats(
                avgAcceleration = 0f,
                maxAcceleration = 0f,
                avgSpeed = 0f,
                speedVariance = 0f,
                brakingEvents = 0
            )
        }
    }

    private suspend fun calculateStats(data: List<VehicleEntity>): VehicleStats =
        withContext(Dispatchers.Default) {
            if (data.isEmpty()) {
                return@withContext VehicleStats.EMPTY
            }

            try {
                var sumAcceleration = 0.0f
                var maxAcceleration = Float.MIN_VALUE
                var sumSpeed = 0.0f
                var sumSpeedSquared = 0.0f
                var brakingEvents = 0
                val n = data.size.toFloat()

                // Process first element safely
                data.firstOrNull()?.let { first ->
                    sumAcceleration += first.acceleration
                    maxAcceleration = first.acceleration
                    sumSpeed += first.speed
                    sumSpeedSquared += first.speed * first.speed
                }

                // Process remaining elements
                var prevSpeed = data.firstOrNull()?.speed ?: 0f

                data.drop(1).forEach { entity ->
                    sumAcceleration += entity.acceleration
                    maxAcceleration = maxOf(maxAcceleration, entity.acceleration)

                    val speed = entity.speed
                    sumSpeed += speed
                    sumSpeedSquared += speed * speed

                    if (prevSpeed - speed > BRAKING_THRESHOLD) {
                        brakingEvents++
                    }
                    prevSpeed = speed
                }

                val avgSpeed = if (n > 0) sumSpeed / n else 0f
                val speedVariance = if (n > 0) (sumSpeedSquared / n) - (avgSpeed * avgSpeed) else 0f

                VehicleStats(
                    avgAcceleration = if (n > 0) sumAcceleration / n else 0f,
                    maxAcceleration = maxAcceleration,
                    avgSpeed = avgSpeed,
                    speedVariance = speedVariance,
                    brakingEvents = brakingEvents
                )
            } catch (e: Exception) {
                // Log error if needed
                VehicleStats.EMPTY
            }
        }

    private fun calculateAccelerationScore(avgAcceleration: Float, maxAcceleration: Float): Float =
        (1 - ((avgAcceleration + maxAcceleration) / (2 * MAX_EFFICIENT_ACCELERATION)))
            .coerceIn(0f, 1f)

    private fun calculateSpeedScore(avgSpeed: Float, speedVariance: Float): Float =
        (1 - (avgSpeed / OPTIMAL_SPEED) - (sqrt(speedVariance.toDouble()).toFloat() / MAX_SPEED_VARIATION))
            .coerceIn(0f, 1f)

    private fun calculateBrakingScore(brakingEvents: Int): Float =
        (1 - (brakingEvents.toFloat() / MAX_BRAKING_EVENTS))
            .coerceIn(0f, 1f)

    private fun weightedAverage(vararg scores: Pair<Float, Float>): Float =
        scores.fold(0f) { acc, (score, weight) -> acc + score * weight }
            .coerceIn(0f, 1f)
}

private fun calculateTimeToMaintenance(score: Float): Duration {
    return when {
        score < 0.3f -> Duration.ofDays(1)
        score < 0.5f -> Duration.ofDays(7)
        score < 0.7f -> Duration.ofDays(14)
        score < 0.9f -> Duration.ofDays(30)
        else -> Duration.ofDays(90)
    }
}

private fun calculateConfidence(dataPoints: Int): Float {
    return (dataPoints / 100f).coerceIn(0f, 1f)
}

private fun generateRecommendation(
    component: VehicleComponent,
    severity: MaintenanceSeverity,
    data: List<VehicleData>
): String {
    return when (component) {
        VehicleComponent.ENGINE -> generateEngineRecommendation(severity, data)
        VehicleComponent.BATTERY -> generateBatteryRecommendation(severity, data)
        VehicleComponent.TIRES -> generateTiresRecommendation(severity, data)
        else -> "Maintenance required for ${component.name.lowercase().replace('_', ' ')}"
    }
}
private fun getHealthColor(health: Float): Color {
    return when {
        health >= 0.8f -> Color(0xFF4CAF50) // Green
        health >= 0.6f -> Color(0xFFFFC107) // Yellow
        health >= 0.4f -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
}

private fun getAlertColor(severity: AlertSeverity): Color {
    return when (severity) {
        AlertSeverity.CRITICAL -> Color(0xFFF44336) // Red
        AlertSeverity.HIGH -> Color(0xFFFF9800) // Orange
        AlertSeverity.MEDIUM -> Color(0xFFFFC107) // Yellow
        AlertSeverity.LOW -> Color(0xFF4CAF50) // Green
    }
}
private fun generateEngineRecommendation(
    severity: MaintenanceSeverity,
    data: List<VehicleData>
): String {
    val avgTemp = data.map { it.engineTemp }.average()
    val avgRpm = data.map { it.rpm }.average()

    return when (severity) {
        MaintenanceSeverity.URGENT -> "Immediate engine inspection required. " +
                "High operating temperature (${avgTemp.roundToInt()}°C) " +
                "and irregular RPM patterns detected."
        MaintenanceSeverity.HIGH -> "Schedule engine maintenance soon. " +
                "Engine temperature trending high."
        MaintenanceSeverity.MEDIUM -> "Consider engine check-up at next service. " +
                "Minor temperature fluctuations detected."
        else -> "Engine operating within normal parameters. " +
                "Regular maintenance recommended."
    }
}

private fun generateBatteryRecommendation(
    severity: MaintenanceSeverity,
    data: List<VehicleData>
): String {
    val avgBatteryLevel = data.map { it.batteryLevel }.average()

    return when (severity) {
        MaintenanceSeverity.URGENT -> "Battery replacement needed. " +
                "Significant capacity loss detected. " +
                "Average charge level: ${avgBatteryLevel.roundToInt()}%"
        MaintenanceSeverity.HIGH -> "Battery performance degrading. " +
                "Schedule replacement within next month."
        MaintenanceSeverity.MEDIUM -> "Battery showing signs of wear. " +
                "Monitor performance."
        else -> "Battery operating normally. " +
                "No action required."
    }
}

// Continuing from the previous generateTiresRecommendation function
private fun generateTiresRecommendation(
    severity: MaintenanceSeverity,
    data: List<VehicleData>
): String {
    val latest = data.lastOrNull() ?: return "Insufficient tire data"

    return when (severity) {
        MaintenanceSeverity.URGENT -> "Immediate tire inspection needed. " +
                "Pressure imbalance detected: FL=${latest.tirePressureFL}, " +
                "FR=${latest.tirePressureFR}, RL=${latest.tirePressureRL}, " +
                "RR=${latest.tirePressureRR}"
        MaintenanceSeverity.HIGH -> "Tire pressure irregularities detected. " +
                "Schedule tire service soon."
        MaintenanceSeverity.MEDIUM -> "Tire pressures showing slight variation. " +
                "Check and adjust at next service."
        else -> "Tire pressures within normal range. " +
                "Continue regular monitoring."
    }
}

private fun collectDiagnosticData(
    component: VehicleComponent,
    data: List<VehicleData>,
    diagnosticsState: DiagnosticsState
): Map<String, Any> {
    return when (component) {
        VehicleComponent.ENGINE -> collectEngineDiagnostics(data, diagnosticsState)
        VehicleComponent.BATTERY -> collectBatteryDiagnostics(data, diagnosticsState)
        VehicleComponent.TIRES -> collectTireDiagnostics(data, diagnosticsState)
        VehicleComponent.BRAKES -> collectBrakeDiagnostics(data)
        VehicleComponent.TRANSMISSION -> collectTransmissionDiagnostics(data)
        VehicleComponent.COOLING_SYSTEM -> collectCoolingSystemDiagnostics(data)
        VehicleComponent.TIRE_FL -> TODO()
        VehicleComponent.TIRE_FR -> TODO()
        VehicleComponent.TIRE_RL -> TODO()
        VehicleComponent.TIRE_RR -> TODO()
        VehicleComponent.OVERALL -> TODO()
    }
}

private fun collectEngineDiagnostics(
    data: List<VehicleData>,
    diagnosticsState: DiagnosticsState
): Map<String, Any> {
    if (data.isEmpty()) return emptyMap()

    val temperatures = data.map { it.engineTemp }
    val rpms = data.map { it.rpm }

    return mapOf(
        "engineHealth" to diagnosticsState.engineHealth,
        "averageTemperature" to temperatures.average(),
        "maxTemperature" to (temperatures.maxOrNull() ?: 0),
        "temperatureVariation" to temperatures.standardDeviation(),
        "averageRPM" to rpms.average(),
        "maxRPM" to (rpms.maxOrNull() ?: 0),
        "rpmVariation" to rpms.standardDeviation(),
        "dataPoints" to data.size,
        "lastChecked" to diagnosticsState.lastDiagnosticsRun
    )
}

private fun collectBatteryDiagnostics(
    data: List<VehicleData>,
    diagnosticsState: DiagnosticsState
): Map<String, Any> {
    if (data.isEmpty()) return emptyMap()

    val batteryLevels = data.map { it.batteryLevel }
    val dischargeCurve = calculateDischargeCurve(batteryLevels)

    return mapOf(
        "batteryHealth" to diagnosticsState.batteryHealth,
        "averageLevel" to batteryLevels.average(),
        "minLevel" to (batteryLevels.minOrNull() ?: 0),
        "maxLevel" to (batteryLevels.maxOrNull() ?: 0),
        "dischargeCurve" to dischargeCurve,
        "dischargeRate" to calculateDischargeRate(batteryLevels),
        "cycleCount" to estimateBatteryCycles(data),
        "dataPoints" to data.size,
        "lastChecked" to diagnosticsState.lastDiagnosticsRun
    )
}

private fun calculateDischargeCurve(batteryLevels: List<Int>): List<Int> {
    // If there are not enough points to calculate a curve, return an empty list
    if (batteryLevels.size < 2) return emptyList()

    // Calculate the differences between consecutive battery levels
    return batteryLevels.zipWithNext { a, b -> a - b }
}

private fun collectTireDiagnostics(
    data: List<VehicleData>,
    diagnosticsState: DiagnosticsState
): Map<String, Any> {
    if (data.isEmpty()) return emptyMap()

    val latest = data.last()
    return mapOf(
        "tireHealth" to diagnosticsState.tireHealth,
        "currentPressures" to mapOf(
            "frontLeft" to latest.tirePressureFL,
            "frontRight" to latest.tirePressureFR,
            "rearLeft" to latest.tirePressureRL,
            "rearRight" to latest.tirePressureRR
        ),
        "pressureVariation" to calculatePressureVariation(data),
        "wearPattern" to analyzeTireWearPattern(data),
        "dataPoints" to data.size,
        "lastChecked" to diagnosticsState.lastDiagnosticsRun
    )
}

private fun collectBrakeDiagnostics(data: List<VehicleData>): Map<String, Any> {
    if (data.isEmpty()) return emptyMap()

    return mapOf(
        "brakingEvents" to countBrakingEvents(data),
        "averageBrakingForce" to calculateAverageBrakingForce(data),
        "hardBrakingCount" to countHardBrakingEvents(data),
        "brakingPattern" to analyzeBrakingPattern(data),
        "dataPoints" to data.size
    )
}

private fun collectTransmissionDiagnostics(data: List<VehicleData>): Map<String, Any> {
    if (data.isEmpty()) return emptyMap()

    return mapOf(
        "gearChanges" to countGearChanges(data),
        "gearDistribution" to analyzeGearDistribution(data),
        "shiftPattern" to analyzeShiftPattern(data),
        "dataPoints" to data.size
    )
}

private fun collectCoolingSystemDiagnostics(data: List<VehicleData>): Map<String, Any> {
    if (data.isEmpty()) return emptyMap()

    val temperatures = data.map { it.engineTemp }
    return mapOf(
        "averageTemperature" to temperatures.average(),
        "maxTemperature" to (temperatures.maxOrNull() ?: 0),
        "temperatureVariation" to temperatures.standardDeviation(),
        "coolingEfficiency" to calculateCoolingEfficiency(data),
        "dataPoints" to data.size
    )
}

// Helper functions for diagnostics calculations
private fun calculateDischargeRate(batteryLevels: List<Int>): Float {
    if (batteryLevels.size < 2) return 0f
    return batteryLevels.zipWithNext { a, b -> (a - b).toFloat() }.average().toFloat()
}

private fun estimateBatteryCycles(data: List<VehicleData>): Int {
    val chargeCycles = data.zipWithNext()
        .count { (a, b) -> a.batteryLevel < b.batteryLevel }
    return chargeCycles
}

private fun calculatePressureVariation(data: List<VehicleData>): Map<String, Double> {
    return mapOf(
        "frontLeft" to data.map { it.tirePressureFL }.standardDeviation(),
        "frontRight" to data.map { it.tirePressureFR }.standardDeviation(),
        "rearLeft" to data.map { it.tirePressureRL }.standardDeviation(),
        "rearRight" to data.map { it.tirePressureRR }.standardDeviation()
    )
}
private fun formatTimestamp(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    return LocalDateTime.ofInstant(
        Instant.ofEpochMilli(timestamp),
        ZoneId.systemDefault()
    ).format(formatter)
}

private fun analyzeTireWearPattern(data: List<VehicleData>): Map<String, Double> {
    // Simplified wear pattern analysis based on pressure variations
    val pressureVariations = calculatePressureVariation(data)
    return pressureVariations.mapValues { (_, variation) ->
        1f - (variation / 0.5f).coerceIn(0.0, 1.0)
    }
}

private fun countBrakingEvents(data: List<VehicleData>): Int {
    return data.zipWithNext()
        .count { (a, b) -> (a.speed - b.speed) > BRAKING_THRESHOLD }
}

private fun calculateAverageBrakingForce(data: List<VehicleData>): Float {
    return data.zipWithNext()
        .map { (a, b) -> (a.speed - b.speed).coerceAtLeast(0f) }
        .average()
        .toFloat()
}

private fun countHardBrakingEvents(data: List<VehicleData>): Int {
    return data.zipWithNext()
        .count { (a, b) -> (a.speed - b.speed) > HARD_BRAKING_THRESHOLD }
}

private fun analyzeBrakingPattern(data: List<VehicleData>): Map<String, Any> {
    val brakingForces = data.zipWithNext()
        .map { (a, b) -> (a.speed - b.speed).coerceAtLeast(0f) }

    return mapOf(
        "averageForce" to brakingForces.average(),
        "maxForce" to (brakingForces.maxOrNull() ?: 0f),
        "distribution" to analyzeBrakingDistribution(brakingForces)
    )
}

private fun countGearChanges(data: List<VehicleData>): Int {
    return data.zipWithNext()
        .count { (a, b) -> a.gear != b.gear }
}

private fun analyzeGearDistribution(data: List<VehicleData>): Map<String, Int> {
    return data.groupingBy { it.gear }
        .eachCount()
}

private fun analyzeShiftPattern(data: List<VehicleData>): Map<String, Int> {
    return data.zipWithNext()
        .filter { (a, b) -> a.gear != b.gear }
        .groupBy { (a, b) -> "${a.gear}->${b.gear}" }
        .mapValues { it.value.size }
}

private fun calculateCoolingEfficiency(data: List<VehicleData>): Float {
    if (data.isEmpty()) return 1f

    val temperatures = data.map { it.engineTemp }
    val maxTemp = temperatures.maxOrNull() ?: return 1f
    val avgTemp = temperatures.average().toFloat()

    return (1 - (avgTemp - OPTIMAL_ENGINE_TEMP) / (maxTemp - OPTIMAL_ENGINE_TEMP))
        .coerceIn(0f, 1f)
}

private fun analyzeBrakingDistribution(brakingForces: List<Float>): Map<String, Int> {
    return brakingForces.groupBy { force ->
        when {
            force > HARD_BRAKING_THRESHOLD -> "hard"
            force > BRAKING_THRESHOLD -> "medium"
            force > 0 -> "light"
            else -> "none"
        }
    }.mapValues { it.value.size }
}
private fun DrawScope.drawGridLines(width: Float, height: Float) {
    val gridColor = Color.White.copy(alpha = 0.1f)
    val verticalLines = 5
    val horizontalLines = 4

    // Vertical grid lines
    for (i in 0..verticalLines) {
        val x = (width * i) / verticalLines
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1.dp.toPx()
        )
    }

    // Horizontal grid lines
    for (i in 0..horizontalLines) {
        val y = (height * i) / horizontalLines
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.dp.toPx()
        )
    }
}
fun VehicleAnalytics.toVehicleStatistics() = VehicleStatistics(
    average = avgEngineTemp.toFloat(),  // Used for ENGINE_TEMP metric
    max = maxSpeed,  // Used for ENGINE_TEMP metric
    min = minBatteryLevel.toFloat(),  // Used for ROAD_CONDITION metric
    averageSpeed = avgSpeed.toFloat(),
    maxSpeed = maxSpeed,
    averageRPM = avgRpm.toFloat(),
    maxRPM = maxRpm,
    averageAcceleration = avgAcceleration.toFloat(),
    maxAcceleration = maxAcceleration,
    averageBatteryDrain = minBatteryLevel.toFloat(), // Using minBatteryLevel as a proxy
    efficiency = avgRoadCondition.toFloat() // Using roadCondition as a proxy for efficiency
)

fun VehicleEntity.toVehicleData() = VehicleData(
    timestamp = timestamp,
    speed = speed,
    rpm = rpm,
    gear = gear,
    range = range,
    batteryLevel = batteryLevel,
    engineTemp = engineTemp,
    outsideTemp = outsideTemp,
    tirePressureFL = tirePressureFL,
    tirePressureFR = tirePressureFR,
    tirePressureRL = tirePressureRL,
    tirePressureRR = tirePressureRR,
    acceleration = acceleration,
    accelerationForce = accelerationForce,
    roadCondition = roadCondition,
    tireWear = tireWear,
    latitude = latitude,
    longitude = longitude,
    altitude = altitude
)
fun VehicleData.toVehicleEntity() = VehicleEntity(
    timestamp = timestamp,
    speed = speed,
    rpm = rpm,
    gear = gear,
    range = range,
    batteryLevel = batteryLevel,
    engineTemp = engineTemp,
    outsideTemp = outsideTemp,
    tirePressureFL = tirePressureFL,
    tirePressureFR = tirePressureFR,
    tirePressureRL = tirePressureRL,
    tirePressureRR = tirePressureRR,
    acceleration = acceleration,
    accelerationForce = accelerationForce,
    roadCondition = roadCondition,
    tireWear = tireWear,
    // Handle nullable location fields from VehicleData
    latitude = latitude ?: 0.0,  // Provide default value for null
    longitude = longitude ?: 0.0,
    altitude = altitude ?: 0.0
)

private fun DrawScope.drawSpeedLine(
    data: List<VehicleData>,
    maxSpeed: Float,
    width: Float,
    height: Float
) {
    val path = Path()
    val points = data.mapIndexed { index, vehicleData ->
        Offset(
            x = (index.toFloat() / (data.size - 1)) * width,
            y = height - (vehicleData.speed / maxSpeed) * height
        )
    }

    // Draw line
    points.forEachIndexed { index, offset ->
        if (index == 0) {
            path.moveTo(offset.x, offset.y)
        } else {
            path.lineTo(offset.x, offset.y)
        }
    }

    drawPath(
        path = path,
        color = Color(0xFF00E5FF),
        style = Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Draw points
    points.forEach { offset ->
        drawCircle(
            color = Color(0xFF00E5FF),
            radius = 3.dp.toPx(),
            center = offset
        )
    }
}

class VehicleViewModelFactory(
    private val repository: FirebaseVehicleRepository,
    private val diagnosticsManager: DiagnosticsManager,
    private val analyticsCalculator: AnalyticsCalculator
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VehicleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VehicleViewModel(
                repository,
                diagnosticsManager,
                analyticsCalculator = analyticsCalculator
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


object VehiclePhysics {
    const val ACCELERATION_RAMP_RATE = 0.2f // How quickly acceleration builds up
    const val BRAKE_RAMP_RATE = 0.3f // How quickly braking builds up
    const val DECELERATION_RATE = 0.1f // Nat
    const val MAX_ACCELERATION = 3.0f // m/s²
    const val MAX_BRAKING = -5.0f // m/s²
    const val IDLE_RPM = 800f
    const val MAX_RPM = 6500f
    const val MAX_SPEED = 200f // km/h
    const val DRAG_COEFFICIENT = 0.02f
    const val ROLLING_RESISTANCE = 0.015f
    const val TIRE_WEAR_RATE = 0.00001f
    const val BATTERY_EFFICIENCY = 0.95f

    // Enhanced gear ratios with more realistic values
    val GEAR_RATIOS = mapOf(
        "1" to 3.5f,
        "2" to 2.5f,
        "3" to 1.8f,
        "4" to 1.4f,
        "5" to 1.0f,
        "6" to 0.8f,
        "R" to -3.2f,
        "N" to 0f,
        "P" to 0f
    )

    fun calculateNextGear(currentGear: Int, speed: Float, rpm: Float): Int {
        return when {
            rpm > 5500 && speed < 180 ->
                if (currentGear + 1 > 6) 6 else currentGear + 1
            rpm < 1500 && speed > 10 ->
                if (currentGear - 1 < 1) 1 else currentGear - 1
            speed < 20 -> 1
            speed < 40 -> 2
            speed < 70 -> 3
            speed < 100 -> 4
            speed < 140 -> 5
            else -> 6
        }
    }
    fun calculateRPM(speed: Float, gear: String, accelerating: Boolean): Float {
        val gearRatio = GEAR_RATIOS[gear] ?: 1.0f
        if (gear in listOf("P", "N")) return if (accelerating) IDLE_RPM * 1.2f else IDLE_RPM

        val baseRPM = (speed * 60) * gearRatio
        val loadFactor = if (accelerating) 1.2f else 1.0f
        return ((baseRPM + IDLE_RPM) * loadFactor).coerceIn(IDLE_RPM, MAX_RPM)
    }

    fun calculateAcceleration(
        speed: Float,
        accelerating: Boolean,
        braking: Boolean,
        roadCondition: Float,
        tireWear: Float
    ): Float {
        val tractionFactor = roadCondition * (1f - (1f - tireWear) * 0.3f)

        return when {
            braking -> MAX_BRAKING * tractionFactor
            accelerating -> {
                val speedFactor = 1f - (speed / MAX_SPEED)
                MAX_ACCELERATION * speedFactor * tractionFactor
            }
            else -> {
                val dragForce = -DRAG_COEFFICIENT * speed * speed
                val rollingResistance = -ROLLING_RESISTANCE * speed
                (dragForce + rollingResistance) * tractionFactor
            }
        }
    }
}
private fun DrawScope.drawAnimatedLine(
    data: List<Float>,
    maxValue: Float,
    minValue: Float,
    width: Float,
    height: Float,
    progress: Float,
    color: Color
) {
    if (data.isEmpty()) return

    val path = Path()
    val range = (maxValue - minValue).coerceAtLeast(0.1f)
    val animatedPoints = (data.indices.take((data.size * progress).toInt() + 1))
        .map { index ->
            Offset(
                x = (index.toFloat() / (data.size - 1)) * width,
                y = height - ((data[index] - minValue) / range) * height
            )
        }

    // Draw line
    animatedPoints.forEachIndexed { index, offset ->
        if (index == 0) {
            path.moveTo(offset.x, offset.y)
        } else {
            path.lineTo(offset.x, offset.y)
        }
    }

    // Draw the line with a gradient
    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(
                color,
                color.copy(alpha = 0.5f)
            )
        ),
        style = Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Draw points with animation
    animatedPoints.forEach { offset ->
        drawCircle(
            color = color,
            radius = 3.dp.toPx(),
            center = offset
        )
    }
}

// New function to calculate efficiency
fun calculateEfficiency(data: List<VehicleData>): Float {
    if (data.size < 2) return 0f

    val totalDistance = data.zipWithNext { a, b ->
        (b.speed + a.speed) / 2 * (b.timestamp - a.timestamp) / 3600000f
    }.sum()

    val batteryUsed = data.first().batteryLevel - data.last().batteryLevel

    return if (batteryUsed > 0) totalDistance / batteryUsed else 0f
}
data class VehicleStatistics(
    val average: Float,
    val max: Float,
    val min: Float,
    val averageSpeed: Float,
    val maxSpeed: Float,
    val averageRPM: Float,
    val maxRPM: Float,
    val averageAcceleration: Float,
    val maxAcceleration: Float,
    val averageBatteryDrain: Float,
    val efficiency: Float
)
enum class VehicleMetric(val displayName: String) {
    SPEED("Speed"),
    RPM("RPM"),
    BATTERY("Battery"),
    ENGINE_TEMP("Engine Temp"),
    ACCELERATION("Acceleration"),
    ROAD_CONDITION("Road Condition")
}

// Create a sealed class for UI state management
sealed class VehicleUiState {
    data object Loading : VehicleUiState()
    data class Success(val data: VehicleData) : VehicleUiState()
    data class Error(val message: String) : VehicleUiState()
}

// Create events sealed class for user interactions
sealed class VehicleEvent {
    data object StartAccelerating : VehicleEvent()
    data object StopAccelerating : VehicleEvent()
    data object StopBraking : VehicleEvent()
    data class UpdateTimeFrame(val timeFrame: TimeFrame) : VehicleEvent()
    data class UpdateMetric(val metric: VehicleMetric) : VehicleEvent()
}

class FirebaseVehicleException(
    message: String,
    cause: Throwable? = null,
    val code: ErrorCode = ErrorCode.UNKNOWN
) : Exception(message, cause) {
    enum class ErrorCode {
        NETWORK_ERROR,
        AUTHENTICATION_REQUIRED,
        RATE_LIMIT_EXCEEDED,
        UNKNOWN
    }
}

// Main repository class
class FirebaseVehicleRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val dispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO
) {
    private val vehicleCollection = firestore.collection("vehicle_data")

    suspend fun insertVehicleData(data: VehicleData) = withContext(dispatcher) {
        try {
            vehicleCollection.document(data.timestamp.toString())
                .set(data)
                .await()
        } catch (e: FirebaseFirestoreException) {
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    throw FirebaseVehicleException(
                        "Authentication required",
                        e,
                        FirebaseVehicleException.ErrorCode.AUTHENTICATION_REQUIRED
                    )
                FirebaseFirestoreException.Code.UNAVAILABLE ->
                    throw FirebaseVehicleException(
                        "Network connection unavailable",
                        e,
                        FirebaseVehicleException.ErrorCode.NETWORK_ERROR
                    )
                else -> throw FirebaseVehicleException(
                    "Failed to insert vehicle data: ${e.message}",
                    e
                )
            }
        }
    }

    fun getVehicleDataStream(since: Long): Flow<List<VehicleEntity>> = callbackFlow {
        // Start with empty list
        trySend(emptyList())  // Changed from send() to trySend()

        // Set up real-time listener
        val listener = vehicleCollection
            .whereGreaterThanOrEqualTo("timestamp", since)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(FirebaseVehicleException(
                        "Real-time updates failed",
                        error
                    ))
                    return@addSnapshotListener
                }

                snapshot?.let { validSnapshot ->
                    val vehicleData = validSnapshot.documents.mapNotNull { doc ->
                        doc.toObject(VehicleData::class.java)?.toVehicleEntity()
                    }
                    trySend(vehicleData)  // Changed from send() to trySend()
                }
            }

        // Clean up listener when Flow collection is cancelled
        awaitClose { listener.remove() }
    }
        .flowOn(dispatcher)
        .catch { e -> throw handleFirestoreException(e) }
        .retryWhen { cause, attempt ->
            // Retry on network issues with exponential backoff
            if (cause is FirebaseFirestoreException &&
                cause.code == FirebaseFirestoreException.Code.UNAVAILABLE &&
                attempt < 3
            ) {
                delay(2000L * (attempt + 1))
                true
            } else false
        }

    fun getVehicleAnalytics(since: Long): Flow<VehicleAnalytics> = flow {
        getVehicleDataStream(since)
            .catch { e -> throw handleFirestoreException(e) }
            .collect { entities ->
                val analytics = calculateAnalytics(entities)
                emit(analytics)
            }
    }.flowOn(dispatcher)

    fun getHourlyAnalytics(since: Long): Flow<List<HourlyAnalytics>> = flow {
        try {
            val snapshot = vehicleCollection
                .whereGreaterThanOrEqualTo("timestamp", since)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()

            val vehicleData = snapshot.documents.mapNotNull { doc ->
                doc.toObject(VehicleData::class.java)
            }

            val hourlyStats = processHourlyAnalytics(vehicleData)
            emit(hourlyStats)
        } catch (e: Exception) {
            throw handleFirestoreException(e)
        }
    }.flowOn(dispatcher)

    private fun handleFirestoreException(e: Throwable): FirebaseVehicleException {
        return when (e) {
            is FirebaseFirestoreException -> when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    FirebaseVehicleException(
                        "Authentication required",
                        e,
                        FirebaseVehicleException.ErrorCode.AUTHENTICATION_REQUIRED
                    )

                FirebaseFirestoreException.Code.UNAVAILABLE ->
                    FirebaseVehicleException(
                        "Network connection unavailable",
                        e,
                        FirebaseVehicleException.ErrorCode.NETWORK_ERROR
                    )

                FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED ->
                    FirebaseVehicleException(
                        "Rate limit exceeded",
                        e,
                        FirebaseVehicleException.ErrorCode.RATE_LIMIT_EXCEEDED
                    )

                else -> FirebaseVehicleException(
                    "Firestore error: ${e.message}",
                    e
                )
            }

            is FirebaseVehicleException -> e
            else -> FirebaseVehicleException(
                "Repository error: ${e.message}",
                e
            )
        }
    }

    private fun calculateAnalytics(entities: List<VehicleEntity>): VehicleAnalytics {
        if (entities.isEmpty()) return VehicleAnalytics()

        return VehicleAnalytics(
            avgSpeed = entities.map { it.speed }.average(),
            maxSpeed = entities.maxOf { it.speed },
            avgRpm = entities.map { it.rpm }.average(),
            maxRpm = entities.maxOf { it.rpm },
            avgEngineTemp = entities.map { it.engineTemp }.average(),
            minBatteryLevel = entities.minOf { it.batteryLevel },
            avgAcceleration = entities.map { it.acceleration }.average(),
            maxAcceleration = entities.maxOf { abs(it.acceleration) },
            avgRoadCondition = entities.map { it.roadCondition }.average()
        )
    }

    private fun processHourlyAnalytics(data: List<VehicleData>): List<HourlyAnalytics> {
        return data.groupBy {
            Instant.ofEpochMilli(it.timestamp)
                .atZone(ZoneId.systemDefault())
                .hour
                .toString()
        }.map { (hour, hourData) ->
            HourlyAnalytics(
                hour = hour,
                avgSpeed = hourData.map { it.speed }.average().toFloat(),
                avgBatteryLevel = hourData.map { it.batteryLevel }.average().toFloat()
            )
        }.sortedBy { it.hour }
    }

    fun insertVehicleDataBatch(batch: List<VehicleData>) {

    }
}

// Extension function for List<Number>
fun List<Number>.average(): Double {
    return if (isEmpty()) 0.0 else sumOf { it.toDouble() } / size
}


// Add missing TimeFrame enum
enum class TimeFrame(val duration: Long, val label: String) {
    LAST_HOUR(3600000, "1H"),
    LAST_DAY(86400000, "24H"),
    LAST_WEEK(604800000, "1W"),
    LAST_MONTH(2592000000, "1M")
}

// Add missing VehicleData class

data class VehicleState(
    val diagnosticsState: DiagnosticsState = DiagnosticsState(),
    val drivingMode: DrivingMode = DrivingMode.NORMAL,
    var speed: Float = 0f,
    var rpm: Float = VehiclePhysics.IDLE_RPM,
    var gear: Int = 1,
    var acceleration: Float = 0f,
    var engineTemperature: Float = DEFAULT_TEMPERATURE,
    var batteryCharge: Float = 100f,
    var tireWear: Float = 1f,
    var roadCondition: Float = 1f,
    var accelerating: Boolean = false,
    var braking: Boolean = false,
    var range: Int = 500
) {
    fun update(deltaTime: Float) {
        // Update speed based on acceleration
        speed = (speed + acceleration * deltaTime)
            .coerceIn(0f, VehiclePhysics.MAX_SPEED)

        // Update RPM based on gear and speed
        rpm = calculateRPM(speed, gear.toString(), accelerating)  // Fixed: Removed toString()

        // Update engine temperature based on load
        val rpmLoad = (rpm - VehiclePhysics.IDLE_RPM) / (VehiclePhysics.MAX_RPM - VehiclePhysics.IDLE_RPM)
        val speedLoad = speed / VehiclePhysics.MAX_SPEED
        val totalLoad = (rpmLoad + speedLoad) / 2f

        engineTemperature += totalLoad * 5f * deltaTime
        engineTemperature -= (engineTemperature - DEFAULT_TEMPERATURE) * 0.1f * deltaTime
        engineTemperature = engineTemperature.coerceIn(MIN_TEMPERATURE, MAX_TEMPERATURE)

        // Update tire wear
        tireWear -= VehiclePhysics.TIRE_WEAR_RATE *
                speed * deltaTime *
                (if (braking) 2f else 1f)
        tireWear = tireWear.coerceIn(0.5f, 1f)

        // Update battery charge and range
        val powerDraw = (speed * 0.001f +
                acceleration * 0.0005f +
                rpm * 0.0001f) * deltaTime
        batteryCharge -= powerDraw / VehiclePhysics.BATTERY_EFFICIENCY
        batteryCharge = batteryCharge.coerceIn(0f, 100f)

        // Update range based on battery charge
        range = (batteryCharge * 5).toInt()
    }

    companion object {
        const val DEFAULT_TEMPERATURE = 85f
        const val MAX_TEMPERATURE = 105f
        const val MIN_TEMPERATURE = 85f
    }
}


// Centralized analytics calculation interface


// Implementation of analytics calculator
// Update the interface
interface AnalyticsCalculator {
    suspend fun calculateEfficiencyScore(data: List<VehicleEntity>, mode: DrivingMode): Float
    suspend fun analyzeMaintenance(
        data: List<VehicleData>,
        diagnostics: DiagnosticsState
    ): MaintenanceRecommendation
}

// Update the implementation
class VehicleAnalyticsCalculator : AnalyticsCalculator {
    override suspend fun calculateEfficiencyScore(data: List<VehicleEntity>, mode: DrivingMode): Float {
        return DrivingEfficiencyCalculator.calculate(data, mode)
    }

    override suspend fun analyzeMaintenance(
        data: List<VehicleData>,
        diagnostics: DiagnosticsState
    ): MaintenanceRecommendation {
        return MaintenanceAnalyzer.analyze(data, diagnostics)
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var vehicleViewModel: VehicleViewModel
    private val vehicleState = VehicleState()
    // Use a more structured coroutine scope with a SupervisorJob to handle errors better
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val random = Random(System.currentTimeMillis())
    private val physicsCalculator = PhysicsCalculator()
    private val dataGenerator = VehicleDataGenerator()

    // Increase buffer size to prevent blocking
    private val dataUpdateBuffer = Channel<VehicleData>(Channel.BUFFERED)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeComponents()
        setupUI()
        startDataSimulation()
    }

    private fun initializeComponents() {
        val firestore = Firebase.firestore
        val repository = FirebaseVehicleRepository(firestore)
        val diagnosticsManager = DiagnosticsManager(
            // Use the activity's scope instead of creating a new one
            scope = scope.plus(Dispatchers.IO),
            repository = repository
        )
        val analyticsCalculator = VehicleAnalyticsCalculator()

        vehicleViewModel = ViewModelProvider(
            this,
            VehicleViewModelFactory(repository, diagnosticsManager, analyticsCalculator)
        )[VehicleViewModel::class.java]
    }

    private fun setupUI() {
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VehicleApp(viewModel = vehicleViewModel)
                }
            }
        }
    }

    // In MainActivity.kt
    private fun startDataSimulation() {
        scope.launch(Dispatchers.Default) {
            // Use a more efficient SharedFlow for high-frequency updates
            val dataUpdateFlow = MutableSharedFlow<VehicleData>(
                replay = 1,
                extraBufferCapacity = 64,
                onBufferOverflow = BufferOverflow.DROP_OLDEST
            )

            // Separate data processing pipeline with optimized collection
            launch {
                dataUpdateFlow
                    // Sample at a fixed rate to reduce processing frequency
                    .sample(16.milliseconds)
                    // Process on background thread
                    .flowOn(Dispatchers.Default)
                    // Collect updates less frequently
                    .collect { data ->
                        // Switch to Main thread only when updating UI state
                        withContext(Dispatchers.Main) {
                            vehicleViewModel.updateVehicleState(data)
                        }
                    }
            }

            // Main simulation loop with more efficient timing
            launch {
                val frameTime = 50.milliseconds
                var lastUpdateTime = System.currentTimeMillis()
                var accumulatedTime = 0f

                while (isActive) {
                    try {
                        val currentTime = System.currentTimeMillis()
                        val deltaTime = (currentTime - lastUpdateTime) / 1000f
                        accumulatedTime += deltaTime
                        lastUpdateTime = currentTime

                        // Fixed update rate
                        if (accumulatedTime >= 0.033f) {
                            val newData = withContext(Dispatchers.Default) {
                                physicsCalculator.updateVehicleState(vehicleState, accumulatedTime, random)
                                dataGenerator.generateVehicleData(vehicleState, currentTime)
                            }

                            // Non-blocking emission with drop policy
                            dataUpdateFlow.tryEmit(newData)
                            accumulatedTime = 0f
                        }

                        // More consistent frame timing using delay
                        val processingTime = System.currentTimeMillis() - currentTime
                        val remainingTime = frameTime.inWholeMilliseconds - processingTime
                        if (remainingTime > 0) delay(remainingTime)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Simulation error", e)
                        delay(1000)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}

class VehicleViewModel(
    val repository: FirebaseVehicleRepository,
    private val diagnosticsManager: DiagnosticsManager,
    private val analyticsCalculator: AnalyticsCalculator,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    // State holders with increased buffer capacity
    private val _vehicleState = MutableStateFlow(VehicleState())
    val vehicleState = _vehicleState.asStateFlow()

    private val _analyticsState = MutableStateFlow<AnalyticsState>(AnalyticsState.Loading)
    val analyticsState = _analyticsState.asStateFlow()

    private val _vehicleMetrics = MutableStateFlow<VehicleMetrics?>(null)
    val vehicleMetrics = _vehicleMetrics.asStateFlow()

    private val _diagnosticsAlerts = MutableStateFlow<List<DiagnosticAlert>>(emptyList())
    val diagnosticsAlerts = _diagnosticsAlerts.asStateFlow()

    private val _displaySettings = MutableStateFlow(DisplaySettings())
    val displaySettings = _displaySettings.asStateFlow()

    private val _unitSettings = MutableStateFlow(UnitSettings())
    val unitSettings = _unitSettings.asStateFlow()

    private val _vehiclePreferences = MutableStateFlow(VehiclePreferences())

    // Performance optimization variables
    private val physicsCache = LruCache<String, Float>(100)
    private val lastUpdateTime = AtomicLong(System.nanoTime())
    private val locationData = AtomicReference<Triple<Double, Double, Double>?>(null)

    private val vehicleDataBuffer = Channel<VehicleData>(Channel.BUFFERED) {
        BufferOverflow.DROP_OLDEST
    }
    private val locationBuffer = Channel<Triple<Double, Double, Double>>(Channel.BUFFERED) {
        BufferOverflow.DROP_OLDEST
    }
    private val vehicleDataFlow = MutableSharedFlow<VehicleData>(
        replay = 0,
        extraBufferCapacity = 100,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val _locationData = MutableStateFlow<Triple<Double, Double, Double>?>(null)

    // More efficient update rates with separate concerns
    private val uiUpdateRate = 100L // ms (10fps for UI updates)
    private val physicsUpdateRate = 33L // ms (30fps for physics)
    private val diagnosticsUpdateRate = 1000L // ms (1fps for diagnostics)

    // Use atomic references for thread-safe state reading
    private val lastVehicleUpdate = AtomicLong(0L)
    private val pendingUpdates = AtomicInteger(0)


    private val updateBatcher = BatchProcessor<VehicleData>(
        batchSize = 10,
        maxDelayMs = 100,
        scope = viewModelScope,
        dispatcher = dispatcher,
        processor = ::processBatchedVehicleData
    )

    private var diagnosticsJob: Job? = null

    // Update throttling with longer intervals
    private val updateThrottleTime = 50L // 20fps

    private var lastPhysicsUpdate = 0L
    private var lastDiagnosticsUpdate = 0L

    // Cache for expensive calculations
    private val engineHealthCache = ExpiringCache<VehicleState, Float>(
        maxSize = 50,
        expirationMs = 1000
    )
    init {
        initializeVehicleData()
        startDiagnostics()
        startPhysicsSimulation()
        startDataProcessor()
    }
    private fun startDataProcessor() {
        viewModelScope.launch(dispatcher) {
            // Use conflation to only process latest updates
            vehicleDataFlow
                // Add buffer but throttle collection
                .buffer(capacity = Channel.BUFFERED)
                // Only take the latest item from bursts of updates
                .conflate()
                // Batch process for efficiency
                .collect { data ->
                    processVehicleData(data)
                }
        }

        // Separate UI update flow at controlled rate
        viewModelScope.launch(dispatcher) {
            // Use fixed rate timer for UI updates
            while (isActive) {
                updateUiState()
                delay(uiUpdateRate)
            }
        }
    }
    private val pendingUpdatesList = Collections.synchronizedList(mutableListOf<VehicleData>())

    private fun collectPendingUpdates(): List<VehicleData> {
        val result = ArrayList(pendingUpdatesList)
        pendingUpdatesList.clear()
        return result
    }

    // Separated UI update mechanism to control recomposition rate
    private suspend fun updateUiState() {
        // Only update UI at controlled rate
        if (System.currentTimeMillis() - lastVehicleUpdate.get() >= uiUpdateRate) {
            // Emit new state to UI flow
            _vehicleState.emit(_vehicleState.value)
            lastVehicleUpdate.set(System.currentTimeMillis())
        }
    }
    private suspend fun processBatchedVehicleData(batch: List<VehicleData>) {
        withContext(dispatcher) {
            // Only process if batch is not empty
            if (batch.isNotEmpty()) {
                val latestData = batch.last()
                updateVehicleStateInternal(latestData)
                repository.insertVehicleDataBatch(batch)
            }
        }
    }
    private suspend fun processVehicleData(data: VehicleData) {
        // Update internal model without UI recomposition
        updateInternalState(data)

        // Track pending updates to avoid lost data
        pendingUpdates.incrementAndGet()

        // Log data to repository with batching
        if (pendingUpdates.get() > 10) {
            repository.insertVehicleDataBatch(collectPendingUpdates())
            pendingUpdates.set(0)
        }
    }

    fun updateVehicleState(data: VehicleData) {
        viewModelScope.launch(dispatcher) {
            // Non-blocking emission with overflow handling
            vehicleDataFlow.tryEmit(data)

            // Only process location when available (avoids allocations)
            if (data.latitude != null && data.longitude != null && data.altitude != null) {
                _locationData.value = Triple(data.latitude, data.longitude, data.altitude)
            }
        }
    }
    private suspend fun updateVehicleStateInternal(data: VehicleData) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastVehicleUpdate.get() >= 100) {
            val newState = _vehicleState.value.copy(
                speed = data.speed,
                rpm = data.rpm,
                gear = data.gear.toIntOrNull() ?: 1,
                range = data.range,
                batteryCharge = data.batteryLevel.toFloat(),
                engineTemperature = data.engineTemp.toFloat(),
                acceleration = data.acceleration,
                roadCondition = data.roadCondition,
                tireWear = data.tireWear
            )
            _vehicleState.emit(newState)
            lastVehicleUpdate.set(currentTime)
        }
    }
    private fun updateInternalState(data: VehicleData) {
        // Get current state once to avoid multiple reads
        val currentState = _vehicleState.value

        // Create new state with minimal property changes
        val newState = currentState.copy(
            speed = data.speed ?: currentState.speed,
            rpm = data.rpm ?: currentState.rpm,
            gear = data.gear?.toIntOrNull() ?: currentState.gear,
            range = data.range ?: currentState.range,
            batteryCharge = data.batteryLevel?.toFloat() ?: currentState.batteryCharge,
            engineTemperature = data.engineTemp?.toFloat() ?: currentState.engineTemperature,
            acceleration = data.acceleration ?: currentState.acceleration,
            roadCondition = data.roadCondition ?: currentState.roadCondition,
            tireWear = data.tireWear ?: currentState.tireWear
        )

        // Update internal state directly without emission
        _vehicleState.value = newState

        // Add to pending updates for batch repository sync
        pendingUpdatesList.add(data)
    }

    private fun startPhysicsSimulation() {
        viewModelScope.launch(dispatcher) {
            var previousTime = System.nanoTime()

            while (isActive) {
                val currentTime = System.nanoTime()
                val deltaTime = ((currentTime - previousTime) / 1_000_000_000f)
                    .coerceAtMost(0.1f)

                if (deltaTime >= 0.033f) {
                    updatePhysics(deltaTime)
                    syncWithRepository()
                    previousTime = currentTime
                }

                delay(16)
            }
        }
    }

    private suspend fun syncWithRepository() = withContext(dispatcher) {
        val currentState = _vehicleState.value
        val location = locationData.get()

        val vehicleData = VehicleData(
            timestamp = System.currentTimeMillis(),
            speed = currentState.speed,
            rpm = currentState.rpm,
            gear = currentState.gear.toString(),
            range = currentState.range,
            batteryLevel = currentState.batteryCharge.toInt(),
            engineTemp = currentState.engineTemperature.toInt(),
            acceleration = currentState.acceleration,
            accelerationForce = VehiclePhysics.calculateAcceleration(
                currentState.speed,
                currentState.accelerating,
                currentState.braking,
                currentState.roadCondition,
                currentState.tireWear
            ),
            roadCondition = currentState.roadCondition,
            tireWear = currentState.tireWear,
            latitude = location?.first,
            longitude = location?.second,
            altitude = location?.third
        )

        try {
            repository.insertVehicleData(vehicleData)
        } catch (e: Exception) {
            Log.e("VehicleViewModel", "Error syncing with repository", e)
        }
    }

    private suspend fun updatePhysics(deltaTime: Float) {
        val currentState = _vehicleState.value

        val cacheKey = "${currentState.speed}-${currentState.accelerating}-${currentState.braking}-${currentState.roadCondition}-${currentState.tireWear}"
        val newAcceleration = physicsCache.get(cacheKey) ?: run {
            VehiclePhysics.calculateAcceleration(
                speed = currentState.speed,
                accelerating = currentState.accelerating,
                braking = currentState.braking,
                roadCondition = currentState.roadCondition,
                tireWear = currentState.tireWear
            ).also { physicsCache.put(cacheKey, it) }
        }

        val nextGear = VehiclePhysics.calculateNextGear(
            currentState.gear,
            currentState.speed,
            currentState.rpm
        )

        val newRpm = VehiclePhysics.calculateRPM(
            currentState.speed,
            currentState.gear.toString(),
            currentState.accelerating
        )

        val updatedState = currentState.copy(
            acceleration = newAcceleration,
            gear = nextGear,
            rpm = newRpm
        ).apply { update(deltaTime) }

        _vehicleState.emit(updatedState)
    }

    private fun startDiagnostics() {
        diagnosticsJob?.cancel()
        diagnosticsJob = viewModelScope.launch(dispatcher) {
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastDiagnosticsUpdate >= 1000) {
                    val currentState = _vehicleState.value
                    runDiagnostics(currentState)
                    lastDiagnosticsUpdate = currentTime
                }
                delay(100)
            }
        }
    }

    private suspend fun runDiagnostics(currentState: VehicleState) {
        val alerts = mutableListOf<DiagnosticAlert>()

        // Engine temperature check
        if (currentState.engineTemperature > VehicleState.MAX_TEMPERATURE) {
            alerts.add(DiagnosticAlert(
                severity = AlertSeverity.HIGH,
                component = VehicleComponent.ENGINE,
                message = "Engine temperature critical: ${currentState.engineTemperature}°C"
            ))
        }

        // Battery charge check
        when {
            currentState.batteryCharge < 10f -> {
                alerts.add(DiagnosticAlert(
                    severity = AlertSeverity.CRITICAL,
                    component = VehicleComponent.BATTERY,
                    message = "Battery critically low: ${currentState.batteryCharge}%"
                ))
            }
            currentState.batteryCharge < 20f -> {
                alerts.add(DiagnosticAlert(
                    severity = AlertSeverity.HIGH,
                    component = VehicleComponent.BATTERY,
                    message = "Battery low: ${currentState.batteryCharge}%"
                ))
            }
        }

        val diagnosticsState = DiagnosticsState(
            engineHealth = calculateEngineHealth(currentState),
            batteryHealth = currentState.batteryCharge / 100f,
            tireHealth = createTireHealthMap(currentState.tireWear),
            transmissionHealth = calculateTransmissionHealth(currentState),
            overallPerformance = calculateOverallPerformance(currentState),
            lastDiagnosticsRun = System.currentTimeMillis(),
            alerts = alerts
        )

        withContext(Dispatchers.Main) {
            _diagnosticsAlerts.value = alerts
            _vehicleState.value = currentState.copy(diagnosticsState = diagnosticsState)
        }
    }

    fun updateDrivingMode(mode: DrivingMode) {
        viewModelScope.launch {
            _vehicleState.value = _vehicleState.value.copy(drivingMode = mode)
        }
    }

    fun updateDisplaySettings(settings: DisplaySettings) {
        viewModelScope.launch {
            _displaySettings.value = settings
        }
    }

    private fun createTireHealthMap(tireWear: Float) = mapOf(
        TirePosition.FRONT_LEFT to tireWear,
        TirePosition.FRONT_RIGHT to tireWear,
        TirePosition.REAR_LEFT to tireWear,
        TirePosition.REAR_RIGHT to tireWear
    )

    private fun calculateEngineHealth(state: VehicleState): Float {
        return engineHealthCache.get(state) ?: run {
            val tempFactor = 1f - ((state.engineTemperature - VehicleState.MIN_TEMPERATURE) /
                    (VehicleState.MAX_TEMPERATURE - VehicleState.MIN_TEMPERATURE))
            val rpmFactor = 1f - (state.rpm / VehiclePhysics.MAX_RPM)
            ((tempFactor + rpmFactor) / 2f).also { engineHealthCache.put(state, it) }
        }
    }

    private fun calculateTransmissionHealth(state: VehicleState): Float {
        val optimalGear = VehiclePhysics.calculateNextGear(state.gear, state.speed, state.rpm)
        return if (state.gear == optimalGear) 1.0f else 0.8f
    }

    private fun calculateOverallPerformance(state: VehicleState): Float {
        return (calculateEngineHealth(state) +
                (state.batteryCharge / 100f) +
                state.tireWear +
                calculateTransmissionHealth(state)) / 4f
    }

    private fun initializeVehicleData() {
        viewModelScope.launch {
            try {
                combine(
                    repository.getVehicleDataStream(TimeFrame.LAST_HOUR.duration)
                        .map { entities ->
                            entities.lastOrNull()?.toVehicleData()
                        },
                    repository.getVehicleAnalytics(TimeFrame.LAST_HOUR.duration)
                ) { latestData, analytics ->
                    Pair(latestData, analytics)
                }
                    .flowOn(dispatcher)
                    .collect { (latest, analytics) ->
                        // Add null check here
                        if (latest != null) {
                            _vehicleMetrics.value = VehicleMetrics(
                                currentData = latest,
                                analytics = analytics
                            )

                            _vehicleState.value = _vehicleState.value.copy(
                                speed = latest.speed ?: 0f,  // Add default values
                                rpm = latest.rpm ?: 0f,
                                gear = latest.gear?.toIntOrNull() ?: 1,
                                acceleration = latest.acceleration ?: 0f,
                                engineTemperature = latest.engineTemp?.toFloat() ?: 0f,
                                batteryCharge = latest.batteryLevel?.toFloat() ?: 0f,
                                tireWear = latest.tireWear ?: 1f,
                                roadCondition = latest.roadCondition ?: 1f,
                                range = latest.range ?: 0  // Changed from 0f to 0
                            )

                            _analyticsState.value = AnalyticsState.Success
                        } else {
                            // Handle case when no data is available
                            _analyticsState.value = AnalyticsState.Success  // or different state if needed
                            _vehicleState.value = _vehicleState.value.copy(
                                // Set default values
                                speed = 0f,
                                rpm = 0f,
                                gear = 1,
                                acceleration = 0f,
                                engineTemperature = 0f,
                                batteryCharge = 100f,
                                tireWear = 1f,
                                roadCondition = 1f,
                                range = 0
                            )
                        }
                    }
            } catch (e: Exception) {
                _analyticsState.value = AnalyticsState.Error(e.message ?: "Unknown error")
                _vehicleState.value = _vehicleState.value.copy(
                    diagnosticsState = DiagnosticsState(
                        engineHealth = 0f,
                        batteryHealth = 0f,
                        tireHealth = createTireHealthMap(0f),
                        transmissionHealth = 0f,
                        overallPerformance = 0f,
                        lastDiagnosticsRun = System.currentTimeMillis(),
                        alerts = listOf(DiagnosticAlert(
                            severity = AlertSeverity.HIGH,
                            component = VehicleComponent.ENGINE,
                            message = "Data initialization failed: ${e.message}"
                        ))
                    )
                )
            }
        }
    }
    // Resource cleanup
    override fun onCleared() {
        super.onCleared()
        diagnosticsJob?.cancel()
        engineHealthCache.clear()
        physicsCache.evictAll()
        updateBatcher.stop()
        viewModelScope.cancel()
    }

    // Inner class for batch processing
    private class BatchProcessor<T>(
        private val batchSize: Int,
        private val maxDelayMs: Long,
        private val scope: CoroutineScope,
        private val dispatcher: CoroutineDispatcher,
        private val processor: suspend (List<T>) -> Unit
    ) {
        private val buffer = mutableListOf<T>()
        private var lastProcessTime = 0L
        private var job: Job? = null

        fun add(item: T) {
            scope.launch(dispatcher) {
                buffer.add(item)
                if (buffer.size >= batchSize ||
                    (System.currentTimeMillis() - lastProcessTime) >= maxDelayMs) {
                    processBatch()
                }
            }
        }

        private suspend fun processBatch() {
            if (buffer.isNotEmpty()) {
                val batch = buffer.toList()
                buffer.clear()
                processor(batch)
                lastProcessTime = System.currentTimeMillis()
            }
        }

        fun stop() {
            job?.cancel()
            scope.launch(dispatcher) {
                processBatch() // Process remaining items
            }
        }
    }

    // Cache implementation
    private class ExpiringCache<K, V>(
        private val maxSize: Int,
        private val expirationMs: Long
    ) {
        private val cache = LruCache<K, Pair<V, Long>>(maxSize)

        fun get(key: K): V? {
            val value = cache.get(key)
            return if (value != null && !isExpired(value.second)) {
                value.first
            } else {
                cache.remove(key)
                null
            }
        }

        fun put(key: K, value: V) {
            cache.put(key, value to System.currentTimeMillis())
        }

        private fun isExpired(timestamp: Long): Boolean {
            return System.currentTimeMillis() - timestamp > expirationMs
        }

        fun clear() {
            cache.evictAll()
        }
    }
}
data class VehicleMetrics(
    val currentData: VehicleData = VehicleData(),
    val analytics: VehicleAnalytics = VehicleAnalytics()
)

data class DisplaySettings(
    val brightness: Float = 0.5f,
    val nightMode: Boolean = false,
    val speedUnit: SpeedUnit = SpeedUnit.KPH,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val theme: Theme = Theme.DARK
)

enum class SpeedUnit {
    KPH,
    MPH
}

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT
}

enum class Theme {
    LIGHT,
    DARK,
    SYSTEM
}

enum class DrivingMode {
    ECO,
    NORMAL,
    PERFORMANCE,
    SPORT
}
private class BatchProcessor<T>(
    private val batchSize: Int,
    private val maxDelayMs: Long,
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val processor: suspend (List<T>) -> Unit
) {
    private val buffer = mutableListOf<T>()
    private var lastProcessTime = 0L
    private var job: Job? = null

    fun add(item: T) {
        scope.launch(dispatcher) {
            buffer.add(item)
            if (buffer.size >= batchSize ||
                (System.currentTimeMillis() - lastProcessTime) >= maxDelayMs) {
                processBatch()
            }
        }
    }

    private suspend fun processBatch() {
        if (buffer.isNotEmpty()) {
            val batch = buffer.toList()
            buffer.clear()
            // Only process if the batch has items
            processor(batch)
            lastProcessTime = System.currentTimeMillis()
        }
    }

    fun stop() {
        job?.cancel()
        scope.launch(dispatcher) {
            processBatch() // Process remaining items
        }
    }
}
data class UnitSettings(
    val speedUnit: SpeedUnit = SpeedUnit.KPH,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS
)

data class VehiclePreferences(
    val defaultDrivingMode: DrivingMode = DrivingMode.NORMAL,
    val automaticHeadlights: Boolean = true,
    val automaticWipers: Boolean = true
)