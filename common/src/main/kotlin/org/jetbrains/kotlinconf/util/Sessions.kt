package org.jetbrains.kotlinconf.util

import org.jetbrains.kotlinconf.data.*

operator fun Session.compareTo(other: Session): Int {
    val thisDate = startsAt ?: Date()
    val otherDate = other.startsAt ?: Date()
    return if (thisDate == otherDate) (title ?: "").compareTo(other.title ?: "") else thisDate.compareTo(otherDate)
}

object SessionsComparator : Comparator<Session> {
    override fun compare(a: Session, b: Session): Int = a.compareTo(b)
}
