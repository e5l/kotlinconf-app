package org.jetbrains.kotlinconf.api

import org.jetbrains.kotlinconf.*
import org.jetbrains.kotlinconf.data.*
import org.jetbrains.kotlinconf.util.*
import kotlin.*

class DataRepository(val uuid: String) {
    private var data = AllData()

    private var localFavorites: MutableList<Favorite> = mutableListOf()
    private var localVotes: MutableList<Vote> = mutableListOf()
    private var sessionModels: List<SessionModel>? = null

    private val api = KotlinConfApi(uuid)

    fun updateSessions(
            onComplete: () -> Unit,
            onError: (cause: Throwable) -> Unit
    ): Unit = konfAsync {
        data = api.getAll()
        syncState()
    }.then { onComplete() }.catch(onError)

    fun updateFavorites(onComplete: () -> Unit = {}) {
    }

    fun updateVotes(onComplete: () -> Unit = {}) {
    }

    fun isFavorite(session: Session) = getFavorite(session) != null

    private fun getFavorite(session: Session): Favorite? {
        return localFavorites.firstOrNull { it.sessionId == session.id }
    }

    fun toggleFavorite(
            session: Session,
            onComplete: (Boolean) -> Unit,
            onError: (cause: Throwable) -> Unit
    ) {
        val newFavorite = !isFavorite(session)

        setLocalFavorite(session, isFavorite = newFavorite)
        onComplete(newFavorite)

        konfAsync {
            val favorite = Favorite(session.id)
            if (newFavorite) api.postFavorite(favorite) else api.deleteFavorite(favorite)
        }
    }

    private fun syncState() = with(data) {
        sessionModels = sessions?.map { SessionModel.forSession(data, it.id!!)!! }
        localFavorites = favorites.orEmpty().toMutableList()
        localVotes = votes.orEmpty().toMutableList()
    }

    private fun setLocalFavorite(session: Session, isFavorite: Boolean) {
        val favorite = getFavorite(session)
        if (favorite != null && !isFavorite) {
            localFavorites.remove(favorite)
        } else if (isFavorite) {
            localFavorites.add(Favorite(session.id))
        }
    }

    fun getFavoriteSessionIds(): List<String> = localFavorites.mapNotNull { it.sessionId }

    fun getRating(session: Session): SessionRating? =
            localVotes.firstOrNull { it.sessionId == session.id }?.rating?.let { SessionRating.valueOf(it) }

    fun setRating(
            session: Session,
            rating: SessionRating,
            onError: (Throwable) -> Unit,
            onComplete: (SessionRating?) -> Unit
    ) {
        val currentRating = getRating(session)

        val newRating = if (currentRating == rating) null else rating

        println("rating: $rating, currentRating = $currentRating, newRating = $newRating")

        konfAsync {
            try {
                val vote = Vote(session.id, rating.value)
                if (newRating != null) api.postVote(vote) else api.deleteVote(vote)
                setLocalRating(session, rating = newRating)
                onComplete(newRating)
            } catch (cause: ApiException) {
                val code = VoteActionResult.fromCode(cause.response.statusCode)
                when (code) {
                    VoteActionResult.TOO_EARLY -> onError(EARLY_SUBMITTION_ERROR)
                    VoteActionResult.TOO_LATE -> onError(LATE_SUBMITTION_ERROR)
                    else -> onError(cause)
                }
            } catch (cause: Throwable) {
                onError(cause)
            }
        }
    }

    private fun setLocalRating(session: Session, rating: SessionRating?) {
        localVotes.removeAll { it.sessionId == session.id }
        rating?.let { localVotes.add(Vote(session.id, it.value)) }
    }

    fun shouldShowBadge(onComplete: (Boolean) -> Unit) {
        // todo: what is this?
        onComplete(false)
    }

    private var _cachedSessions: Map<Date, List<Session>>? = null
        set(value) {
            field = value
            _cachedSessionsList = value!!.toList()
        }

    private var _cachedSessionsList: List<Pair<Date, List<Session>>>? = null

    fun fetchSessions(mode: SessionsListMode = SessionsListMode.ALL) {
        var seq = data.sessions.orEmpty().asSequence()
        if (mode == SessionsListMode.FAVORITES) {
            val favorites = getFavoriteSessionIds()
            seq = seq.filter { it.id in favorites }
        }

        seq = seq.sortedWith(SessionsComparator)
                .sortedBy { it.roomId }
                .sortedBy { it.id }

        _cachedSessions = seq.toList().groupBy { it.startsAt!! }
    }

    val sessions: Map<Date, List<Session>>
        get() = _cachedSessions.orEmpty()

    val hasModels: Boolean
        get() = (sessionModels?.size ?: 0) > 0

    fun getSession(bucket: Int, idx: Int) = _cachedSessionsList?.getOrNull(bucket)?.second?.getOrNull(idx)

    fun getSessionBucketSize(bucket: Int) = _cachedSessionsList?.getOrNull(bucket)?.second?.size ?: 0

    companion object {
        private val kHTTPStatusCodeComeBackLater = 477
        private val kHTTPStatusCodeTooLate = 478

        val EARLY_SUBMITTION_ERROR = RuntimeException("VotesManager")
        val LATE_SUBMITTION_ERROR = RuntimeException("VotesManager")
    }

    enum class VoteActionResult {
        OK, TOO_EARLY, TOO_LATE;

        companion object {
            fun fromCode(statusCode: Int) = when (statusCode) {
                kHTTPStatusCodeTooLate -> TOO_LATE
                kHTTPStatusCodeComeBackLater -> TOO_EARLY
                else -> OK
            }
        }
    }

    enum class SessionsListMode {
        ALL, FAVORITES
    }
}
