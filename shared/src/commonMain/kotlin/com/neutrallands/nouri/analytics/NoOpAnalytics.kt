package com.neutrallands.nouri.analytics

object NoOpAnalytics : NouriAnalytics, CrashReporter {
    override fun logSignUp(role: String) = Unit

    override fun logLogin() = Unit

    override fun logMealLogged(
        slot: String,
        status: String,
    ) = Unit

    override fun logPlanDownloaded() = Unit

    override fun logAppointmentCreated() = Unit

    override fun logAppointmentRescheduled() = Unit

    override fun logAppointmentCancelled() = Unit

    override fun logMeasurementEntered() = Unit

    override fun logSubscriptionPlanViewed(planName: String) = Unit

    override fun setUserId(userId: String) = Unit

    override fun recordException(throwable: Throwable) = Unit
}
