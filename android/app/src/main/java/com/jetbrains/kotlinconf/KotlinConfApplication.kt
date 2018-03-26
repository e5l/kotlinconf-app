package org.jetbrains.kotlinconf

import android.app.Application
import kotlinx.coroutines.experimental.*
import org.jetbrains.kotlinconf.model.KotlinConfDataRepository
import kotlinx.coroutines.experimental.android.UI
import org.jetbrains.anko.*
import org.jetbrains.kotlinconf.api.*
import java.util.*

class KotlinConfApplication : Application(), AnkoLogger {
    val repository: KotlinConfDataRepository = KotlinConfDataRepository(this)

    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler { t, cause ->
            println("APPLICATION ERROR: $cause")
            cause.printStackTrace()
        }

        val userId = getUserId()
        repository.userId = userId
        repository.onError = { action ->
            when (action) {
                KotlinConfDataRepository.Error.FAILED_TO_DELETE_RATING ->
                    toast(R.string.msg_failed_to_delete_vote)

                KotlinConfDataRepository.Error.FAILED_TO_POST_RATING ->
                    toast(R.string.msg_failed_to_post_vote)

                KotlinConfDataRepository.Error.FAILED_TO_GET_DATA ->
                    toast(R.string.msg_failed_to_get_data)

                KotlinConfDataRepository.Error.EARLY_TO_VOTE ->
                    toast(R.string.msg_early_vote)

                KotlinConfDataRepository.Error.LATE_TO_VOTE ->
                    toast(R.string.msg_late_vote)
            }
        }

        launch(UI) {
            val dataLoaded = repository.loadLocalData()
            if (!dataLoaded) {
                repository.update()
            }

            val userIsNew = withContext(CommonPool) {
                KonfRest.createUser(userId)
            }

            // Get new data from server if new user was created (server db was cleaned)
            if (userIsNew && dataLoaded) {
                repository.update()
            }
        }
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