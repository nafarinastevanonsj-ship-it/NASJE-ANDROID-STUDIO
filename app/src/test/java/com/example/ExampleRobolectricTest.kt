package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.compiler.CodeTemplates
import com.example.compiler.OfflineCompilerEngine
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectTemplate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Android Studio", appName)
    }

    @Test
    fun `test offline compiler produces real apk`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val project = ProjectEntity(
            id = 1,
            name = "TestApp",
            packageName = "com.test.app",
            templateType = ProjectTemplate.COMPOSE_EMPTY.name
        )
        val files = CodeTemplates.generateInitialFiles(1, "TestApp", "com.test.app", ProjectTemplate.COMPOSE_EMPTY)

        val result = OfflineCompilerEngine.compileProject(
            context = context,
            project = project,
            files = files,
            buildVariant = "debug"
        )

        assertTrue("Compilation should succeed offline", result.isSuccess)
        assertNotNull("APK file should be created", result.apkFile)
        assertTrue("APK file should exist on disk", result.apkFile?.exists() == true)
        assertTrue("APK should have positive size", (result.apkFile?.length() ?: 0) > 0)
    }
}
