package org.jetbrains.kotlinconf

import android.app.*
import org.jetbrains.anko.*
import org.jetbrains.kotlinconf.model.*
import java.util.*

class KotlinConfApplication : Application(), AnkoLogger {
    lateinit var repository: KotlinConfDataRepository

    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler { t, cause ->
            println("APPLICATION ERROR: $cause")
            cause.printStackTrace()
        }

        val userId = getUserId()
        repository = KotlinConfDataRepository(this, userId)
    }

    private fun getUserId(): String {
        defaultSharedPreferences.getString(USER_ID_KEY, null)?.let {
            return it
        }

        val userId = "android-" + UUID.randomUUID().toString()
        defaultSharedPreferences
                .edit()
                .putString(USER_ID_KEY, userId)
                .apply()

        return userId
    }

    companion object {
        const val USER_ID_KEY = "UserId"
    }
}