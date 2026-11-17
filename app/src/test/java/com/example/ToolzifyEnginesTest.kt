package com.example

import com.example.domain.ExpressionEvaluator
import com.example.domain.IslamicAstroEngine
import com.example.domain.PrayerCalcMethod
import com.example.model.ToolRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

class ToolzifyEnginesTest {

    @Test
    fun `expression evaluator handles standard and scientific math`() {
        assertEquals(14.0, ExpressionEvaluator.evaluate("2+3×4").getOrThrow(), 0.0001)
        assertEquals(5.0, ExpressionEvaluator.evaluate("√(9+16)").getOrThrow(), 0.0001)
        assertEquals(0.5, ExpressionEvaluator.evaluate("sin(30)", useDegrees = true).getOrThrow(), 0.0001)
        assertEquals(120.0, ExpressionEvaluator.evaluate("5!").getOrThrow(), 0.0001)
        assertTrue(ExpressionEvaluator.evaluate("10÷0").isFailure)
    }

    @Test
    fun `islamic astro engine computes valid prayer times and qibla bearing`() {
        val makkah = IslamicAstroEngine.findCity("makkah")
        val times = IslamicAstroEngine.calculatePrayerTimes(
            LocalDate.of(2026, 3, 15),
            makkah.latitude,
            makkah.longitude,
            makkah.utcOffsetHours,
            PrayerCalcMethod.MAKKAH
        )
        assertTrue(times.fajr.contains("AM"))
        assertTrue(times.maghrib.contains("PM"))

        val nyc = IslamicAstroEngine.findCity("new_york")
        val qibla = IslamicAstroEngine.calculateQiblaBearing(nyc.latitude, nyc.longitude)
        assertTrue(qibla in 50.0..65.0)
    }

    @Test
    fun `hijri date conversion works offline via java time`() {
        val hijri = HijrahDate.from(LocalDate.of(2026, 2, 18))
        assertTrue(hijri.get(ChronoField.YEAR) >= 1447)
    }

    @Test
    fun `tool registry has all 39 tools with unique ids`() {
        val ids = ToolRegistry.allTools.map { it.id }
        assertEquals(39, ids.size)
        assertEquals(39, ids.toSet().size)
    }
}
