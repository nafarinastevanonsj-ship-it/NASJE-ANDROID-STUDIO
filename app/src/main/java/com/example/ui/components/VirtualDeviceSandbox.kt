package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LogcatMessage
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectFileEntity
import com.example.data.model.VirtualDeviceConfig
import com.example.emulator.SandboxAppRenderer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VirtualDeviceSandbox(
    project: ProjectEntity?,
    files: List<ProjectFileEntity>,
    selectedDevice: VirtualDeviceConfig,
    onLogEvent: (LogcatMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    var isLandscape by remember { mutableStateOf(false) }
    var restartTrigger by remember { mutableStateOf(0) }

    Surface(
        color = Color(0xFF1E1F22),
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Virtual Device Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF27282B))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.PhoneIphone,
                        contentDescription = null,
                        tint = Color(0xFF3574F0),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${selectedDevice.name} (API ${selectedDevice.apiLevel})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFDFE1E5)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Rotate
                    IconButton(
                        onClick = { isLandscape = !isLandscape },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.ScreenRotation,
                            contentDescription = "Pivoter",
                            tint = Color(0xFF9DA0A8),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Restart
                    IconButton(
                        onClick = { restartTrigger++ },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Redémarrer",
                            tint = Color(0xFF9DA0A8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Virtual Phone Outer Case
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                val phoneWidth = if (isLandscape) 480.dp else 290.dp
                val phoneHeight = if (isLandscape) 290.dp else 520.dp

                Card(
                    modifier = Modifier
                        .size(width = phoneWidth, height = phoneHeight)
                        .border(4.dp, Color(0xFF3A3D42), RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                    ) {
                        // 1. Android Status Bar with Camera Punch-Hole
                        AndroidStatusBar()

                        // 2. Main Screen Area (Interactive App)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color(0xFF121212))
                        ) {
                            key(restartTrigger, project?.id) {
                                if (project != null) {
                                    SandboxAppRenderer(
                                        project = project,
                                        files = files,
                                        onLogEvent = onLogEvent
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Aucun projet actif", color = Color.Gray, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // 3. Android Navigation Gesture Bar
                        AndroidNavBar()
                    }
                }
            }
        }
    }
}

@Composable
private fun AndroidStatusBar() {
    val currentTime = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(Color(0xFF000000).copy(alpha = 0.6f))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Clock
        Text(
            text = currentTime,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        // Camera Cutout Punch Hole
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(Color.Black, CircleShape)
        )

        // System Icons: Offline/Airplane, Battery
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.WifiOff,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.BatteryFull,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
private fun AndroidNavBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .background(Color(0xFF000000).copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        // Android Navigation Pill
        Box(
            modifier = Modifier
                .width(68.dp)
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
        )
    }
}
