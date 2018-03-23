package org.jetbrains.kotlinconf.data

import io.ktor.common.client.*
import org.jetbrains.kotlinconf.*
import org.jetbrains.kotlinconf.api.*
import org.jetbrains.kotlinconf.util.*

class DataRepository(val uuid: String) {
    val api by lazy {
        KotlinConfApi(uuid)
    }

    fun updateSessions(onComplete: () -> Unit) {
        runSuspend {
            val all = api.getAll()
            parseSessions(all)
            onComplete()
        }
    }

    private fun parseSessions(all: AllData) {
        val sessionModels = all.sessions?.map { SessionModel.forSession(all, it.id!!)!! }
        AppContext.allData = all
        AppContext.sessionsModels = sessionModels
        AppContext.localFavorites = all.favorites.orEmpty().toMutableList()
        AppContext.localVotes = all.votes.orEmpty().toMutableList()
    }

    fun updateFavorites(onComplete: () -> Unit = {}) {
        // todo: api.getFavourites
        updateSessions(onComplete)
    }

    fun updateVotes(onComplete: () -> Unit = {}) {
        // todo: api.getVotes
        updateSessions(onComplete)
    }

    fun isFavorite(session: Session) = getFavorite(session) != null

    private fun getFavorite(session: Session): Favorite? {
        return AppContext.localFavorites.firstOrNull { it.sessionId == session.id }
    }

    fun toggleFavorite(
            session: Session,
            onComplete: (Boolean) -> Unit
    ) {
        val newFavorite = !isFavorite(session)

        setLocalFavorite(session, isFavorite = newFavorite)
        onComplete(newFavorite)

        runSuspend {
            val favorite = Favorite(session.id)
            if (newFavorite) {
                api.postFavorite(favorite)
            } else {
                api.deleteFavorite(favorite)
            }
        }
    }

    private fun setLocalFavorite(session: Session, isFavorite: Boolean) {
        val favorite = getFavorite(session)
        if (favorite != null && !isFavorite) {
            AppContext.localFavorites.remove(favorite)
        } else if (isFavorite) {
            AppContext.localFavorites.add(Favorite(session.id))
        }
    }

    fun getFavoriteSessionIds(): List<String> {
        return AppContext.localFavorites.mapNotNull { it.sessionId }
    }

    fun getRating(session: Session): SessionRating? {
        return AppContext.localVotes.firstOrNull { it.sessionId == session.id }?.rating?.let { SessionRating.valueOf(it) }
    }

    fun setRating(
            session: Session,
            rating: SessionRating,
            onError: (Throwable) -> Unit,
            onComplete: (SessionRating?) -> Unit
    ) {
        val currentRating = getRating(session)

        val newRating = if (currentRating == rating) null else rating

        println("rating: $rating, currentRating = $currentRating, newRating = $newRating")

        runSuspend {
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
        AppContext.localVotes.removeAll { it.sessionId == session.id }
        if (rating != null) {
            AppContext.localVotes.add(Vote(session.id, rating.value))
        }
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

        var seq = AppContext.allData?.sessions.orEmpty().asSequence()
        if (mode == SessionsListMode.FAVORITES) {
            val favorites = getFavoriteSessionIds()
            seq = seq.filter { it.id in favorites }
        }

        seq = seq
                .sortedWith(SessionsComparator)
                .sortedBy { it.roomId }
                .sortedBy { it.id }

        _cachedSessions = seq.toList().groupBy { it.startsAt!! }
    }

    val sessions: Map<Date, List<Session>>
        get() = _cachedSessions.orEmpty()

    val hasModels: Boolean
        get() = (AppContext.sessionsModels?.size ?: 0) > 0

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
