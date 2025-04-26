package com.example.automobilemediaapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.random.Random

@Composable
fun NavigationSystemView() {
    var currentX by remember { mutableStateOf(0f) }
    var currentY by remember { mutableStateOf(0f) }
    var destinationX by remember { mutableStateOf(0f) }
    var destinationY by remember { mutableStateOf(0f) }
    val waypoints = remember { mutableStateListOf<Pair<Float, Float>>() }
    val path = remember { Path() }
    
    val instructions = remember {
        listOf(
            "Turn right in 200 meters",
            "Continue straight for 1 kilometer",
            "Take the next left",
            "Merge onto the highway",
            "Exit in 500 meters"
        )
    }
    
    var currentInstructionIndex by remember { mutableStateOf(0) }

    // Generate initial random route
    LaunchedEffect(Unit) {
        generateRandomRoute(path, waypoints) { x, y, destX, destY ->
            currentX = x
            currentY = y
            destinationX = destX
            destinationY = destY
        }
    }

    // Update route animation
    LaunchedEffect(Unit) {
        while (true) {
            updateRoute(
                waypoints,
                currentX,
                currentY,
                destinationX,
                destinationY
            ) { newX, newY ->
                currentX = newX
                currentY = newY
                if (waypoints.isNotEmpty()) {
                    currentInstructionIndex = (currentInstructionIndex + 1) % instructions.size
                }
            }
            kotlinx.coroutines.delay(16) // ~60fps
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Draw route path
        drawPath(
            path = path,
            color = Color.Blue,
            style = Stroke(
                width = 8f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
            )
        )

        // Draw current location
        drawCircle(
            color = Color.Red,
            radius = 15f,
            center = Offset(currentX, currentY),
            style = Stroke(width = 12f)
        )

        // Draw destination
        drawCircle(
            color = Color.Green,
            radius = 15f,
            center = Offset(destinationX, destinationY),
            style = Stroke(width = 12f)
        )
    }
}

private fun generateRandomRoute(
    path: Path,
    waypoints: MutableList<Pair<Float, Float>>,
    onPositionsUpdated: (Float, Float, Float, Float) -> Unit
) {
    path.reset()
    val startX = Random.nextFloat() * 1000
    val startY = Random.nextFloat() * 1000
    val endX = Random.nextFloat() * 1000
    val endY = Random.nextFloat() * 1000

    path.moveTo(startX, startY)
    waypoints.clear()
    var x = startX
    var y = startY

    repeat(5) {
        x += Random.nextFloat() * 200 - 100
        y += Random.nextFloat() * 200 - 100
        waypoints.add(Pair(x, y))
        path.lineTo(x, y)
    }
    path.lineTo(endX, endY)
    
    onPositionsUpdated(startX, startY, endX, endY)
}

private fun updateRoute(
    waypoints: MutableList<Pair<Float, Float>>,
    currentX: Float,
    currentY: Float,
    destinationX: Float,
    destinationY: Float,
    onPositionUpdated: (Float, Float) -> Unit
) {
    var newX = currentX
    var newY = currentY
    
    if (waypoints.isNotEmpty()) {
        val nextWaypoint = waypoints.first()
        val directionX = nextWaypoint.first - currentX
        val directionY = nextWaypoint.second - currentY
        val distance = kotlin.math.sqrt((directionX * directionX + directionY * directionY))
        if (distance > 5) {
            newX += directionX * 0.1f
            newY += directionY * 0.1f
        } else {
            waypoints.removeAt(0)
        }
    } else {
        val directionX = destinationX - currentX
        val directionY = destinationY - currentY
        val distance = kotlin.math.sqrt((directionX * directionX + directionY * directionY))
        if (distance > 5) {
            newX += directionX * 0.05f
            newY += directionY * 0.05f
        }
    }
    
    onPositionUpdated(newX, newY)
}
