package org.jetbrains.kotlinconf

import org.jetbrains.kotlinconf.data.*
import org.jetbrains.kotlinconf.utils.*
import io.ktor.common.client.*

class SessionModel(
        var id: String,
        var title: String,
        val category: String?,
        var description: String,
        var startsAt: Date,
        var endsAt: Date,
        val room: String?,
        var speakers: Array<Speaker>
) {
    companion object {
        fun forSession(all: AllData, sessionId: String): SessionModel? {
            val client = HttpClient()
            val briefSession = all.sessions?.firstOrNull { it.id == sessionId } ?: return null
            val speakerMap = all.speakers?.associateBy { it.id } ?: emptyMap()
            val roomMap = all.rooms?.associateBy { it.id } ?: emptyMap()
            val categoryMap = all.categories?.flatMap {
                it.items?.filterNotNull() ?: emptyList()
            }?.associateBy { it.id } ?: emptyMap()
            return forSession(briefSession,
                    speakerProvider = { id -> speakerMap[id] },
                    categoryProvider = { id -> categoryMap[id] },
                    roomProvider = { id -> roomMap[id] }
            )
        }

        fun forSession(
                briefSession: Session,
                speakerProvider: (String) -> Speaker?,
                categoryProvider: (Int) -> CategoryItem?,
                roomProvider: (Int) -> Room?
        ): SessionModel? {
            val startsAt = briefSession.startsAt ?: return null
            val endsAt = briefSession.endsAt ?: return null
            return SessionModel(
                    id = briefSession.id!!,
                    title = briefSession.title ?: "<untitled>",
                    category = briefSession.categoryItems?.filterNotNull()?.map(categoryProvider)?.firstOrNull()?.name,
                    description = briefSession.description ?: "",
                    startsAt = startsAt,
                    endsAt = endsAt,
                    speakers = (briefSession.speakers
                            ?: emptyList()).filterNotNull().mapNotNull { speakerProvider(it) }.toTypedArray(),
                    room = briefSession.roomId?.let(roomProvider)?.name
            )
        }
    }
}
