package org.jetbrains.kotlinconf.data

import libs.*
import org.jetbrains.kotlinconf.util.*
import platform.Foundation.*
import platform.UIKit.*

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

fun AllData.findSpeaker(id: String): Speaker? = speakers?.firstOrNull { it.id == id }
fun AllData.findRoom(id: Int): Room? = rooms?.firstOrNull { it.id?.equals(id) == true }

fun UIImageView.loadUserIcon(url: String?) {
    val nsURL = url?.let { NSURL.URLWithString(it) }
    sd_setImageWithURL(nsURL, placeholderImage = PLACEHOLDER_IMAGE)
}

private val PLACEHOLDER_IMAGE = UIImage.imageNamed("user_default")?.circularImage()
