package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.viewmodel.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioApp(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val projectFiles by viewModel.projectFiles.collectAsStateWithLifecycle()
    val openFiles by viewModel.openFiles.collectAsStateWithLifecycle()
    val activeFileIndex by viewModel.activeFileIndex.collectAsStateWithLifecycle()
    val currentFile by viewModel.currentFile.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val activeToolWindow by viewModel.activeToolWindow.collectAsStateWithLifecycle()
    val buildState by viewModel.buildState.collectAsStateWithLifecycle()
    val buildLogs by viewModel.buildLogs.collectAsStateWithLifecycle()
    val logcatLogs by viewModel.logcatLogs.collectAsStateWithLifecycle()
    val logFilterLevel by viewModel.logFilterLevel.collectAsStateWithLifecycle()
    val logSearchQuery by viewModel.logSearchQuery.collectAsStateWithLifecycle()
    val terminalOutput by viewModel.terminalOutput.collectAsStateWithLifecycle()
    val diagnosticIssues by viewModel.diagnosticIssues.collectAsStateWithLifecycle()
    val latestApkFile by viewModel.latestApkFile.collectAsStateWithLifecycle()
    val selectedDevice by viewModel.selectedDevice.collectAsStateWithLifecycle()

    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF2B2D30),
                modifier = Modifier.width(300.dp)
            ) {
                ProjectTreeDrawer(
                    activeProject = activeProject,
                    files = projectFiles,
                    activeFileId = currentFile?.id,
                    onSelectFile = { file ->
                        viewModel.openFile(file)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onNewFileClick = { showNewFileDialog = true },
                    onDeleteFile = { file -> viewModel.deleteFile(file) },
                    onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = Color(0xFF1E1F22),
            topBar = {
                TopMenuBar(
                    activeProject = activeProject,
                    allProjects = allProjects,
                    onSelectProject = { proj -> viewModel.selectProject(proj) },
                    onNewProjectClick = { showNewProjectDialog = true },
                    viewMode = viewMode,
                    onViewModeChange = { mode -> viewModel.setViewMode(mode) },
                    buildState = buildState,
                    onRunClick = { viewModel.runProject() },
                    onBuildClick = { viewModel.buildApk() },
                    onSyncClick = { viewModel.syncProject() },
                    selectedDevice = selectedDevice,
                    availableDevices = viewModel.availableDevices,
                    onSelectDevice = { dev -> viewModel.selectDevice(dev) },
                    onToggleDrawer = {
                        coroutineScope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    }
                )
            },
            bottomBar = {
                BottomToolWindows(
                    activeTab = activeToolWindow,
                    onSelectTab = { tab -> viewModel.toggleToolWindow(tab) },
                    buildState = buildState,
                    buildLogs = buildLogs,
                    logcatLogs = logcatLogs,
                    logFilterLevel = logFilterLevel,
                    onSetFilterLevel = { lvl -> viewModel.setLogFilterLevel(lvl) },
                    logSearchQuery = logSearchQuery,
                    onSetSearchQuery = { q -> viewModel.setLogSearchQuery(q) },
                    onClearLogcat = { viewModel.clearLogcat() },
                    terminalLogs = terminalOutput,
                    onExecuteTerminalCommand = { cmd -> viewModel.executeTerminalCommand(cmd) },
                    diagnosticIssues = diagnosticIssues,
                    latestApk = latestApkFile,
                    onShareApk = { viewModel.shareOrInstallApk() }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color(0xFF1E1F22))
            ) {
                when (viewMode) {
                    EditorViewMode.CODE -> {
                        EditorComponent(
                            openFiles = openFiles,
                            activeFileIndex = activeFileIndex,
                            currentFile = currentFile,
                            onSelectTab = { idx -> viewModel.selectActiveTab(idx) },
                            onCloseTab = { idx -> viewModel.closeFile(idx) },
                            onContentChange = { newContent -> viewModel.updateCurrentFileContent(newContent) },
                            diagnosticIssues = diagnosticIssues
                        )
                    }

                    EditorViewMode.DESIGN -> {
                        VirtualDeviceSandbox(
                            project = activeProject,
                            files = projectFiles,
                            selectedDevice = selectedDevice,
                            onLogEvent = { log -> viewModel.addLogcatMessage(log) }
                        )
                    }

                    EditorViewMode.SPLIT -> {
                        if (isLandscape) {
                            // Side by side in landscape
                            Row(modifier = Modifier.fillMaxSize()) {
                                EditorComponent(
                                    openFiles = openFiles,
                                    activeFileIndex = activeFileIndex,
                                    currentFile = currentFile,
                                    onSelectTab = { idx -> viewModel.selectActiveTab(idx) },
                                    onCloseTab = { idx -> viewModel.closeFile(idx) },
                                    onContentChange = { newContent -> viewModel.updateCurrentFileContent(newContent) },
                                    diagnosticIssues = diagnosticIssues,
                                    modifier = Modifier.weight(1.1f)
                                )

                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .fillMaxHeight()
                                        .background(Color(0xFF2B2D30))
                                )

                                VirtualDeviceSandbox(
                                    project = activeProject,
                                    files = projectFiles,
                                    selectedDevice = selectedDevice,
                                    onLogEvent = { log -> viewModel.addLogcatMessage(log) },
                                    modifier = Modifier.weight(0.9f)
                                )
                            }
                        } else {
                            // Stacked in portrait
                            Column(modifier = Modifier.fillMaxSize()) {
                                EditorComponent(
                                    openFiles = openFiles,
                                    activeFileIndex = activeFileIndex,
                                    currentFile = currentFile,
                                    onSelectTab = { idx -> viewModel.selectActiveTab(idx) },
                                    onCloseTab = { idx -> viewModel.closeFile(idx) },
                                    onContentChange = { newContent -> viewModel.updateCurrentFileContent(newContent) },
                                    diagnosticIssues = diagnosticIssues,
                                    modifier = Modifier.weight(1f)
                                )

                                Box(
                                    modifier = Modifier
                                        .height(2.dp)
                                        .fillMaxWidth()
                                        .background(Color(0xFF2B2D30))
                                )

                                VirtualDeviceSandbox(
                                    project = activeProject,
                                    files = projectFiles,
                                    selectedDevice = selectedDevice,
                                    onLogEvent = { log -> viewModel.addLogcatMessage(log) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // New Project Dialog
    if (showNewProjectDialog) {
        NewProjectDialog(
            onDismiss = { showNewProjectDialog = false },
            onCreateProject = { name, pkg, tmpl ->
                viewModel.createNewProject(name, pkg, tmpl)
                showNewProjectDialog = false
            }
        )
    }

    // New File Dialog
    if (showNewFileDialog) {
        NewFileDialog(
            packageName = activeProject?.packageName ?: "com.example.app",
            onDismiss = { showNewFileDialog = false },
            onCreateFile = { path, content ->
                viewModel.createNewFile(path, content)
                showNewFileDialog = false
            }
        )
    }
}
