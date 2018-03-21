package org.jetbrains.kotlinconf.data

import kotlinx.cinterop.*
import org.jetbrains.kotlinconf.ui.*
import org.jetbrains.kotlinconf.util.*
import platform.Foundation.*
import platform.CoreData.*

class FavoritesManager {
    fun isFavorite(session: Session) = getFavorite(session) != null

    private fun getFavorite(session: Session): Favorite? {
        val moc = appDelegate.managedObjectContext

        val request = NSFetchRequest(entityName = "Favorite")
        request.fetchLimit = 1
        request.predicate = NSPredicate.predicateWithFormat(
                "sessionId == %@", 
                argumentArray = nsArrayOf(session.id!!.toNSString())
        )

        return attempt(null) {
            moc.executeFetchRequest(request).firstObject?.uncheckedCast<Favorite>()
        }
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
        val moc = appDelegate.managedObjectContext

        val favorite = getFavorite(session)
        if (favorite != null && !isFavorite) {
            moc.deleteObject(favorite.uncheckedCast())
        } else if (isFavorite) {
            val favoriteItem = NSEntityDescription
                    .insertNewObjectForEntityForName("Favorite", inManagedObjectContext = moc)
                    .uncheckedCast<Favorite>()

            favoriteItem.sessionId = session.id
        }

        moc.save()
    }

    fun getFavoriteItemIds(): NSArray {
        val moc = appDelegate.managedObjectContext

        var favorites: List<Favorite> = emptyList()
        attempt(null) {
            favorites = moc.executeFetchRequest(NSFetchRequest(entityName = "Favorite")).toList()
        }

        val idsArray = NSMutableArray.arrayWithCapacity(favorites.size.toLong())
        for (favoriteItem in favorites) {
            idsArray.addObject(favoriteItem.sessionId!!.toNSString())
        }

        return idsArray
    }
}
