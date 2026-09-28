package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectEntity
import com.example.data.model.VirtualDeviceConfig
import com.example.ui.viewmodel.BuildState
import com.example.ui.viewmodel.EditorViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopMenuBar(
    activeProject: ProjectEntity?,
    allProjects: List<ProjectEntity>,
    onSelectProject: (ProjectEntity) -> Unit,
    onNewProjectClick: () -> Unit,
    viewMode: EditorViewMode,
    onViewModeChange: (EditorViewMode) -> Unit,
    buildState: BuildState,
    onRunClick: () -> Unit,
    onBuildClick: () -> Unit,
    onSyncClick: () -> Unit,
    selectedDevice: VirtualDeviceConfig,
    availableDevices: List<VirtualDeviceConfig>,
    onSelectDevice: (VirtualDeviceConfig) -> Unit,
    onToggleDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showProjectMenu by remember { mutableStateOf(false) }
    var showDeviceMenu by remember { mutableStateOf(false) }

    Surface(
        color = Color(0xFF2B2D30),
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            // Main Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Drawer menu & Project Name Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    IconButton(
                        onClick = onToggleDrawer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu Projet",
                            tint = Color(0xFFDFE1E5)
                        )
                    }

                    // Android Studio Logo / Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFF1E1F22), RoundedCornerShape(6.dp))
                            .clickable { showProjectMenu = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.Android,
                            contentDescription = null,
                            tint = Color(0xFF3DDC84),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = activeProject?.name ?: "Android Studio",
                            color = Color(0xFFDFE1E5),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color(0xFF9DA0A8),
                            modifier = Modifier.size(16.dp)
                        )

                        DropdownMenu(
                            expanded = showProjectMenu,
                            onDismissRequest = { showProjectMenu = false }
                        ) {
                            Text(
                                "Projets récents",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                            allProjects.forEach { proj ->
                                DropdownMenuItem(
                                    text = { Text(proj.name, fontWeight = if (proj.id == activeProject?.id) FontWeight.Bold else FontWeight.Normal) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = if (proj.id == activeProject?.id) Color(0xFF3574F0) else Color.Gray
                                        )
                                    },
                                    onClick = {
                                        onSelectProject(proj)
                                        showProjectMenu = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Nouveau projet...") },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF3DDC84)) },
                                onClick = {
                                    showProjectMenu = false
                                    onNewProjectClick()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Middle: Device Selector
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFF1E1F22), RoundedCornerShape(6.dp))
                            .clickable { showDeviceMenu = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = Color(0xFF3574F0),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = selectedDevice.name,
                            color = Color(0xFFDFE1E5),
                            fontSize = 11.sp
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color(0xFF9DA0A8),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showDeviceMenu,
                        onDismissRequest = { showDeviceMenu = false }
                    ) {
                        Text(
                            "Appareils Virtuels (AVD)",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                        availableDevices.forEach { dev ->
                            DropdownMenuItem(
                                text = { Text("${dev.name} (API ${dev.apiLevel})") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Smartphone,
                                        contentDescription = null,
                                        tint = if (dev.name == selectedDevice.name) Color(0xFF3574F0) else Color.Gray
                                    )
                                },
                                onClick = {
                                    onSelectDevice(dev)
                                    showDeviceMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Right: Action buttons (Run, Build, Sync, View Modes)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Sync Gradle
                    IconButton(
                        onClick = onSyncClick,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = "Sync Gradle",
                            tint = Color(0xFFDFE1E5),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Build APK (Hammer)
                    IconButton(
                        onClick = onBuildClick,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = "Compiler l'APK",
                            tint = Color(0xFFE5A84B),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Run App (Green Play Triangle)
                    FilledIconButton(
                        onClick = onRunClick,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF388E3C)),
                        modifier = Modifier.size(34.dp)
                    ) {
                        if (buildState is BuildState.Building) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Run app",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Sub-bar: View Mode Toggles (Code | Split | Design)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1F22))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    ViewModeButton(
                        label = "Code",
                        icon = Icons.Default.Code,
                        isSelected = viewMode == EditorViewMode.CODE,
                        onClick = { onViewModeChange(EditorViewMode.CODE) }
                    )
                    ViewModeButton(
                        label = "Split",
                        icon = Icons.Default.VerticalSplit,
                        isSelected = viewMode == EditorViewMode.SPLIT,
                        onClick = { onViewModeChange(EditorViewMode.SPLIT) }
                    )
                    ViewModeButton(
                        label = "Design",
                        icon = Icons.Default.Visibility,
                        isSelected = viewMode == EditorViewMode.DESIGN,
                        onClick = { onViewModeChange(EditorViewMode.DESIGN) }
                    )
                }

                // 100% Offline Badge
                Surface(
                    color = Color(0xFF1A3626),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF3DDC84), RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "100% HORS-LIGNE",
                            color = Color(0xFF3DDC84),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewModeButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) Color(0xFF3574F0).copy(alpha = 0.25f) else Color.Transparent,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF56A8F5) else Color(0xFF9DA0A8),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color(0xFFDFE1E5) else Color(0xFF9DA0A8)
            )
        }
    }
}
