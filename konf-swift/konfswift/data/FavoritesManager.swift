import Foundation

class FavoritesManager {
    private static let KEY = "favorites"
    private let userDefaults = UserDefaults.standard

    func isFavorite(session: KSession) -> Bool {
        return getFavorite(session: session) != nil
    }

    private func getFavorite(session: KSession) -> KFavorite? {
        let moc = AppDelegate.me.managedObjectContext

        let request = NSMakeFetchRequest(for: KFavorite.self)
        request.predicate = NSPredicate(format: "sessionId == %@", session.id!)
        request.fetchLimit = 1

        guard let favorite = (try? moc.fetch(request))?.first else {
            return nil
        }

        return favorite
    }

    func toggleFavorite(
        for session: KSession,
        errorHandler: @escaping (Error) -> (),
        onComplete: @escaping (Bool) -> ()
    ) {
        let newFavorite = !isFavorite(session: session)

        let service = KonfService(errorHandler: errorHandler)

        let completionHandler: () -> () = {
            assert(Thread.isMainThread)

            do {
                try self.setLocalFavorite(for: session, isFavorite: newFavorite)
                onComplete(newFavorite)
            } catch let e {
                errorHandler(e)
            }
        }

        let uuid = AppDelegate.me.userUuid
        if (newFavorite) {
            service.addFavorite(session, uuid: uuid, onComplete: completionHandler)
        } else {
            service.deleteFavorite(session, uuid: uuid, onComplete: completionHandler)
        }
    }

    func setLocalFavorite(for session: KSession, isFavorite: Bool) throws {
        let moc = AppDelegate.me.managedObjectContext

        if let favorite = getFavorite(session: session) {
            if (!isFavorite) {
                moc.delete(favorite)
            }
        } else if isFavorite {
            let favorite = NSEntityDescription.insertNewObject(
                forEntityName: String(describing: KFavorite.self), into: moc) as! KFavorite
            favorite.sessionId = session.id

            try moc.save()
        }
    }

    func getFavoriteItemIds() -> [String] {
        let moc = AppDelegate.me.managedObjectContext

        let favorites: [KFavorite] = (try? moc.fetch(NSMakeFetchRequest(for: KFavorite.self))) ?? []
        return favorites.flatMap { $0.sessionId }
    }
}
