package com.example.automobilemediaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextLayoutResult

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashboardScreen(
                gear = "D",
                speed = 60,
                rpm = 3000,
                range = 450,
                temperature = 22.0f,
                time = "20:00"
            )
        }
    }
}

@Composable
fun DashboardScreen(
    gear: String,
    speed: Int,
    rpm: Int,
    range: Int,
    temperature: Float,
    time: String
) {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Gear: $gear",
                onTextLayout = { /* empty */ },
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = "Speed: $speed",
                onTextLayout = { /* empty */ },
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = "RPM: $rpm",
                onTextLayout = { /* empty */ },
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = "Range: $range",
                onTextLayout = { /* empty */ },
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = "Temp: $temperature",
                onTextLayout = { /* empty */ },
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = "Time: $time",
                onTextLayout = { /* empty */ },
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}