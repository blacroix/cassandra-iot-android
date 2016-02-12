package fr.blacroix.cassandra_iot

import android.app.Application
import android.preference.PreferenceManager
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
import java.util.*

class App : Application() {

    companion object {
        val DEVICE_ID_KEY: String = "DEVICE_ID_KEY"
    }

    override fun onCreate() {
        super.onCreate()

        // Set up Crashlytics, disabled for debug builds
        val crashlyticsKit = Crashlytics.Builder().core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build()

        // Initialize Fabric with the debug-disabled crashlytics.
        Fabric.with(this, crashlyticsKit)

        generateUniqueId()
    }

    private fun generateUniqueId() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getString(DEVICE_ID_KEY, null) == null) {
            val editor = preferences.edit()
            editor.putString(DEVICE_ID_KEY, UUID.randomUUID().toString())
            editor.apply()
        }
    }
}
