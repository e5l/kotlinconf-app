package org.jetbrains.kotlinconf.api

import org.jetbrains.kotlinconf.*
import org.jetbrains.kotlinconf.data.*
import org.jetbrains.kotlinconf.util.*
import kotlin.*

class KonfService(private val uuid: String) {
    private val api = KonfRest(uuid)

    var data = AllData()
        private set

    var favorites: MutableList<Favorite> = mutableListOf()
        private set

    private var localVotes: MutableList<Vote> = mutableListOf()

    var sessions: List<Session> = listOf()
        private set

    var sessionsModels: Map<String, SessionModel> = mapOf()
        private set

    fun register(): KonfPromise<Boolean> = konfAsync {
        KonfRest.createUser(uuid)
    }

    fun refresh(): KonfPromise<Unit> = konfAsync {
        data = api.getAll()
        syncState()
    }

    fun isFavorite(session: Session) = getFavorite(session) != null

    fun toggleFavorite(session: Session): KonfPromise<Boolean> {
        val newFavorite = !isFavorite(session)
        setFavoriteState(session, isFavorite = newFavorite)

        return konfAsync {
            val favorite = Favorite(session.id)
            if (newFavorite) api.postFavorite(favorite) else api.deleteFavorite(favorite)
            return@konfAsync newFavorite
        }
    }

    private fun syncState() {
        sessions = data.sessions ?: emptyList()
        sessionsModels = data.sessions?.map { it.id!! to SessionModel.forSession(data, it.id)!! }?.toMap() ?: emptyMap()
        favorites = data.favorites.orEmpty().toMutableList()
        localVotes = data.votes.orEmpty().toMutableList()
    }

    private fun setFavoriteState(session: Session, isFavorite: Boolean) {
        val favorite = getFavorite(session)
        if (favorite != null && !isFavorite) {
            favorites.remove(favorite)
        } else if (isFavorite) {
            favorites.add(Favorite(session.id))
        }
    }

    fun getRating(session: Session): SessionRating? = localVotes
            .firstOrNull { it.sessionId == session.id }
            ?.rating?.let { SessionRating.valueOf(it) }

    fun setRating(
            session: Session,
            rating: SessionRating
    ): KonfPromise<SessionRating?> {
        val currentRating = getRating(session)
        val newRating = if (currentRating == rating) null else rating

        return konfAsync {
            try {
                val vote = Vote(session.id, rating.value)
                if (newRating != null) api.postVote(vote) else api.deleteVote(vote)
                setLocalRating(session, rating = newRating)

                return@konfAsync newRating
            } catch (cause: ApiException) {
                val code = VoteActionResult.fromCode(cause.response.statusCode)
                when (code) {
                    VoteActionResult.TOO_EARLY -> throw EARLY_SUBMITTION_ERROR
                    VoteActionResult.TOO_LATE -> throw LATE_SUBMITTION_ERROR
                    else -> throw cause
                }
            }
        }
    }

    fun deleteRating(session: Session): KonfPromise<Unit> = konfAsync {
        api.deleteVote(Vote(session.id))
    }

    private fun getFavorite(session: Session): Favorite? =
            favorites.firstOrNull { it.sessionId == session.id }

    private fun setLocalRating(session: Session, rating: SessionRating?) {
        localVotes.removeAll { it.sessionId == session.id }
        rating?.let { localVotes.add(Vote(session.id, it.value)) }
    }

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
