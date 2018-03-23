package org.jetbrains.kotlinconf.data

import org.jetbrains.kotlinconf.*
import org.jetbrains.kotlinconf.util.*

object AppContext {
    var allData: AllData? = null

    var localFavorites: MutableList<Favorite> = mutableListOf()
    var localVotes: MutableList<Vote> = mutableListOf()

    var sessionsModels: List<SessionModel>? = null
}

operator fun Session.compareTo(other: Session): Int {
    val thisDate = startsAt ?: Date()
    val otherDate = other.startsAt ?: Date()

    if (thisDate == otherDate) {
        return (title ?: "").compareTo(other.title ?: "")
    }

    return thisDate.compareTo(otherDate)
}

object SessionsComparator: Comparator<Session> {
    override fun compare(a: Session, b: Session): Int {
        return a.compareTo(b)
    }
}
