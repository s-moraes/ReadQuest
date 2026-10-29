package com.saulo.readquest.theme

import androidx.compose.ui.unit.sp
import com.saulo.readquest.ui.theme.Typography
import com.saulo.readquest.ui.theme.scaledTypography
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeScalingTest {

    @Test
    fun scaledTypography_defaultScale_returnsIdenticalSizes() {
        val scaled = scaledTypography(Typography, 1.0f)
        assertEquals(Typography.bodyLarge.fontSize, scaled.bodyLarge.fontSize)
        assertEquals(Typography.headlineSmall.fontSize, scaled.headlineSmall.fontSize)
    }

    @Test
    fun scaledTypography_customScale_scalesProportionally() {
        val scale = 1.15f
        val scaled = scaledTypography(Typography, scale)

        val expectedBodyLarge = 16.sp * scale
        val expectedHeadlineSmall = 24.sp * scale

        assertEquals(expectedBodyLarge.value, scaled.bodyLarge.fontSize.value, 0.001f)
        assertEquals(expectedHeadlineSmall.value, scaled.headlineSmall.fontSize.value, 0.001f)
    }

    @Test
    fun scaledTypography_smallScale_scalesDown() {
        val scale = 0.85f
        val scaled = scaledTypography(Typography, scale)

        val expectedBodyLarge = 16.sp * scale
        assertEquals(expectedBodyLarge.value, scaled.bodyLarge.fontSize.value, 0.001f)
    }
}
