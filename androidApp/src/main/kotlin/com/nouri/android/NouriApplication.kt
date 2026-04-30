package com.nouri.android

import android.app.Application
import android.util.Log
import com.nouri.android.analytics.FirebaseNouriAnalytics
import com.nouri.data.connectivity.AndroidConnectivityObserver
import com.nouri.data.datasource.remote.SubscriptionPlanDataSource
import com.nouri.data.local.DatabaseDriverFactory
import com.nouri.di.appModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.get

class NouriApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val firebase = FirebaseNouriAnalytics(this, BuildConfig.DEBUG)
        startKoin {
            androidContext(this@NouriApplication)
            modules(
                appModule(
                    supabaseUrl = BuildConfig.SUPABASE_URL,
                    supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY,
                    driverFactory = DatabaseDriverFactory(this@NouriApplication),
                    connectivityObserver = AndroidConnectivityObserver(this@NouriApplication),
                    analytics = firebase,
                    crashReporter = firebase,
                ),
            )
        }
        verifySupabaseConnection()
    }

    private fun verifySupabaseConnection() {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val dataSource: SubscriptionPlanDataSource = get(SubscriptionPlanDataSource::class.java)
                val plans = dataSource.fetchAll()
                Log.d("Nouri", "Supabase OK — ${plans.size} subscription plan(s): ${plans.map { it.name }}")
            }.onFailure { e ->
                Log.e("Nouri", "Supabase connection failed", e)
            }
        }
    }
}
