package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.viewmodel.BuildState
import com.example.ui.viewmodel.ToolWindowTab
import java.io.File

@Composable
fun BottomToolWindows(
    activeTab: ToolWindowTab?,
    onSelectTab: (ToolWindowTab) -> Unit,
    buildState: BuildState,
    buildLogs: List<String>,
    logcatLogs: List<LogcatMessage>,
    logFilterLevel: LogLevel?,
    onSetFilterLevel: (LogLevel?) -> Unit,
    logSearchQuery: String,
    onSetSearchQuery: (String) -> Unit,
    onClearLogcat: () -> Unit,
    terminalLogs: List<String>,
    onExecuteTerminalCommand: (String) -> Unit,
    diagnosticIssues: List<DiagnosticIssue>,
    latestApk: File?,
    onShareApk: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF2B2D30),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Tab Selector Strip
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF27282B))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item {
                    ToolTabButton(
                        label = "Sortie de build",
                        icon = Icons.Default.Build,
                        badge = if (buildState is BuildState.Building) "..." else null,
                        isSelected = activeTab == ToolWindowTab.BUILD_OUTPUT,
                        onClick = { onSelectTab(ToolWindowTab.BUILD_OUTPUT) }
                    )
                }
                item {
                    ToolTabButton(
                        label = "Logcat",
                        icon = Icons.Default.Dns,
                        badge = if (logcatLogs.isNotEmpty()) "${logcatLogs.size}" else null,
                        isSelected = activeTab == ToolWindowTab.LOGCAT,
                        onClick = { onSelectTab(ToolWindowTab.LOGCAT) }
                    )
                }
                item {
                    ToolTabButton(
                        label = "Terminal",
                        icon = Icons.Default.Terminal,
                        isSelected = activeTab == ToolWindowTab.TERMINAL,
                        onClick = { onSelectTab(ToolWindowTab.TERMINAL) }
                    )
                }
                item {
                    ToolTabButton(
                        label = "Problèmes",
                        icon = Icons.Default.ErrorOutline,
                        badge = if (diagnosticIssues.isNotEmpty()) "${diagnosticIssues.size}" else null,
                        badgeColor = if (diagnosticIssues.any { it.severity == IssueSeverity.ERROR }) Color(0xFFF75464) else Color(0xFFE5A84B),
                        isSelected = activeTab == ToolWindowTab.PROBLEMS,
                        onClick = { onSelectTab(ToolWindowTab.PROBLEMS) }
                    )
                }
                item {
                    ToolTabButton(
                        label = "Télécharger APK",
                        icon = Icons.Default.Download,
                        badge = if (latestApk != null) "Prêt" else null,
                        badgeColor = Color(0xFF388E3C),
                        isSelected = activeTab == ToolWindowTab.EXPORT_APK,
                        onClick = { onSelectTab(ToolWindowTab.EXPORT_APK) }
                    )
                }
                item {
                    ToolTabButton(
                        label = "Dépendances",
                        icon = Icons.Default.Inventory2,
                        isSelected = activeTab == ToolWindowTab.DEPENDENCIES,
                        onClick = { onSelectTab(ToolWindowTab.DEPENDENCIES) }
                    )
                }
            }

            // Expanded Window Body
            if (activeTab != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFF1E1F22))
                ) {
                    when (activeTab) {
                        ToolWindowTab.BUILD_OUTPUT -> {
                            BuildOutputView(buildState = buildState, logs = buildLogs)
                        }
                        ToolWindowTab.LOGCAT -> {
                            LogcatView(
                                logs = logcatLogs,
                                filterLevel = logFilterLevel,
                                onSetFilterLevel = onSetFilterLevel,
                                searchQuery = logSearchQuery,
                                onSetSearchQuery = onSetSearchQuery,
                                onClear = onClearLogcat
                            )
                        }
                        ToolWindowTab.TERMINAL -> {
                            TerminalView(
                                logs = terminalLogs,
                                onExecuteCommand = onExecuteTerminalCommand
                            )
                        }
                        ToolWindowTab.PROBLEMS -> {
                            ProblemsView(issues = diagnosticIssues)
                        }
                        ToolWindowTab.EXPORT_APK -> {
                            ExportApkView(apkFile = latestApk, onShareOrInstall = onShareApk)
                        }
                        ToolWindowTab.DEPENDENCIES -> {
                            DependenciesView()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolTabButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badge: String? = null,
    badgeColor: Color = Color(0xFF3574F0),
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) Color(0xFF1E1F22) else Color.Transparent,
        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
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
            if (badge != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = badge,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BuildOutputView(buildState: BuildState, logs: List<String>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                when (buildState) {
                    is BuildState.Building -> {
                        CircularProgressIndicator(
                            color = Color(0xFF3574F0),
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Compilation hors-ligne : ${buildState.currentTask} (${(buildState.progress * 100).toInt()}%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF56A8F5)
                        )
                    }
                    is BuildState.Success -> {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF388E3C),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BUILD SUCCESSFUL (${buildState.result.durationMs}ms)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    is BuildState.Failed -> {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = null,
                            tint = Color(0xFFF75464),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BUILD FAILED",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF75464)
                        )
                    }
                    BuildState.Idle -> {
                        Text(
                            text = "Prêt pour la compilation",
                            fontSize = 12.sp,
                            color = Color(0xFF9DA0A8)
                        )
                    }
                }
            }
        }

        items(logs) { line ->
            val textColor = when {
                line.contains("FAILED") || line.startsWith("e:") -> Color(0xFFF75464)
                line.contains("SUCCESS") || line.contains("BUILD SUCCESSFUL") -> Color(0xFF4CAF50)
                line.startsWith("> Task") -> Color(0xFF56A8F5)
                else -> Color(0xFFBCBEC4)
            }

            Text(
                text = line,
                color = textColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun LogcatView(
    logs: List<LogcatMessage>,
    filterLevel: LogLevel?,
    onSetFilterLevel: (LogLevel?) -> Unit,
    searchQuery: String,
    onSetSearchQuery: (String) -> Unit,
    onClear: () -> Unit
) {
    val filteredLogs = remember(logs, filterLevel, searchQuery) {
        logs.filter { msg ->
            (filterLevel == null || msg.level == filterLevel) &&
                    (searchQuery.isBlank() || msg.message.contains(searchQuery, ignoreCase = true) || msg.tag.contains(searchQuery, ignoreCase = true))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Controls bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF27282B))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level buttons
            LogLevel.values().forEach { lvl ->
                val isSelected = filterLevel == lvl
                Surface(
                    color = if (isSelected) Color(0xFF3574F0) else Color(0xFF393B40),
                    shape = RoundedCornerShape(3.dp),
                    modifier = Modifier
                        .clickable { onSetFilterLevel(if (isSelected) null else lvl) }
                        .padding(end = 4.dp)
                ) {
                    Text(
                        text = lvl.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Search query
            BasicTextField(
                value = searchQuery,
                onValueChange = onSetSearchQuery,
                textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                cursorBrush = SolidColor(Color(0xFF3574F0)),
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF1E1F22), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                decorationBox = { inner ->
                    if (searchQuery.isEmpty()) {
                        Text("Filtrer tag / message...", color = Color.Gray, fontSize = 11.sp)
                    }
                    inner()
                }
            )

            IconButton(
                onClick = onClear,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Effacer", tint = Color(0xFF9DA0A8), modifier = Modifier.size(14.dp))
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            if (filteredLogs.isEmpty()) {
                item {
                    Text("Aucun log correspondant", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                }
            }
            items(filteredLogs) { msg ->
                val levelColor = when (msg.level) {
                    LogLevel.VERBOSE -> Color(0xFF9DA0A8)
                    LogLevel.DEBUG -> Color(0xFF56A8F5)
                    LogLevel.INFO -> Color(0xFF4CAF50)
                    LogLevel.WARN -> Color(0xFFE5A84B)
                    LogLevel.ERROR -> Color(0xFFF75464)
                }

                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
                    Text(
                        text = "${msg.timestamp} ${msg.level.label}/${msg.tag}: ",
                        color = levelColor,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = msg.message,
                        color = Color(0xFFDFE1E5),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TerminalView(
    logs: List<String>,
    onExecuteCommand: (String) -> Unit
) {
    var cmdInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick command chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF27282B))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("./gradlew assembleDebug", "./gradlew assembleRelease", "./gradlew clean", "help").forEach { cmd ->
                Surface(
                    color = Color(0xFF393B40),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.clickable { onExecuteCommand(cmd) }
                ) {
                    Text(
                        text = cmd,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFDFE1E5),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Terminal logs
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            items(logs) { line ->
                Text(
                    text = line,
                    color = if (line.startsWith("$")) Color(0xFF56A8F5) else Color(0xFFBCBEC4),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                )
            }
        }

        // Terminal Prompt Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF27282B))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$ ", color = Color(0xFF56A8F5), fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            BasicTextField(
                value = cmdInput,
                onValueChange = { cmdInput = it },
                textStyle = TextStyle(color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                cursorBrush = SolidColor(Color(0xFF3574F0)),
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    if (cmdInput.isNotBlank()) {
                        onExecuteCommand(cmdInput)
                        cmdInput = ""
                    }
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Envoyer", tint = Color(0xFF3574F0), modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun ProblemsView(issues: List<DiagnosticIssue>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        if (issues.isEmpty()) {
            item {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF388E3C), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aucun problème détecté. Le code est syntaxiquement valide !", color = Color(0xFFDFE1E5), fontSize = 12.sp)
                }
            }
        } else {
            items(issues) { issue ->
                val icon = if (issue.severity == IssueSeverity.ERROR) Icons.Default.Error else Icons.Default.Warning
                val iconTint = if (issue.severity == IssueSeverity.ERROR) Color(0xFFF75464) else Color(0xFFE5A84B)

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF27282B)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = issue.message,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFDFE1E5)
                            )
                            Text(
                                text = "${issue.filePath} : ligne ${issue.line}",
                                fontSize = 10.sp,
                                color = Color(0xFF9DA0A8),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportApkView(apkFile: File?, onShareOrInstall: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (apkFile != null && apkFile.exists()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF27282B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Android,
                            contentDescription = null,
                            tint = Color(0xFF3DDC84),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = apkFile.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDFE1E5)
                            )
                            Text(
                                text = "Taille : ${(apkFile.length() / 1024.0 / 1024.0).let { String.format("%.2f MB", it) }} | Emplacement : ${apkFile.name}",
                                fontSize = 11.sp,
                                color = Color(0xFF9DA0A8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onShareOrInstall,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.InstallMobile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Installer / Partager l'APK", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF9DA0A8), modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Aucun APK n'a encore été compilé.", color = Color(0xFFDFE1E5), fontSize = 13.sp)
                Text("Cliquez sur le marteau ou 'Compiler l'APK' pour générer le fichier .apk téléchargeable.", color = Color(0xFF9DA0A8), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun DependenciesView() {
    val bundledDependencies = listOf(
        "androidx.compose.material3:material3" to "1.3.1 (Hors-ligne)",
        "androidx.compose.ui:ui" to "1.7.5 (Hors-ligne)",
        "androidx.activity:activity-compose" to "1.10.1 (Hors-ligne)",
        "androidx.room:room-runtime" to "2.7.0 (Hors-ligne)",
        "org.jetbrains.kotlinx:kotlinx-coroutines-android" to "1.10.2 (Hors-ligne)",
        "androidx.core:core-ktx" to "1.18.0 (Hors-ligne)",
        "androidx.lifecycle:lifecycle-viewmodel-compose" to "2.8.7 (Hors-ligne)"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        item {
            Text(
                "Bibliothèques AndroidX & Jetpack intégrées 100% hors-ligne",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF56A8F5),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        items(bundledDependencies) { (lib, ver) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(lib, fontSize = 11.sp, color = Color(0xFFDFE1E5), fontFamily = FontFamily.Monospace)
                Text(ver, fontSize = 10.sp, color = Color(0xFF3DDC84), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
