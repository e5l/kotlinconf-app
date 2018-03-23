import Foundation

class VotesManager {
    func getRating(for session: KSession) -> KSessionRating? {
        let moc = AppDelegate.me.managedObjectContext

        let request = NSMakeFetchRequest(for: KVote.self)
        request.predicate = NSPredicate(format: "sessionId == %@", session.id!)
        request.fetchLimit = 1

        guard let vote: KVote = (try? moc.fetch(request))?.first else {
            return nil
        }

        return vote.sessionRating
    }

    func setRating(
        for session: KSession,
        rating: KSessionRating,
        errorHandler: @escaping (Error) -> (),
        onComplete: @escaping (KSessionRating?) -> ()
    ) {
        let currentRating = getRating(for: session)
        let service = KonfService(errorHandler: errorHandler)

        let newRating: KSessionRating?
        if let _currentRating = currentRating, _currentRating == rating {
            newRating = nil
        } else {
            newRating = rating
        }

        let completionHandler: () -> () = {
            assert(Thread.isMainThread)

            do {
                try self.setLocalRating(for: session, rating: newRating)
                onComplete(newRating)
            } catch let e {
                errorHandler(e)
            }
        }

        let uuid = AppDelegate.me.userUuid
        if let rating = newRating {
            service.addVote(session, rating: rating, uuid: uuid, onComplete: completionHandler)
        } else {
            service.deleteVote(session, uuid: uuid, onComplete: completionHandler)
        }
    }

    private func setLocalRating(for session: KSession, rating: KSessionRating?) throws {
        let moc = AppDelegate.me.managedObjectContext

        // Delete old rating if exists
        let request = NSMakeFetchRequest(for: KVote.self)
        request.predicate = NSPredicate(format: "sessionId == %@", session.id!)
        let votes: [KVote] = try moc.fetch(request)
        votes.forEach { moc.delete($0) }

        if let newRating = rating {
            let vote = KVote(entity: moc.entityDescription(for: KVote.self), insertInto: moc)
            vote.sessionId = session.id
            vote.rating = Int32(newRating.rawValue)
        }

        try moc.save()
    }
}
