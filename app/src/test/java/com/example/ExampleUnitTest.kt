package com.example

import com.example.compiler.CodeTemplates
import com.example.data.model.ProjectTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun `test template generation generates all essential files`() {
        val files = CodeTemplates.generateInitialFiles(
            projectId = 99,
            projectName = "DemoOfflineApp",
            packageName = "com.offline.demo",
            template = ProjectTemplate.COMPOSE_EMPTY
        )

        assertTrue(files.any { it.filePath.contains("AndroidManifest.xml") })
        assertTrue(files.any { it.filePath.contains("build.gradle.kts") })
        assertTrue(files.any { it.filePath.contains("MainActivity.kt") })
        assertTrue(files.any { it.filePath.contains("strings.xml") })
    }
}
