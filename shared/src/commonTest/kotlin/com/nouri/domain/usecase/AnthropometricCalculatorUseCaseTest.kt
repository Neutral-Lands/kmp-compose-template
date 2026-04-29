package com.nouri.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AnthropometricCalculatorUseCaseTest {
    private val useCase = AnthropometricCalculatorUseCase()

    // region BMI calculation

    @Test
    fun `given 70kg 175cm when calculateBmi then returns 22_86`() {
        val bmi = useCase.calculateBmi(weightKg = 70f, heightCm = 175f)
        assertEquals(22.86, bmi.toDouble(), absoluteTolerance = 0.01)
    }

    @Test
    fun `given 55kg 160cm when calculateBmi then returns 21_48`() {
        val bmi = useCase.calculateBmi(weightKg = 55f, heightCm = 160f)
        assertEquals(21.48, bmi.toDouble(), absoluteTolerance = 0.01)
    }

    @Test
    fun `given zero weight when calculateBmi then throws`() {
        assertFailsWith<IllegalArgumentException> {
            useCase.calculateBmi(weightKg = 0f, heightCm = 175f)
        }
    }

    @Test
    fun `given negative height when calculateBmi then throws`() {
        assertFailsWith<IllegalArgumentException> {
            useCase.calculateBmi(weightKg = 70f, heightCm = -1f)
        }
    }

    // endregion

    // region BMI category

    @Test
    fun `given bmi 17 when bmiCategory then UNDERWEIGHT`() {
        assertEquals(BmiCategory.UNDERWEIGHT, useCase.bmiCategory(17f))
    }

    @Test
    fun `given bmi 22 when bmiCategory then NORMAL`() {
        assertEquals(BmiCategory.NORMAL, useCase.bmiCategory(22f))
    }

    @Test
    fun `given bmi 27 when bmiCategory then OVERWEIGHT`() {
        assertEquals(BmiCategory.OVERWEIGHT, useCase.bmiCategory(27f))
    }

    @Test
    fun `given bmi 35 when bmiCategory then OBESE`() {
        assertEquals(BmiCategory.OBESE, useCase.bmiCategory(35f))
    }

    // endregion

    // region Boundary values

    @Test
    fun `given bmi exactly 18_5 when bmiCategory then NORMAL`() {
        assertEquals(BmiCategory.NORMAL, useCase.bmiCategory(18.5f))
    }

    @Test
    fun `given bmi exactly 25 when bmiCategory then OVERWEIGHT`() {
        assertEquals(BmiCategory.OVERWEIGHT, useCase.bmiCategory(25.0f))
    }

    @Test
    fun `given bmi exactly 30 when bmiCategory then OBESE`() {
        assertEquals(BmiCategory.OBESE, useCase.bmiCategory(30.0f))
    }

    // endregion
}
