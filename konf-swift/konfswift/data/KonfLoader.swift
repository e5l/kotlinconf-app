import Foundation
import Groot
import CoreData
import OMGHTTPURLRQ
import ILGHttpConstants

class KonfLoader {
    let service: KonfService

    init(_ service: KonfService) {
        self.service = service
    }

    func updateSessions(onComplete: @escaping () -> ()) {
        service.getSessions { rawAll in
            self.parseSessions(dict: rawAll, onComplete: onComplete)
        }
    }

    func updateFavorites(onComplete: @escaping () -> () = {}) {
        service.getFavorites(uuid: AppDelegate.me.userUuid,
                         onComplete: updateSimpleList(for: KFavorite.self, onComplete: onComplete))
    }

    func updateVotes(onComplete: @escaping () -> () = {}) {
        service.getVotes(uuid: AppDelegate.me.userUuid,
                         onComplete: updateSimpleList(for: KVote.self, onComplete: onComplete))
    }

    private func updateSimpleList<T>(
        for type: T.Type,
        onComplete: @escaping () -> ()
    ) -> ([Any]) -> () where T : NSManagedObject {
        return { arr in
            let moc = AppDelegate.me.managedObjectContext.asPrivateThreadContext()

            self.performSafe(moc) {
                try moc.deleteAll(for: type)
                let _: [T] = try objects(fromJSONArray: arr, inContext: moc)
                try moc.saveRecursively()
                DispatchQueue.main.async { onComplete() }
            }
        }
    }

    private func parseSessions(dict: [String: Any], onComplete: @escaping () -> ()) {
        let moc = AppDelegate.me.managedObjectContext.asPrivateThreadContext()

        performSafe(moc) {
            try moc.deleteAll(for: KAll.self)
            let all: KAll = try object(fromJSONDictionary: dict, inContext: moc)

            for session in all.session ?? [] {
                guard let firstSpeakerId = session.speakerIds?.first as? String else { continue }
                guard let firstSpeaker = all.findSpeaker(by: firstSpeakerId) else { continue }
                session.profilePicture = firstSpeaker.profilePicture
            }

            try moc.saveRecursively()
            DispatchQueue.main.async { onComplete() }
        }
    }

    private func performSafe(_ moc: NSManagedObjectContext, block: @escaping () throws -> ()) {
        moc.perform {
            do {
                try block()
            } catch let e {
                DispatchQueue.main.async { self.service.errorHandler(e) }
            }
        }
    }
}

extension OMGHTTPURLRQ {
    static func post(_ url: String, rawText: String) throws -> NSMutableURLRequest {
        let request = try OMGHTTPURLRQ.post(url, nil)
        request.httpBody = rawText.data(using: .utf8)
        return request
    }

    static func delete(_ url: String, json: Any?) throws -> NSMutableURLRequest {
        let request = try OMGHTTPURLRQ.post(url, json: json)
        request.httpMethod = kHTTPMethodDelete
        return request
    }
}

fileprivate extension NSManagedObjectContext {
    func deleteAll<T>(for type: T.Type) throws where T : NSManagedObject {
        let fetchRequest: NSFetchRequest<T> = NSMakeFetchRequest(for: T.self)
        fetchRequest.includesPropertyValues = false

        for item in try fetch(fetchRequest) {
            delete(item)
        }
    }

    func saveRecursively() throws {
        try self.save()

        var _current: NSManagedObjectContext? = parent
        while let current = _current {
            var error: Error? = nil

            current.performAndWait {
                do {
                    try current.save()
                } catch let e {
                    error = e
                }
            }

            if let err = error {
                throw err
            }

            _current = current.parent
        }
    }
}
