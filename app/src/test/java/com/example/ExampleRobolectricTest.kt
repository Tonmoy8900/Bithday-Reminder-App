package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PreloadedRoster
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context and verify preloaded employee roster`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Birthday Pulse", appName)

        val roster = PreloadedRoster.defaultEmployeeRoster("uid_test")
        assertEquals(12, roster.size)
        assertTrue(roster.any { it.name == "Kamalesh Vishwakarma" && it.birthDate == "01-01-1980" })
        assertTrue(roster.any { it.name == "Alip Lodh" && it.birthDate == "01-05-1993" })
    }
}
