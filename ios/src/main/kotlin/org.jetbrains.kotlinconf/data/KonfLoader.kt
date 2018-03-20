package org.jetbrains.kotlinconf.data

import kotlinx.cinterop.*
import libs.*
import org.jetbrains.kotlinconf.ui.*
import org.jetbrains.kotlinconf.util.*
import platform.CoreData.*
import platform.Foundation.*

class KonfLoader(private val service: KonfService) {
    fun updateSessions(onComplete: () -> Unit) {
        service.getSessions(appDelegate.userUuid) { parseSessions(it, onComplete) }
    }

    fun updateFavorites(onComplete: () -> Unit = {}) {
        service.getFavorites(appDelegate.userUuid, updateSimpleList("KFavorite", onComplete))
    }

    fun updateVotes(onComplete: () -> Unit = {}) {
        service.getVotes(appDelegate.userUuid, updateSimpleList("KVote", onComplete))
    }

    private fun updateSimpleList(entityName: String, onComplete: () -> Unit): (NSArray) -> Unit {
        return { arr ->
            val moc = appDelegate.managedObjectContext

            performSafe(moc) {
                moc.deleteAll(entityName)
                nsTry { errorPtr ->
                    GRTJSONSerialization.objectsWithEntityName(
                        entityName, fromJSONArray = arr, inContext = moc, error = errorPtr)
                }
                moc.saveRecursively()
                DispatchQueue.main.async { onComplete() }
            }
        }
    }

    private fun parseSessions(dict: NSDictionary, onComplete: () -> Unit) {
        val moc = appDelegate.managedObjectContext

        performSafe(moc) {
//            moc.deleteAll("AllData")
            
            val all = nsTry { errorPtr ->
                GRTJSONSerialization.objectWithEntityName("AllData",
                    fromJSONDictionary = dict, inContext = moc, error = errorPtr)
            }!!.uncheckedCast<AllData>()

            for (session in all.sessions ?: emptyList()) {
                val speakerIdsList: List<String?> = session.speakers!!
                
                var subtitle = speakerIdsList.asSequence()
                    .filterNotNull()
                    .mapNotNull { all.findSpeaker(it) }
                    .take(2)
                    .mapNotNull { it.fullName }
                    .joinToString()

                val roomName = all.findRoom(session.roomId!!)?.name?.let { roomName ->
                    subtitle += " — $roomName"
//                    session.roomName = roomName
                }

//                session.startsAtDate = parseDate(session.startsAt)
//                session.endsAtDate = parseDate(session.endsAt)
                
//                session.subtitle = subtitle
            }

            moc.saveRecursively()
            DispatchQueue.main.async { onComplete() }
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
