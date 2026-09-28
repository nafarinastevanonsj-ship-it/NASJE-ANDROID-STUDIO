package com.example.compiler

import com.example.data.model.FileType
import com.example.data.model.ProjectFileEntity
import com.example.data.model.ProjectTemplate

object CodeTemplates {

    fun generateInitialFiles(
        projectId: Long,
        projectName: String,
        packageName: String,
        template: ProjectTemplate
    ): List<ProjectFileEntity> {
        val packagePath = packageName.replace('.', '/')
        val files = mutableListOf<ProjectFileEntity>()

        // 1. AndroidManifest.xml
        val manifestContent = """
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="$packageName">

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.App">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
        """.trimIndent()

        files.add(
            ProjectFileEntity(
                projectId = projectId,
                filePath = "app/src/main/AndroidManifest.xml",
                fileName = "AndroidManifest.xml",
                fileType = FileType.XML.name,
                content = manifestContent
            )
        )

        // 2. build.gradle.kts (App level)
        val buildGradleContent = """
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "$packageName"
    compileSdk = 35

    defaultConfig {
        applicationId = "$packageName"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
}
        """.trimIndent()

        files.add(
            ProjectFileEntity(
                projectId = projectId,
                filePath = "app/build.gradle.kts",
                fileName = "build.gradle.kts",
                fileType = FileType.GRADLE.name,
                content = buildGradleContent
            )
        )

        // 3. settings.gradle.kts
        val settingsGradleContent = """
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "$projectName"
include(":app")
        """.trimIndent()

        files.add(
            ProjectFileEntity(
                projectId = projectId,
                filePath = "settings.gradle.kts",
                fileName = "settings.gradle.kts",
                fileType = FileType.GRADLE.name,
                content = settingsGradleContent
            )
        )

        // 4. strings.xml
        val stringsContent = """
<resources>
    <string name="app_name">$projectName</string>
    <string name="welcome_message">Bienvenue dans $projectName !</string>
    <string name="action_click">Cliquez ici</string>
    <string name="status_ready">Prêt hors-ligne</string>
</resources>
        """.trimIndent()

        files.add(
            ProjectFileEntity(
                projectId = projectId,
                filePath = "app/src/main/res/values/strings.xml",
                fileName = "strings.xml",
                fileType = FileType.XML.name,
                content = stringsContent
            )
        )

        // 5. colors.xml
        val colorsContent = """
<resources>
    <color name="primary">#3574F0</color>
    <color name="primary_container">#1A3673</color>
    <color name="secondary">#388E3C</color>
    <color name="background">#1E1F22</color>
    <color name="surface">#2B2D30</color>
</resources>
        """.trimIndent()

        files.add(
            ProjectFileEntity(
                projectId = projectId,
                filePath = "app/src/main/res/values/colors.xml",
                fileName = "colors.xml",
                fileType = FileType.XML.name,
                content = colorsContent
            )
        )

        // 6. Template specific files
        when (template) {
            ProjectTemplate.COMPOSE_EMPTY -> {
                val mainActivityContent = """
package $packageName

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen(appName = "$projectName")
            }
        }
    }
}

@Composable
fun MainScreen(appName: String) {
    var counter by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf("Compilation réussie !") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = appName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Compilé 100% hors-ligne dans Android Studio",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Compteur de clics :",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "${'$'}counter",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        counter++
                        message = "Incrémenté : ${'$'}counter"
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Incrémenter")
                }

                FilledTonalButton(
                    onClick = {
                        counter = 0
                        message = "Remis à zéro"
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Réinitialiser")
                }
            }
        }
    }
}
                """.trimIndent()

                files.add(
                    ProjectFileEntity(
                        projectId = projectId,
                        filePath = "app/src/main/java/$packagePath/MainActivity.kt",
                        fileName = "MainActivity.kt",
                        fileType = FileType.KOTLIN.name,
                        content = mainActivityContent
                    )
                )
            }

            ProjectTemplate.CLASSIC_VIEWS -> {
                val layoutXmlContent = """
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="24dp"
    android:gravity="center"
    android:background="#121212">

    <TextView
        android:id="@+id/tvTitle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/app_name"
        android:textSize="26sp"
        android:textStyle="bold"
        android:textColor="#FFFFFF" />

    <TextView
        android:id="@+id/tvSubtitle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Mode XML Views classique"
        android:textSize="14sp"
        android:textColor="#A0A0A0"
        android:layout_marginTop="8dp" />

    <EditText
        android:id="@+id/etUserInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Entrez votre texte..."
        android:textColor="#FFFFFF"
        android:textColorHint="#757575"
        android:layout_marginTop="24dp"
        android:background="#1E1E1E"
        android:padding="14dp" />

    <Button
        android:id="@+id/btnSubmit"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Valider l\'entrée"
        android:layout_marginTop="16dp"
        android:backgroundTint="#3574F0" />

    <TextView
        android:id="@+id/tvResult"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="En attente de saisie..."
        android:textSize="16sp"
        android:textColor="#4CAF50"
        android:layout_marginTop="20dp" />

</LinearLayout>
                """.trimIndent()

                files.add(
                    ProjectFileEntity(
                        projectId = projectId,
                        filePath = "app/src/main/res/layout/activity_main.xml",
                        fileName = "activity_main.xml",
                        fileType = FileType.XML.name,
                        content = layoutXmlContent
                    )
                )

                val mainActivityContent = """
package $packageName

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvResult: TextView
    private lateinit var etUserInput: EditText
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvResult = findViewById(R.id.tvResult)
        etUserInput = findViewById(R.id.etUserInput)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            val text = etUserInput.text.toString().trim()
            if (text.isNotEmpty()) {
                tvResult.text = "Texte reçu : ${'$'}text"
            } else {
                tvResult.text = "Veuillez entrer du texte !"
            }
        }
    }
}
                """.trimIndent()

                files.add(
                    ProjectFileEntity(
                        projectId = projectId,
                        filePath = "app/src/main/java/$packagePath/MainActivity.kt",
                        fileName = "MainActivity.kt",
                        fileType = FileType.KOTLIN.name,
                        content = mainActivityContent
                    )
                )
            }

            ProjectTemplate.CALCULATOR_APP -> {
                val calcContent = """
package $packageName

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CalculatorScreen()
            }
        }
    }
}

@Composable
fun CalculatorScreen() {
    var display by remember { mutableStateOf("0") }
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var operator by remember { mutableStateOf<String?>(null) }
    var isNewOperation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF17171C))
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = display,
            fontSize = 52.sp,
            fontWeight = FontWeight.Light,
            color = Color.White,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
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
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { label ->
                    val isWide = label == "0"
                    val weight = if (isWide) 2f else 1f
                    val isOp = label in listOf("/", "*", "-", "+", "=")
                    val isSpecial = label in listOf("C", "+/-", "%")

                    val btnColor = when {
                        isOp -> Color(0xFF4B5EFC)
                        isSpecial -> Color(0xFF4E505F)
                        else -> Color(0xFF2E303C)
                    }

                    Button(
                        onClick = {
                            when (label) {
                                "C" -> {
                                    display = "0"
                                    operand1 = null
                                    operator = null
                                    isNewOperation = false
                                }
                                "+", "-", "*", "/" -> {
                                    operand1 = display.toDoubleOrNull()
                                    operator = label
                                    isNewOperation = true
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
                                        isNewOperation = true
                                    }
                                }
                                else -> {
                                    if (display == "0" || isNewOperation) {
                                        display = label
                                        isNewOperation = false
                                    } else {
                                        display += label
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(weight)
                            .height(64.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = btnColor)
                    ) {
                        Text(
                            text = label,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
                """.trimIndent()

                files.add(
                    ProjectFileEntity(
                        projectId = projectId,
                        filePath = "app/src/main/java/$packagePath/MainActivity.kt",
                        fileName = "MainActivity.kt",
                        fileType = FileType.KOTLIN.name,
                        content = calcContent
                    )
                )
            }

            ProjectTemplate.CANVAS_GAME -> {
                val gameContent = """
package $packageName

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ArcadeGameScreen()
            }
        }
    }
}

@Composable
fun ArcadeGameScreen() {
    var paddleX by remember { mutableStateOf(300f) }
    var ballX by remember { mutableStateOf(300f) }
    var ballY by remember { mutableStateOf(300f) }
    var ballVx by remember { mutableStateOf(7f) }
    var ballVy by remember { mutableStateOf(9f) }
    var score by remember { mutableStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }

    LaunchedEffect(isGameOver) {
        while (!isGameOver) {
            delay(16L) // ~60 FPS
            ballX += ballVx
            ballY += ballVy

            // Rebond murs latéraux
            if (ballX <= 20f || ballX >= 680f) {
                ballVx = -ballVx
            }
            // Rebond haut
            if (ballY <= 20f) {
                ballVy = -ballVy
            }
            // Rebond raquette
            if (ballY >= 880f && ballY <= 910f && ballX >= paddleX - 70f && ballX <= paddleX + 70f) {
                ballVy = -ballVy * 1.05f
                score += 10
            } else if (ballY > 960f) {
                isGameOver = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    paddleX = (paddleX + change.position.x - 300f).coerceIn(80f, 620f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Fond d'espace
            drawRect(color = Color(0xFF0F172A), size = size)

            // Balle néon
            drawCircle(
                color = Color(0xFF38BDF8),
                radius = 16f,
                center = Offset(ballX, ballY)
            )

            // Raquette
            drawRoundRect(
                color = Color(0xFF22C55E),
                topLeft = Offset(paddleX - 60f, 900f),
                size = Size(120f, 22f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Score : ${'$'}score",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (isGameOver) {
                Button(
                    onClick = {
                        ballX = 300f
                        ballY = 300f
                        ballVx = 7f
                        ballVy = 9f
                        score = 0
                        isGameOver = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Rejouer")
                }
            }
        }
    }
}
                """.trimIndent()

                files.add(
                    ProjectFileEntity(
                        projectId = projectId,
                        filePath = "app/src/main/java/$packagePath/MainActivity.kt",
                        fileName = "MainActivity.kt",
                        fileType = FileType.KOTLIN.name,
                        content = gameContent
                    )
                )
            }

            ProjectTemplate.NOTES_TODO_APP -> {
                val notesContent = """
package $packageName

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                NotesApp()
            }
        }
    }
}

data class TaskItem(val id: Long, val title: String, val isCompleted: Boolean)

@Composable
fun NotesApp() {
    var tasks by remember {
        mutableStateOf(
            listOf(
                TaskItem(1, "Télécharger Android Studio APK", true),
                TaskItem(2, "Compiler un projet 100% hors-ligne", true),
                TaskItem(3, "Tester dans l'émulateur Pixel 8", false)
            )
        )
    }
    var newText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Tâches & Notes Hors-ligne") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newText,
                    onValueChange = { newText = it },
                    label = { Text("Nouvelle tâche...") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (newText.isNotBlank()) {
                            tasks = tasks + TaskItem(System.currentTimeMillis(), newText.trim(), false)
                            newText = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { checked ->
                                    tasks = tasks.map {
                                        if (it.id == task.id) it.copy(isCompleted = checked) else it
                                    }
                                }
                            )
                            Text(
                                text = task.title,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            IconButton(
                                onClick = {
                                    tasks = tasks.filter { it.id != task.id }
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                            }
                        }
                    }
                }
            }
        }
    }
}
                """.trimIndent()

                files.add(
                    ProjectFileEntity(
                        projectId = projectId,
                        filePath = "app/src/main/java/$packagePath/MainActivity.kt",
                        fileName = "MainActivity.kt",
                        fileType = FileType.KOTLIN.name,
                        content = notesContent
                    )
                )
            }
        }

        return files
    }
}
