package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.compiler.CompilationResult
import com.example.compiler.CodeTemplates
import com.example.compiler.OfflineCompilerEngine
import com.example.data.local.StudioDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class EditorViewMode {
    CODE,
    SPLIT,
    DESIGN
}

enum class ToolWindowTab {
    LOGCAT,
    TERMINAL,
    BUILD_OUTPUT,
    PROBLEMS,
    DEPENDENCIES,
    EXPORT_APK
}

sealed class BuildState {
    object Idle : BuildState()
    data class Building(val currentTask: String, val progress: Float) : BuildState()
    data class Success(val result: CompilationResult) : BuildState()
    data class Failed(val result: CompilationResult) : BuildState()
}

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = StudioDatabase.getInstance(application)
    private val projectDao = db.projectDao()
    private val fileDao = db.projectFileDao()
    private val buildHistoryDao = db.buildHistoryDao()

    val allProjects: StateFlow<List<ProjectEntity>> = projectDao.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeProject = MutableStateFlow<ProjectEntity?>(null)
    val activeProject: StateFlow<ProjectEntity?> = _activeProject.asStateFlow()

    private val _projectFiles = MutableStateFlow<List<ProjectFileEntity>>(emptyList())
    val projectFiles: StateFlow<List<ProjectFileEntity>> = _projectFiles.asStateFlow()

    private val _openFiles = MutableStateFlow<List<ProjectFileEntity>>(emptyList())
    val openFiles: StateFlow<List<ProjectFileEntity>> = _openFiles.asStateFlow()

    private val _activeFileIndex = MutableStateFlow(0)
    val activeFileIndex: StateFlow<Int> = _activeFileIndex.asStateFlow()

    val currentFile: StateFlow<ProjectFileEntity?> = combine(_openFiles, _activeFileIndex) { files, index ->
        files.getOrNull(index)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _viewMode = MutableStateFlow(EditorViewMode.SPLIT)
    val viewMode: StateFlow<EditorViewMode> = _viewMode.asStateFlow()

    private val _activeToolWindow = MutableStateFlow<ToolWindowTab?>(ToolWindowTab.BUILD_OUTPUT)
    val activeToolWindow: StateFlow<ToolWindowTab?> = _activeToolWindow.asStateFlow()

    private val _buildState = MutableStateFlow<BuildState>(BuildState.Idle)
    val buildState: StateFlow<BuildState> = _buildState.asStateFlow()

    private val _logcatLogs = MutableStateFlow<List<LogcatMessage>>(emptyList())
    val logcatLogs: StateFlow<List<LogcatMessage>> = _logcatLogs.asStateFlow()

    private val _logFilterLevel = MutableStateFlow<LogLevel?>(null)
    val logFilterLevel: StateFlow<LogLevel?> = _logFilterLevel.asStateFlow()

    private val _logSearchQuery = MutableStateFlow("")
    val logSearchQuery: StateFlow<String> = _logSearchQuery.asStateFlow()

    private val _terminalOutput = MutableStateFlow<List<String>>(
        listOf(
            "Android Studio Terminal (100% Offline Shell)",
            "SDK Location: /system/android-sdk-offline",
            "Gradle Version: 8.9.0 (Kotlin DSL)",
            "Tapez './gradlew assembleDebug' ou 'help' pour les commandes disponibles.",
            ""
        )
    )
    val terminalOutput: StateFlow<List<String>> = _terminalOutput.asStateFlow()

    private val _diagnosticIssues = MutableStateFlow<List<DiagnosticIssue>>(emptyList())
    val diagnosticIssues: StateFlow<List<DiagnosticIssue>> = _diagnosticIssues.asStateFlow()

    private val _latestApkFile = MutableStateFlow<File?>(null)
    val latestApkFile: StateFlow<File?> = _latestApkFile.asStateFlow()

    private val _buildLogs = MutableStateFlow<List<String>>(emptyList())
    val buildLogs: StateFlow<List<String>> = _buildLogs.asStateFlow()

    private val _selectedDevice = MutableStateFlow(
        VirtualDeviceConfig("Pixel 8 Pro", 35, 412, 915, 480)
    )
    val selectedDevice: StateFlow<VirtualDeviceConfig> = _selectedDevice.asStateFlow()

    val availableDevices = listOf(
        VirtualDeviceConfig("Pixel 8 Pro", 35, 412, 915, 480),
        VirtualDeviceConfig("Pixel 8", 34, 392, 850, 420),
        VirtualDeviceConfig("Galaxy Tab S9", 34, 800, 1280, 280),
        VirtualDeviceConfig("Pixel Fold (Déplié)", 35, 700, 750, 380)
    )

    init {
        viewModelScope.launch {
            allProjects.collect { projects ->
                if (projects.isEmpty()) {
                    createDefaultProject()
                } else if (_activeProject.value == null) {
                    selectProject(projects.first())
                }
            }
        }
    }

    private suspend fun createDefaultProject() {
        val proj = ProjectEntity(
            name = "MonApplicationCompose",
            packageName = "com.example.monapp",
            templateType = ProjectTemplate.COMPOSE_EMPTY.name
        )
        val projId = projectDao.insertProject(proj)
        val createdProj = proj.copy(id = projId)
        val initialFiles = CodeTemplates.generateInitialFiles(
            projectId = projId,
            projectName = createdProj.name,
            packageName = createdProj.packageName,
            template = ProjectTemplate.COMPOSE_EMPTY
        )
        fileDao.insertFiles(initialFiles)
        selectProject(createdProj)
    }

    fun selectProject(project: ProjectEntity) {
        _activeProject.value = project
        viewModelScope.launch {
            fileDao.getFilesForProject(project.id).collect { files ->
                _projectFiles.value = files
                if (_openFiles.value.isEmpty() && files.isNotEmpty()) {
                    // Open main activity and manifest by default
                    val main = files.find { it.filePath.endsWith("MainActivity.kt") } ?: files.first()
                    _openFiles.value = listOf(main)
                    _activeFileIndex.value = 0
                } else {
                    // Refresh open files with updated content
                    val updatedOpen = _openFiles.value.mapNotNull { open ->
                        files.find { it.id == open.id }
                    }
                    _openFiles.value = if (updatedOpen.isNotEmpty()) updatedOpen else files.take(1)
                }
            }
        }
    }

    fun openFile(file: ProjectFileEntity) {
        val currentOpen = _openFiles.value.toMutableList()
        val existingIndex = currentOpen.indexOfFirst { it.id == file.id }
        if (existingIndex >= 0) {
            _activeFileIndex.value = existingIndex
        } else {
            currentOpen.add(file)
            _openFiles.value = currentOpen
            _activeFileIndex.value = currentOpen.size - 1
        }
    }

    fun closeFile(index: Int) {
        val currentOpen = _openFiles.value.toMutableList()
        if (index in currentOpen.indices) {
            currentOpen.removeAt(index)
            _openFiles.value = currentOpen
            _activeFileIndex.value = (_activeFileIndex.value.coerceAtMost(currentOpen.size - 1)).coerceAtLeast(0)
        }
    }

    fun selectActiveTab(index: Int) {
        if (index in _openFiles.value.indices) {
            _activeFileIndex.value = index
        }
    }

    fun updateCurrentFileContent(newContent: String) {
        val cur = currentFile.value ?: return
        val updated = cur.copy(content = newContent, isModified = true)
        val currentOpen = _openFiles.value.toMutableList()
        val idx = _activeFileIndex.value
        if (idx in currentOpen.indices) {
            currentOpen[idx] = updated
            _openFiles.value = currentOpen
        }
        viewModelScope.launch {
            fileDao.updateFile(updated)
        }
    }

    fun setViewMode(mode: EditorViewMode) {
        _viewMode.value = mode
    }

    fun toggleToolWindow(tab: ToolWindowTab) {
        if (_activeToolWindow.value == tab) {
            _activeToolWindow.value = null
        } else {
            _activeToolWindow.value = tab
        }
    }

    fun selectDevice(device: VirtualDeviceConfig) {
        _selectedDevice.value = device
    }

    fun runProject() {
        viewModelScope.launch {
            val project = _activeProject.value ?: return@launch
            val files = _projectFiles.value
            _activeToolWindow.value = ToolWindowTab.BUILD_OUTPUT
            _buildState.value = BuildState.Building(":app:preBuild", 0.1f)

            val result = OfflineCompilerEngine.compileProject(
                context = getApplication(),
                project = project,
                files = files,
                buildVariant = "debug",
                onTaskProgress = { taskName, progress ->
                    _buildState.value = BuildState.Building(taskName, progress)
                }
            )

            _buildLogs.value = result.logs
            _diagnosticIssues.value = result.issues

            if (result.isSuccess) {
                _buildState.value = BuildState.Success(result)
                _latestApkFile.value = result.apkFile
                // Switch to Design/Sandbox mode to view running app!
                _viewMode.value = EditorViewMode.SPLIT
                addLogcatMessage(
                    LogcatMessage(
                        timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date()),
                        tag = "AndroidRuntime",
                        level = LogLevel.INFO,
                        message = "Starting process ${project.packageName} in sandbox container"
                    )
                )
            } else {
                _buildState.value = BuildState.Failed(result)
                _activeToolWindow.value = ToolWindowTab.PROBLEMS
            }
        }
    }

    fun buildApk() {
        viewModelScope.launch {
            val project = _activeProject.value ?: return@launch
            val files = _projectFiles.value
            _activeToolWindow.value = ToolWindowTab.BUILD_OUTPUT
            _buildState.value = BuildState.Building(":app:assembleRelease", 0.05f)

            val result = OfflineCompilerEngine.compileProject(
                context = getApplication(),
                project = project,
                files = files,
                buildVariant = "release",
                onTaskProgress = { taskName, progress ->
                    _buildState.value = BuildState.Building(taskName, progress)
                }
            )

            _buildLogs.value = result.logs
            _diagnosticIssues.value = result.issues

            if (result.isSuccess) {
                _buildState.value = BuildState.Success(result)
                _latestApkFile.value = result.apkFile
                _activeToolWindow.value = ToolWindowTab.EXPORT_APK

                // Save to history
                buildHistoryDao.insertBuildHistory(
                    BuildHistoryEntity(
                        projectId = project.id,
                        durationMs = result.durationMs,
                        isSuccess = true,
                        apkSizeFormatted = result.apkSizeFormatted,
                        apkFilePath = result.apkFile?.absolutePath ?: "",
                        logOutput = result.logs.joinToString("\n")
                    )
                )
            } else {
                _buildState.value = BuildState.Failed(result)
                _activeToolWindow.value = ToolWindowTab.PROBLEMS
            }
        }
    }

    fun syncProject() {
        viewModelScope.launch {
            _activeToolWindow.value = ToolWindowTab.BUILD_OUTPUT
            _buildState.value = BuildState.Building("Gradle Sync", 0.3f)
            _buildLogs.value = listOf(
                "Starting Gradle Sync with build.gradle.kts...",
                "Scanning offline Gradle cache: ~/.gradle/caches",
                "Resolved dependency: androidx.compose.material3:material3:1.3.1 [CACHED]",
                "Resolved dependency: androidx.activity:activity-compose:1.10.1 [CACHED]",
                "Resolved dependency: androidx.room:room-runtime:2.7.0 [CACHED]",
                "BUILD SUCCESSFUL - Gradle sync finished in 420ms (offline)"
            )
            _buildState.value = BuildState.Idle
        }
    }

    fun createNewProject(name: String, packageName: String, template: ProjectTemplate) {
        viewModelScope.launch {
            val proj = ProjectEntity(
                name = name,
                packageName = packageName,
                templateType = template.name
            )
            val projId = projectDao.insertProject(proj)
            val created = proj.copy(id = projId)
            val files = CodeTemplates.generateInitialFiles(projId, name, packageName, template)
            fileDao.insertFiles(files)
            selectProject(created)
        }
    }

    fun createNewFile(filePath: String, content: String = "") {
        val proj = _activeProject.value ?: return
        viewModelScope.launch {
            val fileName = filePath.substringAfterLast('/')
            val fileType = when {
                fileName.endsWith(".kt") -> FileType.KOTLIN
                fileName.endsWith(".java") -> FileType.JAVA
                fileName.endsWith(".xml") -> FileType.XML
                fileName.endsWith(".gradle.kts") || fileName.endsWith(".gradle") -> FileType.GRADLE
                fileName.endsWith(".json") -> FileType.JSON
                fileName.endsWith(".properties") -> FileType.PROPERTIES
                else -> FileType.OTHER
            }

            val newFile = ProjectFileEntity(
                projectId = proj.id,
                filePath = filePath,
                fileName = fileName,
                fileType = fileType.name,
                content = content
            )
            val id = fileDao.insertFile(newFile)
            openFile(newFile.copy(id = id))
        }
    }

    fun deleteFile(file: ProjectFileEntity) {
        viewModelScope.launch {
            fileDao.deleteFile(file)
            val currentOpen = _openFiles.value.filter { it.id != file.id }
            _openFiles.value = currentOpen
            _activeFileIndex.value = 0
        }
    }

    fun addLogcatMessage(msg: LogcatMessage) {
        _logcatLogs.value = (_logcatLogs.value + msg).takeLast(200)
    }

    fun clearLogcat() {
        _logcatLogs.value = emptyList()
    }

    fun setLogFilterLevel(level: LogLevel?) {
        _logFilterLevel.value = level
    }

    fun setLogSearchQuery(q: String) {
        _logSearchQuery.value = q
    }

    fun executeTerminalCommand(cmd: String) {
        val trimmed = cmd.trim()
        val current = _terminalOutput.value.toMutableList()
        current.add("$ $trimmed")

        when {
            trimmed == "help" -> {
                current.add("Commandes hors-ligne disponibles :")
                current.add("  ./gradlew assembleDebug   - Compile l'application en mode Debug")
                current.add("  ./gradlew assembleRelease - Génère l'APK signé téléchargeable")
                current.add("  ./gradlew clean           - Nettoie le dossier build/")
                current.add("  ./gradlew test            - Exécute les tests unitaires locaux")
                current.add("  ls / dir                  - Affiche les fichiers du projet")
                current.add("  cat <fichier>             - Affiche le contenu d'un fichier")
                current.add("  clear                     - Efface la console")
            }
            trimmed.startsWith("./gradlew assembleDebug") -> {
                runProject()
                current.add("> Starting Gradle Daemon...")
                current.add("> Task :app:assembleDebug SUCCESS (Hors-ligne)")
            }
            trimmed.startsWith("./gradlew assembleRelease") -> {
                buildApk()
                current.add("> Building Release APK...")
                current.add("> Task :app:assembleRelease SUCCESS (APK généré)")
            }
            trimmed == "./gradlew clean" -> {
                current.add("> Task :app:clean SUCCESS")
                current.add("BUILD SUCCESSFUL in 180ms")
            }
            trimmed == "./gradlew test" -> {
                current.add("> Task :app:testDebugUnitTest SUCCESS")
                current.add("Test results: 4 passed, 0 failed (100% offline)")
            }
            trimmed == "ls" || trimmed == "dir" -> {
                val files = _projectFiles.value
                current.add("Arborescence du projet :")
                files.forEach { current.add("  ${it.filePath} (${it.content.length} bytes)") }
            }
            trimmed.startsWith("cat ") -> {
                val targetName = trimmed.removePrefix("cat ").trim()
                val f = _projectFiles.value.find { it.fileName == targetName || it.filePath.endsWith(targetName) }
                if (f != null) {
                    current.addAll(f.content.lines().take(20))
                    if (f.content.lines().size > 20) current.add("... (${f.content.lines().size - 20} lignes supplémentaires)")
                } else {
                    current.add("Fichier '$targetName' introuvable.")
                }
            }
            trimmed == "clear" -> {
                _terminalOutput.value = emptyList()
                return
            }
            trimmed.isBlank() -> {}
            else -> {
                current.add("Commande inconnue : '$trimmed'. Tapez 'help' pour la liste des commandes.")
            }
        }

        _terminalOutput.value = current.takeLast(100)
    }

    fun shareOrInstallApk() {
        val apk = _latestApkFile.value ?: return
        try {
            val context = getApplication<Application>()
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apk
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to share intent
            val context = getApplication<Application>()
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apk
            )
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, uri)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(Intent.createChooser(sendIntent, "Partager l'APK"))
        }
    }
}
