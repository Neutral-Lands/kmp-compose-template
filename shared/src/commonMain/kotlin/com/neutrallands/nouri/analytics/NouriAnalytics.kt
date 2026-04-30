package com.neutrallands.nouri.analytics

interface NouriAnalytics {
    fun logSignUp(role: String)

    fun logLogin()

    fun logMealLogged(
        slot: String,
        status: String,
    )

    fun logPlanDownloaded()

    fun logAppointmentCreated()

    fun logAppointmentRescheduled()

    fun logAppointmentCancelled()

    fun logMeasurementEntered()

    fun logSubscriptionPlanViewed(planName: String)
}
