package org.jetbrains.kotlinconf.model

import android.arch.lifecycle.*
import android.content.*
import android.content.Context.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.*
import kotlinx.serialization.json.*
import org.jetbrains.anko.*
import org.jetbrains.kotlinconf.*
import org.jetbrains.kotlinconf.api.*
import org.jetbrains.kotlinconf.data.*
import java.io.*

const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

class KotlinConfDataRepository(private val context: Context) : AnkoLogger {

    lateinit var userId: String
    var onError: ((action: Error) -> Unit)? = null

    private val kjson: JSON by lazy {
        JSON(nonstrict = true)
    }

    private val kotlinConfApi: KonfRest by lazy { KonfRest(userId) }

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

    suspend fun setFavorite(sessionId: String, isFavorite: Boolean) {
        if (isFavorite) addLocalFavorite(sessionId) else deleteLocalFavorite(sessionId)

        withContext(CommonPool) {
            if (isFavorite) kotlinConfApi.postFavorite(Favorite(sessionId))
            else kotlinConfApi.deleteFavorite(Favorite(sessionId))
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

    suspend fun addRating(sessionId: String, rating: SessionRating) {
        _ratings.value = getAllLocalRatings() + (sessionId to rating)

        withContext(CommonPool) {
            try {
                kotlinConfApi.postVote(Vote(sessionId = sessionId, rating = rating.value))
                withContext(UI) { saveLocalRating(sessionId, rating) }
            } catch (cause: ApiException) {
                withContext(UI) { _ratings.value = getAllLocalRatings()
                when (cause.response.statusCode) {
                    HTTP_COME_BACK_LATER -> onError?.invoke(Error.EARLY_TO_VOTE)
                    HTTP_TOO_LATE -> onError?.invoke(Error.LATE_TO_VOTE)
                    else -> onError?.invoke(Error.FAILED_TO_POST_RATING)
                }
                }
            } catch (cause: Throwable) {
                withContext(UI) {
                    onError?.invoke(Error.FAILED_TO_POST_RATING)
                }
            }
        }
    }

    suspend fun removeRating(sessionId: String) {
        _ratings.value = getAllLocalRatings() - sessionId
        withContext(CommonPool) {
            try {
                kotlinConfApi.deleteVote(Vote(sessionId = sessionId))
                withContext(UI) { deleteLocalRating(sessionId) }
            } catch (cause: Throwable) {
                _ratings.value = getAllLocalRatings()
                onError?.invoke(Error.FAILED_TO_DELETE_RATING)
            }
        }
    }

    private fun syncLocalFavorites(allData: AllData) {
        val sessionIds = allData.favorites?.map { it.sessionId } ?: return
        val favorites = favoritePreferences.getStringSet(FAVORITES_KEY, mutableSetOf()).toMutableSet()

        val missingOnServer = favorites - sessionIds
        launch(CommonPool) {
            missingOnServer.forEach {
                kotlinConfApi.postFavorite(Favorite(it))
            }
        }

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

    private fun updateLocalData(allData: AllData) {
        val allDataFile = File(context.filesDir, CACHED_DATA_FILE_NAME)
        allDataFile.delete()
        allDataFile.createNewFile()
        allDataFile.writeText(kjson.stringify(allData))
        _data.value = allData
    }

    fun loadLocalData(): Boolean {
        val allDataFile = File(context.filesDir, CACHED_DATA_FILE_NAME)
        if (!allDataFile.exists()) {
            return false
        }

        val allData = try {
            kjson.parse<AllData>(allDataFile.readText())
        } catch (cause: Throwable) {
            println(cause)
            return false
        }

        _data.value = allData

        val favorites = favoritePreferences.getStringSet(FAVORITES_KEY, mutableSetOf())
        _favorites.value = sessions.value?.filter { session -> favorites.contains(session.id) }

        _ratings.value = ratingPreferences.all.mapNotNull {
            SessionRating.valueOf(it.value as Int)?.let { rating -> it.key to rating }
        }.toMap()

        return true
    }

    suspend fun update() {
        if (_isUpdating.value == true) {
            return
        }

        _isUpdating.value = true

        try {
            val allData = withContext(CommonPool) {
                kotlinConfApi.getAll()
            }

            syncLocalFavorites(allData)
            syncLocalRatings(allData)
            updateLocalData(allData)
        } catch (cause: Throwable) {
            warn("Failed to get data from server", cause)
            onError?.invoke(Error.FAILED_TO_GET_DATA)
        }
        _isUpdating.value = false
    }

    companion object {
        const val FAVORITES_PREFERENCES_NAME = "favorites"
        const val VOTES_PREFERENCES_NAME = "votes"
        const val FAVORITES_KEY = "favorites"
        const val CACHED_DATA_FILE_NAME = "data.json"

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
