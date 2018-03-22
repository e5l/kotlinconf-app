package org.jetbrains.kotlinconf.data

import kotlinx.cinterop.*
import org.jetbrains.kotlinconf.ui.*
import org.jetbrains.kotlinconf.util.*
import platform.Foundation.*
import platform.CoreData.*

class FavoritesManager {
    fun isFavorite(session: Session) = getFavorite(session) != null

    private fun getFavorite(session: Session): Favorite? {
        return AppContext.localFavorites.firstOrNull { it.sessionId == session.id }
    }

    fun toggleFavorite(
        session: Session,
        onComplete: (Boolean) -> Unit
    ) {
        val newFavorite = !isFavorite(session)
        val service = KonfService({ log(it.localizedDescription) })

        setLocalFavorite(session, isFavorite = newFavorite)
        onComplete(newFavorite)

        val uuid = appDelegate.userUuid
        if (newFavorite) {
            service.addFavorite(session, uuid, onComplete = {})
        } else {
            service.deleteFavorite(session, uuid, onComplete = {})
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
        return AppContext.localFavorites.map { it.sessionId }.filterNotNull()
    }
}
