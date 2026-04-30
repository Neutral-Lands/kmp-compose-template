package com.nouri.android.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nouri.analytics.CrashReporter
import com.nouri.analytics.NouriAnalytics

class FirebaseNouriAnalytics(
    context: Context,
    private val isDebug: Boolean,
) : NouriAnalytics,
    CrashReporter {
    private val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    override fun logSignUp(role: String) {
        if (isDebug) return
        analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, Bundle().apply { putString("role", role) })
    }

    override fun logLogin() {
        if (isDebug) return
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, null)
    }

    override fun logMealLogged(
        slot: String,
        status: String,
    ) {
        if (isDebug) return
        analytics.logEvent(
            "meal_logged",
            Bundle().apply {
                putString("slot", slot)
                putString("status", status)
            },
        )
    }

    override fun logPlanDownloaded() {
        if (isDebug) return
        analytics.logEvent("plan_downloaded", null)
    }

    override fun logAppointmentCreated() {
        if (isDebug) return
        analytics.logEvent("appointment_created", null)
    }

    override fun logAppointmentRescheduled() {
        if (isDebug) return
        analytics.logEvent("appointment_rescheduled", null)
    }

    override fun logAppointmentCancelled() {
        if (isDebug) return
        analytics.logEvent("appointment_cancelled", null)
    }

    override fun logMeasurementEntered() {
        if (isDebug) return
        analytics.logEvent("measurement_entered", null)
    }

    override fun logSubscriptionPlanViewed(planName: String) {
        if (isDebug) return
        analytics.logEvent(
            "subscription_plan_viewed",
            Bundle().apply { putString("plan_name", planName) },
        )
    }

    override fun setUserId(userId: String) {
        if (isDebug) return
        analytics.setUserId(userId)
        crashlytics.setUserId(userId)
    }

    override fun recordException(throwable: Throwable) {
        if (isDebug) return
        crashlytics.recordException(throwable)
    }
}
