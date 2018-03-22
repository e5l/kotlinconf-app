package org.jetbrains.kotlinconf.data

import org.jetbrains.kotlinconf.model.*
import org.jetbrains.kotlinconf.ui.*
import org.jetbrains.kotlinconf.util.*
import platform.Foundation.*

class VotesManager {
    companion object {
        val EARLY_SUBMITTION_ERROR = NSError("VotesManager", code = 1, userInfo = null)
        val LATE_SUBMITTION_ERROR = NSError("VotesManager", code = 2, userInfo = null)
    }

    fun getRating(session: Session): SessionRating? {
        return AppContext.localVotes.firstOrNull { it.sessionId == session.id }?.rating?.let { SessionRating.valueOf(it) }
    }

    fun setRating(
            session: Session,
            rating: SessionRating,
            errorHandler: (NSError) -> Unit,
            onComplete: (SessionRating?) -> Unit
    ) {
        val currentRating = getRating(session)
        val service = KonfService(errorHandler)

        val newRating = if (currentRating == rating) null else rating

        log("rating: $rating, currentRating = $currentRating, newRating = $newRating")

        val completionHandler: (KonfService.VoteActionResult) -> Unit = handler@{ response ->
            assert(NSThread.isMainThread)

            when (response) {
                KonfService.VoteActionResult.TOO_EARLY -> errorHandler(EARLY_SUBMITTION_ERROR)
                KonfService.VoteActionResult.TOO_LATE -> errorHandler(LATE_SUBMITTION_ERROR)
                else -> attempt(errorHandler) {
                    setLocalRating(session, rating = newRating)
                    onComplete(newRating)
                }
            }
        }

        val uuid = appDelegate.userUuid

        if (newRating != null) {
            service.addVote(session, rating, uuid, onComplete = completionHandler)
        } else {
            service.deleteVote(session, uuid, onComplete = completionHandler)
        }
    }

    private fun setLocalRating(session: Session, rating: SessionRating?) {
        AppContext.localVotes.removeAll { it.sessionId == session.id }
        if (rating != null) {
            AppContext.localVotes.add(Vote(session.id, rating.value))
        }
    }
}
