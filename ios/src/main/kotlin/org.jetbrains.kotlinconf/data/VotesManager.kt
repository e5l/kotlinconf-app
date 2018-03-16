package org.jetbrains.kotlinconf.data

import kotlinx.cinterop.*
import org.jetbrains.kotlinconf.model.*
import org.jetbrains.kotlinconf.ui.*
import org.jetbrains.kotlinconf.util.*
import platform.Foundation.*
import platform.CoreData.*

class VotesManager {
    companion object {
        val EARLY_SUBMITTION_ERROR = NSError("VotesManager", code = 1, userInfo = null)
        val LATE_SUBMITTION_ERROR = NSError("VotesManager", code = 2, userInfo = null)
    }

    fun getRating(session: Session): SessionRating? {
        val moc = appDelegate.managedObjectContext

        val request = NSFetchRequest(entityName = "KVote")
        request.fetchLimit = 1
        request.predicate = NSPredicate.predicateWithFormat(
                "sessionId == %@",
                argumentArray = nsArrayOf(session.id.uncheckedCast())
        )

        return attempt(null) {
            moc.executeFetchRequest(request).firstObject?.uncheckedCast<Vote>()
        }?.rating?.let { SessionRating.valueOf(it) }
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
        val moc = appDelegate.managedObjectContext

        // Delete old rating if exists
//        val request = NSFetchRequest(entityName = "KVote")
//        request.predicate = NSPredicate.predicateWithFormat("sessionId == %@", argumentArray = nsArrayOf(session.id!!.toNSString()))
//        val votes: NSArray = moc.executeFetchRequest(request)
//        votes.toList<Vote>().forEach { moc.deleteObject(it) }

//        if (rating != null) {
//            Vote(moc.entityDescription("KVote"), insertIntoManagedObjectContext = moc).apply {
//                sessionId = session.id
//                setRating(rating.rawValue)
//            }
//        }

        moc.save()
    }
}
