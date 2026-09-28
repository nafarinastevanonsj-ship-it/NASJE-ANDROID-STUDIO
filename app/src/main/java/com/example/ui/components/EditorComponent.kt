package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compiler.SyntaxHighlighter
import com.example.data.model.DiagnosticIssue
import com.example.data.model.FileType
import com.example.data.model.ProjectFileEntity

@Composable
fun EditorComponent(
    openFiles: List<ProjectFileEntity>,
    activeFileIndex: Int,
    currentFile: ProjectFileEntity?,
    onSelectTab: (Int) -> Unit,
    onCloseTab: (Int) -> Unit,
    onContentChange: (String) -> Unit,
    diagnosticIssues: List<DiagnosticIssue>,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }

    // Text field state synced with current file
    var textFieldValue by remember(currentFile?.id) {
        mutableStateOf(TextFieldValue(currentFile?.content ?: ""))
    }

    val currentIssues = remember(diagnosticIssues, currentFile?.filePath) {
        diagnosticIssues.filter { it.filePath == currentFile?.filePath }
    }

    val fileType = remember(currentFile?.fileType) {
        try {
            FileType.valueOf(currentFile?.fileType ?: FileType.KOTLIN.name)
        } catch (e: Exception) {
            FileType.KOTLIN
        }
    }

    Surface(
        color = Color(0xFF1E1F22), // Android Studio Editor background
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 1. File Tabs Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2B2D30))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                itemsIndexed(openFiles) { index, file ->
                    val isActive = index == activeFileIndex
                    FileTabItem(
                        fileName = file.fileName,
                        fileType = file.fileType,
                        isActive = isActive,
                        isModified = file.isModified,
                        onClick = { onSelectTab(index) },
                        onClose = { onCloseTab(index) }
                    )
                }
            }

            // 2. Optional Find / Search Bar
            if (isSearchVisible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF27282B))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF9DA0A8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                        cursorBrush = SolidColor(Color(0xFF3574F0)),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text("Rechercher dans le fichier...", color = Color.Gray, fontSize = 12.sp)
                            }
                            innerTextField()
                        }
                    )
                    IconButton(
                        onClick = {
                            searchQuery = ""
                            isSearchVisible = false
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.Gray, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // 3. Diagnostic Issue Banner (if errors detected on this file)
            if (currentIssues.isNotEmpty()) {
                val firstErr = currentIssues.first()
                Surface(
                    color = Color(0xFF5A1D1D),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFF75464),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ligne ${firstErr.line}: ${firstErr.message}",
                            color = Color(0xFFFFD2D2),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4. Code Area with Line Numbers Gutter
            if (currentFile != null) {
                val text = textFieldValue.text
                val lines = remember(text) { text.lines() }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val verticalScrollState = rememberScrollState()
                    val horizontalScrollState = rememberScrollState()

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(verticalScrollState)
                    ) {
                        // Line Numbers Gutter
                        Column(
                            modifier = Modifier
                                .background(Color(0xFF1E1F22))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            lines.indices.forEach { idx ->
                                val lineNum = idx + 1
                                val hasError = currentIssues.any { it.line == lineNum }
                                Text(
                                    text = "$lineNum",
                                    color = if (hasError) Color(0xFFF75464) else Color(0xFF5E6066),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 18.sp,
                                    fontWeight = if (hasError) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }

                        // Vertical Separator line
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(Color(0xFF2B2D30))
                        )

                        // Editable Code Content
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(horizontalScrollState)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            BasicTextField(
                                value = textFieldValue,
                                onValueChange = { newVal ->
                                    textFieldValue = newVal
                                    onContentChange(newVal.text)
                                },
                                textStyle = TextStyle(
                                    color = Color(0xFFDFE1E5),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                ),
                                cursorBrush = SolidColor(Color(0xFF3574F0)),
                                visualTransformation = {
                                    androidx.compose.ui.text.input.TransformedText(
                                        SyntaxHighlighter.highlightCode(it.text, fileType),
                                        androidx.compose.ui.text.input.OffsetMapping.Identity
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // 5. Quick Snippet Toolbar above keyboard
                DeveloperQuickSnippetBar(
                    onInsert = { snippet ->
                        val currentText = textFieldValue.text
                        val selection = textFieldValue.selection
                        val newText = if (selection.start >= 0 && selection.end <= currentText.length) {
                            currentText.substring(0, selection.start) + snippet + currentText.substring(selection.end)
                        } else {
                            currentText + snippet
                        }
                        val newPos = selection.start + snippet.length
                        textFieldValue = TextFieldValue(
                            text = newText,
                            selection = androidx.compose.ui.text.TextRange(newPos)
                        )
                        onContentChange(newText)
                    },
                    onToggleSearch = { isSearchVisible = !isSearchVisible }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Code,
                            contentDescription = null,
                            tint = Color(0xFF43454A),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Aucun fichier ouvert",
                            color = Color(0xFF9DA0A8),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FileTabItem(
    fileName: String,
    fileType: String,
    isActive: Boolean,
    isModified: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    val tabColor = if (isActive) Color(0xFF1E1F22) else Color(0xFF2B2D30)
    val indicatorColor = if (isActive) Color(0xFF3574F0) else Color.Transparent

    Column(
        modifier = Modifier
            .padding(end = 2.dp)
            .background(tabColor, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isModified) "$fileName *" else fileName,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isActive) Color(0xFFDFE1E5) else Color(0xFF9DA0A8)
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Fermer onglet",
                    tint = if (isActive) Color(0xFF9DA0A8) else Color(0xFF5E6066),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .height(2.dp)
                .fillMaxWidth()
                .background(indicatorColor)
        )
    }
}

@Composable
private fun DeveloperQuickSnippetBar(
    onInsert: (String) -> Unit,
    onToggleSearch: () -> Unit
) {
    val commonSnippets = listOf(
        "TAB" to "    ",
        "{" to " { ",
        "}" to " }",
        "(" to "(",
        ")" to ")",
        "\"\"" to "\"\"",
        "=" to " = ",
        "->" to " -> ",
        ": " to ": ",
        "." to ".",
        "@Composable" to "@Composable\n",
        "Modifier" to "Modifier.",
        "remember" to "remember { mutableStateOf() }",
        "Text()" to "Text(\"\")",
        "Button()" to "Button(onClick = { }) {\n    Text(\"\")\n}"
    )

    Surface(
        color = Color(0xFF27282B),
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                IconButton(
                    onClick = onToggleSearch,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Rechercher",
                        tint = Color(0xFFDFE1E5),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            itemsIndexed(commonSnippets) { _, (label, code) ->
                Surface(
                    color = Color(0xFF393B40),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.clickable { onInsert(code) }
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFDFE1E5),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}
