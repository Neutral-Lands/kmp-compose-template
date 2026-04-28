package com.nouri.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionPlan(
    val id: String,
    val name: String,
    @SerialName("max_patients") val maxPatients: Int?,
    @SerialName("price_usd_cents") val priceUsdCents: Int,
    @SerialName("created_at") val createdAt: String? = null
)
