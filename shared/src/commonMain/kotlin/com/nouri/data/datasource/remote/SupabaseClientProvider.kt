package com.nouri.data.datasource.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

fun provideSupabaseClient(
    url: String,
    anonKey: String,
) = createSupabaseClient(
    supabaseUrl = url,
    supabaseKey = anonKey,
) {
    install(Auth) {
        autoLoadFromStorage = false
        enableLifecycleCallbacks = false
    }
    install(Postgrest)
    install(Storage)
    install(Realtime)
}
