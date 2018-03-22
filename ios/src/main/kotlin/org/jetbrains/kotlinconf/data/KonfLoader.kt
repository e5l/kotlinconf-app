package org.jetbrains.kotlinconf.data

import org.jetbrains.kotlinconf.*
import org.jetbrains.kotlinconf.ui.*
import org.jetbrains.kotlinconf.util.*
import platform.CoreData.*
import platform.Foundation.*

class KonfLoader(private val service: KonfService) {
    fun updateSessions(onComplete: () -> Unit) {
        service.getSessions(appDelegate.userUuid) { parseSessions(it, onComplete) }
    }

    fun updateFavorites(onComplete: () -> Unit = {}) {
        service.getFavorites(appDelegate.userUuid, updateSimpleList("Favorite", onComplete))
    }

    fun updateVotes(onComplete: () -> Unit = {}) {
        service.getVotes(appDelegate.userUuid, updateSimpleList("Vote", onComplete))
    }

    private fun updateSimpleList(entityName: String, onComplete: () -> Unit): (NSArray) -> Unit {
       // todo
        return {}
    }

    private fun parseSessions(all: AllData, onComplete: () -> Unit) {

        val sessionModels = all.sessions?.map { SessionModel.forSession(all, it.id!!)!! }

        DispatchQueue.main.async {
            AppContext.allData = all
            AppContext.sessionsModels = sessionModels
            AppContext.localFavorites = all.favorites.orEmpty().toMutableList()
            AppContext.localVotes = all.votes.orEmpty().toMutableList()
            onComplete()
        }
    }

    private fun performSafe(moc: NSManagedObjectContext, block: () -> Unit) {
        moc.perform {
            try {
                block()
            } catch (e: NSErrorException) {
                DispatchQueue.main.async { service.errorHandler(e.error) }
            }
        }
    }
}
