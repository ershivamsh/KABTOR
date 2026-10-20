package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.sortai.model.EWasteCategory
import com.example.sortai.model.HazardLevel
import com.example.sortai.model.ScanMode
import com.example.sortai.service.OfflineVisionClassifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SortAI", appName)
    }

    @Test
    fun `test swollen battery triggers critical fire hazard alert`() {
        val result = OfflineVisionClassifier.classify("swollen bloated battery cell", ScanMode.SEPARATED_COMPONENT)
        assertEquals(EWasteCategory.BATTERY, result.category)
        assertTrue(result.isHazard)
        assertEquals(HazardLevel.CRITICAL, result.hazardLevel)
        assertEquals(2, result.category.crateNumber)
    }

    @Test
    fun `test pcb motherboard maps to green Crate 1`() {
        val result = OfflineVisionClassifier.classify("laptop motherboard pcb chip", ScanMode.SEPARATED_COMPONENT)
        assertEquals(EWasteCategory.PCB, result.category)
        assertEquals(1, result.category.crateNumber)
        assertTrue(result.confidence >= 0.90f)
    }

    @Test
    fun `test copper induction motor maps to orange Crate 3`() {
        val result = OfflineVisionClassifier.classify("electric motor copper windings stator", ScanMode.SEPARATED_COMPONENT)
        assertEquals(EWasteCategory.COPPER_MOTOR, result.category)
        assertEquals(3, result.category.crateNumber)
    }

    @Test
    fun `test crt monitor maps to blue Crate 5 with lead warning`() {
        val result = OfflineVisionClassifier.classify("old crt display monitor", ScanMode.WHOLE_DEVICE)
        assertEquals(EWasteCategory.DISPLAY, result.category)
        assertEquals(5, result.category.crateNumber)
        assertTrue(result.isHazard)
    }
}
