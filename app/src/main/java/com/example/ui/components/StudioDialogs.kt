package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectTemplate

@Composable
fun NewProjectDialog(
    onDismiss: () -> Unit,
    onCreateProject: (name: String, packageName: String, template: ProjectTemplate) -> Unit
) {
    var selectedTemplate by remember { mutableStateOf(ProjectTemplate.COMPOSE_EMPTY) }
    var projectName by remember { mutableStateOf("MonProjetCompose") }
    var packageName by remember { mutableStateOf("com.example.monprojet") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Android, contentDescription = null, tint = Color(0xFF3DDC84))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nouveau projet Android Studio", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    "Choisissez un modèle de démarrage (100% hors-ligne) :",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(ProjectTemplate.values()) { tmpl ->
                        val isSelected = tmpl == selectedTemplate
                        Surface(
                            color = if (isSelected) Color(0xFF3574F0).copy(alpha = 0.2f) else Color(0xFF27282B),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF3574F0) else Color(0xFF393B40),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedTemplate = tmpl
                                    if (projectName.startsWith("MonProjet")) {
                                        projectName = when (tmpl) {
                                            ProjectTemplate.COMPOSE_EMPTY -> "MonProjetCompose"
                                            ProjectTemplate.CLASSIC_VIEWS -> "MonProjetViews"
                                            ProjectTemplate.CALCULATOR_APP -> "CalculatriceApp"
                                            ProjectTemplate.CANVAS_GAME -> "ArcadeGameApp"
                                            ProjectTemplate.NOTES_TODO_APP -> "GestionnaireNotes"
                                        }
                                        packageName = "com.example.${projectName.lowercase()}"
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedTemplate = tmpl }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = tmpl.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = tmpl.description,
                                        fontSize = 11.sp,
                                        color = Color(0xFF9DA0A8),
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = projectName,
                    onValueChange = {
                        projectName = it
                        packageName = "com.example.${it.lowercase().replace(" ", "")}"
                    },
                    label = { Text("Nom de l'application") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (projectName.isNotBlank() && packageName.isNotBlank()) {
                        onCreateProject(projectName.trim(), packageName.trim(), selectedTemplate)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3574F0))
            ) {
                Text("Créer le projet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun NewFileDialog(
    packageName: String,
    onDismiss: () -> Unit,
    onCreateFile: (filePath: String, content: String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    var fileTypeChoice by remember { mutableStateOf("Kotlin") }

    val extensions = listOf("Kotlin (.kt)", "XML Layout (.xml)", "Java (.java)", "Gradle (.gradle.kts)")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau fichier", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Sélectionnez le type et entrez le nom :", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    extensions.forEach { ext ->
                        val isSel = fileTypeChoice == ext
                        Surface(
                            color = if (isSel) Color(0xFF3574F0) else Color(0xFF27282B),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.clickable { fileTypeChoice = ext }
                        ) {
                            Text(
                                text = ext.substringBefore(" "),
                                fontSize = 10.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("Nom du fichier (ex: Utils, ActivityDetail)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fileName.isNotBlank()) {
                        val (filePath, defaultContent) = when (fileTypeChoice) {
                            "Kotlin (.kt)" -> {
                                val name = if (fileName.endsWith(".kt")) fileName else "$fileName.kt"
                                val path = "app/src/main/java/${packageName.replace('.', '/')}/$name"
                                val content = "package $packageName\n\nclass ${name.removeSuffix(".kt")} {\n    // Code hors-ligne\n}\n"
                                path to content
                            }
                            "XML Layout (.xml)" -> {
                                val name = if (fileName.endsWith(".xml")) fileName else "$fileName.xml"
                                val path = "app/src/main/res/layout/$name"
                                val content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n    android:layout_width=\"match_parent\"\n    android:layout_height=\"match_parent\"\n    android:orientation=\"vertical\">\n\n</LinearLayout>\n"
                                path to content
                            }
                            "Java (.java)" -> {
                                val name = if (fileName.endsWith(".java")) fileName else "$fileName.java"
                                val path = "app/src/main/java/${packageName.replace('.', '/')}/$name"
                                val content = "package $packageName;\n\npublic class ${name.removeSuffix(".java")} {\n}\n"
                                path to content
                            }
                            else -> {
                                val name = if (fileName.endsWith(".gradle.kts")) fileName else "$fileName.gradle.kts"
                                "app/$name" to "// Gradle build script\n"
                            }
                        }
                        onCreateFile(filePath, defaultContent)
                    }
                }
            ) {
                Text("Créer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
