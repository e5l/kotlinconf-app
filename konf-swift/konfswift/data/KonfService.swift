import Foundation
import Groot
import OMGHTTPURLRQ
import ILGHttpConstants

class KonfService : NetworkService {
    static let BASE_URL = "https://it-konf.service.spb.consul"
    let errorHandler: (Error) -> ()

    init(errorHandler: @escaping (Error) -> ()) {
        self.errorHandler = errorHandler
    }

    func registerUser(uuid: String, onComplete: @escaping () -> ()) {
        let request = try! OMGHTTPURLRQ.post(url("/users"), rawText: uuid)
        plainRequest(request, requiredCode: HTTPStatusCode.created, handler: onComplete)
    }

    func getVotes(uuid: String, onComplete: @escaping ([Any]) -> ()) {
        let request = try! OMGHTTPURLRQ.get(url("/votes"), nil).acceptsJson().auth(uuid: uuid)
        jsonRequest(request) { onComplete($0 as? [Any] ?? []) }
    }

    func addVote(_ session: KSession, rating: KSessionRating, uuid: String, onComplete: @escaping () -> ()) {
        let request = try! OMGHTTPURLRQ.post(url("/votes"), json: [
            "sessionId": session.id as Any,
            "rating": rating.rawValue
        ]).acceptsJson().auth(uuid: uuid)
        
        plainRequest(request, requiredCode: HTTPStatusCode.created, handler: onComplete)
    }

    func deleteVote(_ session: KSession, uuid: String, onComplete: @escaping () -> ()) {
        let request = try! OMGHTTPURLRQ.delete(url("/votes"), json: [ "sessionId": session.id ])
            .acceptsJson().auth(uuid: uuid)

        plainRequest(request, requiredCode: HTTPStatusCode.OK, handler: onComplete)
    }

    func getFavorites(uuid: String, onComplete: @escaping ([Any]) -> ()) {
        let request = try! OMGHTTPURLRQ.get(url("/favorites"), nil).acceptsJson().auth(uuid: uuid)
        jsonRequest(request) { onComplete($0 as? [Any] ?? []) }
    }

    func addFavorite(_ session: KSession, uuid: String, onComplete: @escaping () -> ()) {
        let request = try! OMGHTTPURLRQ.post(url("/favorites"), json: session.toSessionIdJson())
            .acceptsJson().auth(uuid: uuid)

        plainRequest(request, requiredCode: HTTPStatusCode.created, handler: onComplete)
    }

    func deleteFavorite(_ session: KSession, uuid: String, onComplete: @escaping () -> ()) {
        let request = try! OMGHTTPURLRQ.delete(url("/favorites"), json: session.toSessionIdJson())
            .acceptsJson().auth(uuid: uuid)

        plainRequest(request, requiredCode: HTTPStatusCode.OK, handler: onComplete)
    }

    func getSessions(onComplete: @escaping ([String: Any]) -> ()) {
        jsonRequest(try! OMGHTTPURLRQ.get(url("/all"), nil)) { data in
            onComplete(data as? [String : Any] ?? [:])
        }
    }
}

fileprivate extension KSession {
    func toSessionIdJson() -> Any {
        return [ "sessionId": self.id ]
    }
}

fileprivate extension NSMutableURLRequest {
    func acceptsJson() -> NSMutableURLRequest {
        addValue("application/json", forHTTPHeaderField: "Accept")
        return self
    }

    func auth(uuid: String) -> NSMutableURLRequest {
        addValue("Bearer \(uuid)", forHTTPHeaderField: "Authorization")
        return self
    }
}
