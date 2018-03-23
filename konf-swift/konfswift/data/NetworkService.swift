import Foundation
import ILGHttpConstants

protocol NetworkService {
    static var BASE_URL: String { get }

    var errorHandler: (Error) -> () { get }
}

enum NetworkServiceError : String, Error {
    case jsonParsingError = "Error while parsing JSON response"
    case emptyBody = "Response body is empty"
    case notHttpResponse = "Not an HTTP response"
    case wrongStatusCode = "Wrong status code"
}

extension NetworkService {
    func url(_ path: String) -> String {
        return KonfService.BASE_URL + path
    }

    func plainRequest(
        _ request: NSURLRequest,
        requiredCode: HTTPStatusCode? = nil,
        handler: @escaping () -> ()
        ) {
        // NSURLSession
        // + (NSURLSession *)sessionWithConfiguration:(NSURLSessionConfiguration *)configuration delegate:(id<NSURLSessionDelegate>)delegate delegateQueue:(NSOperationQueue *)queue;
        let session = URLSession(
            configuration: URLSessionConfiguration.default, delegate: nil, delegateQueue: OperationQueue.main)

        session.dataTask(with: request as URLRequest) { (_data, response, error) in
            if let err = error {
                self.errorHandler(err)
                return
            }

            if let httpResponse = response as? HTTPURLResponse {
                if let code = requiredCode, httpResponse.statusCode != code.rawValue {
                    self.errorHandler(NetworkServiceError.wrongStatusCode)
                } else {
                    handler()
                }
            } else {
                self.errorHandler(NetworkServiceError.notHttpResponse)
            }
        }.resume()
    }

    func jsonRequest(_ request: NSURLRequest, handler: @escaping (Any) -> ()) {
        // NSURLSession/NSURLSessionConfiguration
        // @property(class, readonly, strong) NSURLSessionConfiguration *defaultSessionConfiguration;
        let session = URLSession(
            configuration: URLSessionConfiguration.default, delegate: nil, delegateQueue: OperationQueue.main)

        session.dataTask(with: request as URLRequest) { (_data, response, error) in
            guard error == nil, let data = _data else {
                self.errorHandler(error ?? NetworkServiceError.emptyBody)
                return
            }

            guard let jsonData = try? JSONSerialization.jsonObject(with: data, options: []) else {
                self.errorHandler(NetworkServiceError.jsonParsingError)
                return
            }

            handler(jsonData)
        }.resume()
    }
}
