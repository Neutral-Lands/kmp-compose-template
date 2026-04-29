package com.nouri.domain.usecase

class AnthropometricCalculatorUseCase {

    /** BMI = weight (kg) / height (m)² — metric only per app convention */
    fun calculateBmi(weightKg: Float, heightCm: Float): Float {
        require(weightKg > 0f) { "Weight must be positive" }
        require(heightCm > 0f) { "Height must be positive" }
        val heightM = heightCm / CM_PER_METER
        return weightKg / (heightM * heightM)
    }

    fun bmiCategory(bmi: Float): BmiCategory = when {
        bmi < BMI_UNDERWEIGHT_THRESHOLD -> BmiCategory.UNDERWEIGHT
        bmi < BMI_OVERWEIGHT_THRESHOLD -> BmiCategory.NORMAL
        bmi < BMI_OBESE_THRESHOLD -> BmiCategory.OVERWEIGHT
        else -> BmiCategory.OBESE
    }

    private companion object {
        const val CM_PER_METER = 100f
        const val BMI_UNDERWEIGHT_THRESHOLD = 18.5f
        const val BMI_OVERWEIGHT_THRESHOLD = 25.0f
        const val BMI_OBESE_THRESHOLD = 30.0f
    }
}

enum class BmiCategory { UNDERWEIGHT, NORMAL, OVERWEIGHT, OBESE }
