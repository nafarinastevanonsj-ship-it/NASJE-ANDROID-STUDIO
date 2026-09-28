package com.example.emulator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LogLevel
import com.example.data.model.LogcatMessage
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectFileEntity
import com.example.data.model.ProjectTemplate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SandboxAppRenderer(
    project: ProjectEntity,
    files: List<ProjectFileEntity>,
    onLogEvent: (LogcatMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val template = remember(project.templateType) {
        try {
            ProjectTemplate.valueOf(project.templateType)
        } catch (e: Exception) {
            ProjectTemplate.COMPOSE_EMPTY
        }
    }

    val mainFile = remember(files) {
        files.find { it.filePath.endsWith("MainActivity.kt") }
    }
    val codeContent = mainFile?.content ?: ""

    // Initial lifecycle log
    LaunchedEffect(project.id) {
        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        onLogEvent(
            LogcatMessage(
                timestamp = time,
                tag = "ActivityThread",
                level = LogLevel.INFO,
                message = "Attached application ${project.packageName} [PID: 1042]"
            )
        )
        onLogEvent(
            LogcatMessage(
                timestamp = time,
                tag = "MainActivity",
                level = LogLevel.DEBUG,
                message = "onCreate(savedInstanceState=null) called"
            )
        )
        onLogEvent(
            LogcatMessage(
                timestamp = time,
                tag = "ViewRootImpl",
                level = LogLevel.DEBUG,
                message = "Starting layout traversal for ${project.name}"
            )
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (template) {
            ProjectTemplate.COMPOSE_EMPTY -> {
                ComposeSandboxView(
                    projectName = project.name,
                    code = codeContent,
                    onLogEvent = onLogEvent
                )
            }
            ProjectTemplate.CLASSIC_VIEWS -> {
                ClassicViewsSandboxView(
                    projectName = project.name,
                    files = files,
                    onLogEvent = onLogEvent
                )
            }
            ProjectTemplate.CALCULATOR_APP -> {
                CalculatorSandboxView(onLogEvent = onLogEvent)
            }
            ProjectTemplate.CANVAS_GAME -> {
                CanvasGameSandboxView(onLogEvent = onLogEvent)
            }
            ProjectTemplate.NOTES_TODO_APP -> {
                NotesSandboxView(onLogEvent = onLogEvent)
            }
        }
    }
}

@Composable
private fun ComposeSandboxView(
    projectName: String,
    code: String,
    onLogEvent: (LogcatMessage) -> Unit
) {
    var counter by remember { mutableStateOf(0) }
    var statusText by remember { mutableStateOf("Compilé hors-ligne avec succès") }

    // Detect if user modified strings or initial values in code
    val displayTitle = remember(code, projectName) {
        val titleMatch = Regex("""MainScreen\(appName\s*=\s*"([^"]+)"\)""").find(code)
        titleMatch?.groupValues?.get(1) ?: projectName
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = displayTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Exécution en bac à sable (Pixel 8)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Compteur d'état Compose :",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "$counter",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        counter++
                        statusText = "Incrémenté : $counter"
                        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
                        onLogEvent(
                            LogcatMessage(
                                timestamp = time,
                                tag = "MainActivity",
                                level = LogLevel.DEBUG,
                                message = "Button onClick -> counter incremented to $counter"
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Incrémenter", fontSize = 13.sp)
                }

                FilledTonalButton(
                    onClick = {
                        counter = 0
                        statusText = "Remis à zéro"
                        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
                        onLogEvent(
                            LogcatMessage(
                                timestamp = time,
                                tag = "MainActivity",
                                level = LogLevel.INFO,
                                message = "Reset button clicked -> counter reset to 0"
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ClassicViewsSandboxView(
    projectName: String,
    files: List<ProjectFileEntity>,
    onLogEvent: (LogcatMessage) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("En attente de saisie...") }

    val layoutFile = files.find { it.filePath.endsWith("activity_main.xml") }
    val xmlContent = layoutFile?.content ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = projectName,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Inflation Android Layout XML : activity_main.xml",
            color = Color(0xFFA0A0A0),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = { Text("android:hint=\"Entrez votre texte...\"", color = Color(0xFF757575)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF3574F0),
                unfocusedBorderColor = Color(0xFF444444)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
                if (inputText.isNotBlank()) {
                    resultText = "Texte reçu : $inputText"
                    onLogEvent(
                        LogcatMessage(
                            timestamp = time,
                            tag = "MainActivity",
                            level = LogLevel.DEBUG,
                            message = "findViewById(R.id.btnSubmit) -> onClickListener executed with '$inputText'"
                        )
                    )
                } else {
                    resultText = "Veuillez entrer du texte !"
                    onLogEvent(
                        LogcatMessage(
                            timestamp = time,
                            tag = "MainActivity",
                            level = LogLevel.WARN,
                            message = "Empty input submission"
                        )
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3574F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Valider l'entrée (Button)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = resultText,
            color = Color(0xFF4CAF50),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CalculatorSandboxView(onLogEvent: (LogcatMessage) -> Unit) {
    var display by remember { mutableStateOf("0") }
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var operator by remember { mutableStateOf<String?>(null) }
    var isNewOp by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF17171C))
            .padding(14.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = display,
            fontSize = 42.sp,
            fontWeight = FontWeight.Light,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        val buttons = listOf(
            listOf("C", "+/-", "%", "/"),
            listOf("7", "8", "9", "*"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { label ->
                    val weight = if (label == "0") 2f else 1f
                    val isOp = label in listOf("/", "*", "-", "+", "=")
                    val isSpecial = label in listOf("C", "+/-", "%")

                    val btnColor = when {
                        isOp -> Color(0xFF4B5EFC)
                        isSpecial -> Color(0xFF4E505F)
                        else -> Color(0xFF2E303C)
                    }

                    Button(
                        onClick = {
                            val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
                            onLogEvent(
                                LogcatMessage(
                                    timestamp = time,
                                    tag = "Calculator",
                                    level = LogLevel.DEBUG,
                                    message = "Key pressed: $label, current display: $display"
                                )
                            )

                            when (label) {
                                "C" -> {
                                    display = "0"
                                    operand1 = null
                                    operator = null
                                    isNewOp = false
                                }
                                "+", "-", "*", "/" -> {
                                    operand1 = display.toDoubleOrNull()
                                    operator = label
                                    isNewOp = true
                                }
                                "=" -> {
                                    val op1 = operand1
                                    val op = operator
                                    val op2 = display.toDoubleOrNull()
                                    if (op1 != null && op != null && op2 != null) {
                                        val res = when (op) {
                                            "+" -> op1 + op2
                                            "-" -> op1 - op2
                                            "*" -> op1 * op2
                                            "/" -> if (op2 != 0.0) op1 / op2 else Double.NaN
                                            else -> op2
                                        }
                                        display = if (res.isNaN()) "Erreur" else if (res % 1 == 0.0) res.toLong().toString() else res.toString()
                                        operand1 = null
                                        operator = null
                                        isNewOp = true
                                        onLogEvent(
                                            LogcatMessage(
                                                timestamp = time,
                                                tag = "Calculator",
                                                level = LogLevel.INFO,
                                                message = "Evaluated: $op1 $op $op2 = $display"
                                            )
                                        )
                                    }
                                }
                                else -> {
                                    if (display == "0" || isNewOp) {
                                        display = label
                                        isNewOp = false
                                    } else {
                                        display += label
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(weight)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = label, fontSize = 20.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun CanvasGameSandboxView(onLogEvent: (LogcatMessage) -> Unit) {
    var score by remember { mutableStateOf(0) }
    var clicks by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Jeu 2D Canvas Démo", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Rendu matériel à 60 FPS", color = Color(0xFF38BDF8), fontSize = 13.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Score de la session :", color = Color(0xFF94A3B8))
                Text("$score", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color(0xFF22C55E))
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        score += 15
                        clicks++
                        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
                        onLogEvent(
                            LogcatMessage(
                                timestamp = time,
                                tag = "GameEngine",
                                level = LogLevel.DEBUG,
                                message = "Collision hit registered, score=$score, frame=60fps"
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                ) {
                    Text("Toucher la cible néon", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun NotesSandboxView(onLogEvent: (LogcatMessage) -> Unit) {
    var tasks by remember {
        mutableStateOf(
            listOf(
                "Installer l'APK Android Studio" to true,
                "Créer un nouveau projet Compose" to true,
                "Compiler sans connexion Internet" to true,
                "Partager l'APK sur mon téléphone" to false
            )
        )
    }
    var newText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("Mes Tâches & Projets", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newText,
                onValueChange = { newText = it },
                label = { Text("Nouvelle tâche...") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (newText.isNotBlank()) {
                        tasks = tasks + (newText.trim() to false)
                        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
                        onLogEvent(
                            LogcatMessage(
                                timestamp = time,
                                tag = "TodoManager",
                                level = LogLevel.INFO,
                                message = "Added new task: '$newText'"
                            )
                        )
                        newText = ""
                    }
                }
            ) {
                Text("Ajouter")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        tasks.forEachIndexed { index, (task, isChecked) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            tasks = tasks.mapIndexed { idx, pair ->
                                if (idx == index) pair.copy(second = checked) else pair
                            }
                        }
                    )
                    Text(
                        text = task,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
