package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.FileType
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectFileEntity

enum class ProjectExplorerViewMode {
    ANDROID,
    PROJECT
}

@Composable
fun ProjectTreeDrawer(
    activeProject: ProjectEntity?,
    files: List<ProjectFileEntity>,
    activeFileId: Long?,
    onSelectFile: (ProjectFileEntity) -> Unit,
    onNewFileClick: () -> Unit,
    onDeleteFile: (ProjectFileEntity) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(ProjectExplorerViewMode.ANDROID) }
    var fileToDelete by remember { mutableStateOf<ProjectFileEntity?>(null) }

    Surface(
        color = Color(0xFF2B2D30),
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Project Tool Window Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF27282B))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccountTree,
                        contentDescription = null,
                        tint = Color(0xFF3574F0),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Projet",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDFE1E5)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNewFileClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.NoteAdd,
                            contentDescription = "Nouveau fichier",
                            tint = Color(0xFF9DA0A8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onCloseDrawer,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = Color(0xFF9DA0A8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // View Mode Selector (Android vs Project)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .background(Color(0xFF1E1F22), RoundedCornerShape(6.dp))
                    .padding(2.dp)
            ) {
                Surface(
                    color = if (viewMode == ProjectExplorerViewMode.ANDROID) Color(0xFF3574F0) else Color.Transparent,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewMode = ProjectExplorerViewMode.ANDROID }
                ) {
                    Text(
                        "Android",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (viewMode == ProjectExplorerViewMode.ANDROID) Color.White else Color(0xFF9DA0A8),
                        modifier = Modifier.padding(vertical = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                Surface(
                    color = if (viewMode == ProjectExplorerViewMode.PROJECT) Color(0xFF3574F0) else Color.Transparent,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewMode = ProjectExplorerViewMode.PROJECT }
                ) {
                    Text(
                        "Fichiers",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (viewMode == ProjectExplorerViewMode.PROJECT) Color.White else Color(0xFF9DA0A8),
                        modifier = Modifier.padding(vertical = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // File Tree List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                if (viewMode == ProjectExplorerViewMode.ANDROID) {
                    // Group 1: manifests
                    val manifestFiles = files.filter { it.filePath.contains("AndroidManifest.xml") }
                    if (manifestFiles.isNotEmpty()) {
                        item {
                            FolderHeader(title = "manifests", icon = Icons.Default.FolderOpen)
                        }
                        items(manifestFiles) { f ->
                            FileTreeItem(
                                file = f,
                                isSelected = f.id == activeFileId,
                                indentLevel = 1,
                                onClick = { onSelectFile(f) },
                                onDelete = { fileToDelete = f }
                            )
                        }
                    }

                    // Group 2: java / kotlin
                    val codeFiles = files.filter { it.filePath.contains("/src/main/java") || it.filePath.endsWith(".kt") || it.filePath.endsWith(".java") }
                    if (codeFiles.isNotEmpty()) {
                        item {
                            FolderHeader(
                                title = "java + kotlin",
                                subtitle = activeProject?.packageName,
                                icon = Icons.Default.FolderOpen
                            )
                        }
                        items(codeFiles) { f ->
                            FileTreeItem(
                                file = f,
                                isSelected = f.id == activeFileId,
                                indentLevel = 1,
                                onClick = { onSelectFile(f) },
                                onDelete = { fileToDelete = f }
                            )
                        }
                    }

                    // Group 3: res
                    val resFiles = files.filter { it.filePath.contains("/res/") }
                    if (resFiles.isNotEmpty()) {
                        item {
                            FolderHeader(title = "res", icon = Icons.Default.FolderOpen)
                        }
                        items(resFiles) { f ->
                            val subFolder = if (f.filePath.contains("/layout/")) "layout" else "values"
                            FileTreeItem(
                                file = f,
                                customLabel = "$subFolder / ${f.fileName}",
                                isSelected = f.id == activeFileId,
                                indentLevel = 1,
                                onClick = { onSelectFile(f) },
                                onDelete = { fileToDelete = f }
                            )
                        }
                    }

                    // Group 4: Gradle Scripts
                    val gradleFiles = files.filter { it.filePath.endsWith(".gradle.kts") || it.filePath.endsWith(".gradle") || it.filePath.endsWith(".properties") }
                    if (gradleFiles.isNotEmpty()) {
                        item {
                            FolderHeader(title = "Gradle Scripts", icon = Icons.Default.FolderSpecial)
                        }
                        items(gradleFiles) { f ->
                            val desc = if (f.fileName == "build.gradle.kts") " (Module :app)" else " (Project Settings)"
                            FileTreeItem(
                                file = f,
                                customLabel = "${f.fileName}$desc",
                                isSelected = f.id == activeFileId,
                                indentLevel = 1,
                                onClick = { onSelectFile(f) },
                                onDelete = { fileToDelete = f }
                            )
                        }
                    }
                } else {
                    // Full tree by paths
                    item {
                        FolderHeader(
                            title = activeProject?.name ?: "app",
                            subtitle = "/workspace/${activeProject?.packageName}",
                            icon = Icons.Default.Folder
                        )
                    }
                    items(files) { f ->
                        FileTreeItem(
                            file = f,
                            customLabel = f.filePath,
                            isSelected = f.id == activeFileId,
                            indentLevel = 0,
                            onClick = { onSelectFile(f) },
                            onDelete = { fileToDelete = f }
                        )
                    }
                }
            }

            // Bottom Project Metadata Footer
            Surface(
                color = Color(0xFF27282B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        "Package : ${activeProject?.packageName}",
                        fontSize = 10.sp,
                        color = Color(0xFF9DA0A8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Compile SDK : ${activeProject?.targetSdk ?: 35} | Min SDK : ${activeProject?.minSdk ?: 24}",
                        fontSize = 10.sp,
                        color = Color(0xFF9DA0A8)
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    fileToDelete?.let { f ->
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Supprimer le fichier ?") },
            text = { Text("Voulez-vous vraiment supprimer ${f.fileName} ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteFile(f)
                        fileToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF75464))
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun FolderHeader(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFF3574F0),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFDFE1E5)
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "($subtitle)",
                fontSize = 10.sp,
                color = Color(0xFF9DA0A8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FileTreeItem(
    file: ProjectFileEntity,
    isSelected: Boolean,
    indentLevel: Int,
    customLabel: String? = null,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val fileIcon = when (file.fileType) {
        FileType.KOTLIN.name -> Icons.Default.Code
        FileType.JAVA.name -> Icons.Default.Terminal
        FileType.XML.name -> Icons.Default.Description
        FileType.GRADLE.name -> Icons.Default.Settings
        else -> Icons.Default.InsertDriveFile
    }

    val iconColor = when (file.fileType) {
        FileType.KOTLIN.name -> Color(0xFF7F52FF) // Kotlin Purple
        FileType.JAVA.name -> Color(0xFFE5A84B)   // Java Orange
        FileType.XML.name -> Color(0xFF56A8F5)    // XML Cyan
        FileType.GRADLE.name -> Color(0xFF00C853) // Gradle Green
        else -> Color(0xFF9DA0A8)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 1.dp)
            .background(
                if (isSelected) Color(0xFF3574F0).copy(alpha = 0.25f) else Color.Transparent,
                RoundedCornerShape(4.dp)
            )
            .clickable { onClick() }
            .padding(start = (indentLevel * 16).dp, top = 4.dp, bottom = 4.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                fileIcon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = customLabel ?: file.fileName,
                fontSize = 12.sp,
                color = if (isSelected) Color(0xFFDFE1E5) else Color(0xFFBCBEC4),
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Delete icon on hover/subtle
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(20.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Supprimer",
                tint = Color(0xFF5E6066),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
