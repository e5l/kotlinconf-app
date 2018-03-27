package org.jetbrains.kotlinconf.model

import android.arch.lifecycle.*
import android.content.*
import android.content.Context.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.*
import kotlinx.serialization.json.*
import org.jetbrains.anko.*
import org.jetbrains.kotlinconf.*
import org.jetbrains.kotlinconf.R
import org.jetbrains.kotlinconf.api.*
import org.jetbrains.kotlinconf.data.*
import org.jetbrains.kotlinconf.util.*
import java.io.*

class KotlinConfDataRepository(
        private val context: Context,
        userId: String
) : AnkoLogger {
    private val konfSerivce: KonfService by lazy { KonfService(userId) }

    private val kjson: JSON by lazy {
        JSON(nonstrict = true)
    }

    private val favoritePreferences: SharedPreferences by lazy {
        context.getSharedPreferences(FAVORITES_PREFERENCES_NAME, MODE_PRIVATE)
    }

    private val ratingPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(VOTES_PREFERENCES_NAME, MODE_PRIVATE)
    }

    private val _data: MutableLiveData<AllData> = MutableLiveData()

    private val _isUpdating = MutableLiveData<Boolean>()
    val isUpdating: LiveData<Boolean> = _isUpdating

    val sessions: LiveData<List<SessionModel>> = map(_data) { data ->
        data?.sessions?.mapNotNull {
            createSessionModel(it)
        } ?: emptyList()
    }

    private val _favorites = MediatorLiveData<List<SessionModel>>().apply {
        addSource(sessions) { sessions ->
            val favorites = favoritePreferences.getStringSet(FAVORITES_KEY, emptySet())
            value = sessions?.filter { session -> favorites.contains(session.id) }
        }
    }
    val favorites: LiveData<List<SessionModel>> = _favorites

    private val _ratings: MutableLiveData<Map<String, SessionRating>> = MutableLiveData()
    val ratings: LiveData<Map<String, SessionRating>> = _ratings

    private val onError: (suspend (action: Error) -> Unit) = { action ->
        withContext(UI) {
            when (action) {
                KotlinConfDataRepository.Error.FAILED_TO_DELETE_RATING ->
                    context.toast(R.string.msg_failed_to_delete_vote)

                KotlinConfDataRepository.Error.FAILED_TO_POST_RATING ->
                    context.toast(R.string.msg_failed_to_post_vote)

                KotlinConfDataRepository.Error.FAILED_TO_GET_DATA ->
                    context.toast(R.string.msg_failed_to_get_data)

                KotlinConfDataRepository.Error.EARLY_TO_VOTE ->
                    context.toast(R.string.msg_early_vote)

                KotlinConfDataRepository.Error.LATE_TO_VOTE ->
                    context.toast(R.string.msg_late_vote)
            }
        }
    }

    init {
        launch {
            konfSerivce.register().get()
            update()
        }
    }

    private fun createSessionModel(session: Session): SessionModel? {
        return SessionModel.forSession(session,
                speakerProvider = this::getSpeaker,
                categoryProvider = this::getCategoryItem,
                roomProvider = this::getRoom
        )
    }

    private fun getRoom(roomId: Int): Room? = _data.value?.rooms?.find { it.id == roomId }

    private fun getSpeaker(speakerId: String): Speaker? = _data.value?.speakers?.find { it.id == speakerId }

    private fun getCategoryItem(categoryItemId: Int): CategoryItem? {
        return _data.value?.categories
                ?.flatMap { it.items ?: emptyList() }
                ?.find { it?.id == categoryItemId }
    }

    private fun addLocalFavorite(sessionId: String) {
        val favorites = favoritePreferences.getStringSet(FAVORITES_KEY, setOf()).toMutableSet()
        favorites.add(sessionId)
        favoritePreferences
                .edit()
                .putStringSet(FAVORITES_KEY, favorites)
                .apply()

        _favorites.value = sessions.value?.filter { session -> favorites.contains(session.id) }
    }

    private fun deleteLocalFavorite(sessionId: String) {
        val favorites = favoritePreferences.getStringSet(FAVORITES_KEY, setOf()).toMutableSet()
        favorites.remove(sessionId)
        favoritePreferences
                .edit()
                .putStringSet(FAVORITES_KEY, favorites)
                .apply()

        _favorites.value = sessions.value?.filter { session -> favorites.contains(session.id) }
    }

    suspend fun toggleFavorite(session: SessionModel, isFavorite: Boolean) {
        if (isFavorite) addLocalFavorite(session.id) else deleteLocalFavorite(session.id)

        withContext(CommonPool) {
            konfSerivce.toggleFavorite(session.origin).get()
        }
    }

    private fun getAllLocalRatings(): Map<String, SessionRating> =
            ratingPreferences.all.mapNotNull { entry ->
                SessionRating.valueOf(entry.value as Int)?.let { rating -> entry.key to rating }
            }.toMap()

    private fun saveLocalRating(sessionId: String, rating: SessionRating) {
        ratingPreferences.edit().putInt(sessionId, rating.value).apply()
        _ratings.value = getAllLocalRatings()
    }

    private fun deleteLocalRating(sessionId: String) {
        ratingPreferences.edit().remove(sessionId).apply()
        _ratings.value = getAllLocalRatings()
    }

    suspend fun addRating(session: SessionModel, rating: SessionRating) {
        withContext(CommonPool) {
            try {
                konfSerivce.setRating(session.origin, rating).get()
                saveLocalRating(session.id, rating)
                withContext(UI) { _ratings.value = getAllLocalRatings() + (session.id to rating) }
            } catch (cause: Throwable) {
                when (cause) {
                    KonfService.EARLY_SUBMITTION_ERROR -> onError.invoke(Error.EARLY_TO_VOTE)
                    KonfService.LATE_SUBMITTION_ERROR -> onError.invoke(Error.LATE_TO_VOTE)
                    else -> onError.invoke(Error.FAILED_TO_POST_RATING)
                }
            }
        }
    }

    suspend fun removeRating(session: SessionModel) {
        _ratings.value = getAllLocalRatings() - session.id
        withContext(CommonPool) {
            try {
                konfSerivce.deleteRating(session.origin).get()
                deleteLocalRating(session.id)
            } catch (cause: Throwable) {
                withContext(UI) { _ratings.value = getAllLocalRatings() }
                onError.invoke(Error.FAILED_TO_DELETE_RATING)
            }
        }
    }

    private fun syncLocalFavorites(allData: AllData) {
        val sessionIds = allData.favorites?.map { it.sessionId } ?: return
        val favorites = favoritePreferences.getStringSet(FAVORITES_KEY, mutableSetOf()).toMutableSet()

        favorites.clear()
        favorites.addAll(sessionIds)
        favoritePreferences
                .edit()
                .putStringSet(FAVORITES_KEY, favorites)
                .apply()

        _favorites.value = sessions.value?.filter { session -> favorites.contains(session.id) }
    }

    private fun syncLocalRatings(allData: AllData) {
        val ratings = allData.votes?.mapNotNull {
            val sessionId = it.sessionId
            val rating = it.rating?.let { SessionRating.valueOf(it) }
            if (sessionId != null && rating != null) sessionId to rating else null
        }?.toMap()

        ratingPreferences.edit().apply {
            clear()
            ratings?.forEach { putInt(it.key, it.value.value) }
        }.apply()

        _ratings.value = ratings
    }

    suspend fun update() = withContext(UI) {
        if (_isUpdating.value == true) return@withContext
        _isUpdating.value = true

        try {
            withContext(CommonPool) {
                konfSerivce.refresh().get()
            }

            val allData = konfSerivce.data
            syncLocalFavorites(allData)
            syncLocalRatings(allData)
            _data.value = allData
        } catch (cause: Throwable) {
            warn("Failed to get data from server", cause)
            onError.invoke(Error.FAILED_TO_GET_DATA)
        }

        _isUpdating.value = false
    }

    companion object {
        const val FAVORITES_PREFERENCES_NAME = "favorites"
        const val VOTES_PREFERENCES_NAME = "votes"
        const val FAVORITES_KEY = "favorites"

        const val HTTP_COME_BACK_LATER = 477
        const val HTTP_TOO_LATE = 478
    }

    enum class Error {
        FAILED_TO_POST_RATING,
        FAILED_TO_DELETE_RATING,
        FAILED_TO_GET_DATA,
        EARLY_TO_VOTE,
        LATE_TO_VOTE
    }
}
