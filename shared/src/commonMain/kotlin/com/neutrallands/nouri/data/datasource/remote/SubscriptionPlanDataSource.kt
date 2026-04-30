package com.neutrallands.nouri.data.datasource.remote

import com.neutrallands.nouri.data.model.SubscriptionPlan
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class SubscriptionPlanDataSource(
    private val supabase: SupabaseClient,
) {
    suspend fun fetchAll(): List<SubscriptionPlan> = supabase.from("subscription_plans").select().decodeList()
}
