package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProjectTemplate(val title: String, val description: String, val language: String) {
    COMPOSE_EMPTY(
        "Jetpack Compose Activity",
        "Application moderne avec UI déclarative Material 3 et état réactif.",
        "Kotlin"
    ),
    CLASSIC_VIEWS(
        "XML Views Activity",
        "Application Android classique avec layouts XML, TextView, Button et EditText.",
        "Kotlin + XML"
    ),
    CALCULATOR_APP(
        "Calculatrice Moderne",
        "Application complète de calculatrice avec moteur d'évaluation hors-ligne.",
        "Kotlin + Compose"
    ),
    CANVAS_GAME(
        "Jeu 2D Canvas Arcade",
        "Jeu graphique interactif avec boucle de rendu 60fps et détection de collision.",
        "Kotlin"
    ),
    NOTES_TODO_APP(
        "Gestionnaire de Tâches",
        "App de notes et to-do avec liste dynamique, ajout et suppression.",
        "Kotlin + Compose"
    )
}

enum class FileType {
    KOTLIN,
    JAVA,
    XML,
    GRADLE,
    PROPERTIES,
    JSON,
    MARKDOWN,
    OTHER
}

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val packageName: String,
    val templateType: String = ProjectTemplate.COMPOSE_EMPTY.name,
    val minSdk: Int = 24,
    val targetSdk: Int = 35,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "project_files")
data class ProjectFileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val filePath: String, // e.g. "app/src/main/java/com/example/myapp/MainActivity.kt"
    val fileName: String,
    val fileType: String = FileType.KOTLIN.name,
    val content: String,
    val isModified: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "build_history")
data class BuildHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMs: Long,
    val isSuccess: Boolean,
    val apkSizeFormatted: String = "",
    val apkFilePath: String = "",
    val logOutput: String = ""
)

data class DiagnosticIssue(
    val line: Int,
    val column: Int = 1,
    val message: String,
    val severity: IssueSeverity = IssueSeverity.ERROR,
    val filePath: String
)

enum class IssueSeverity {
    ERROR,
    WARNING,
    INFO
}

data class LogcatMessage(
    val id: Long = System.nanoTime(),
    val timestamp: String,
    val pid: Int = 1042,
    val tag: String,
    val level: LogLevel,
    val message: String
)

enum class LogLevel(val label: String) {
    VERBOSE("V"),
    DEBUG("D"),
    INFO("I"),
    WARN("W"),
    ERROR("E")
}

data class VirtualDeviceConfig(
    val name: String,
    val apiLevel: Int,
    val screenWidthDp: Int,
    val screenHeightDp: Int,
    val densityDpi: Int
)
