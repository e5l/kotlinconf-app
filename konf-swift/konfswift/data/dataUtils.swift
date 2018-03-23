import UIKit
import SDWebImage

enum KSessionRating : Int {
    case good = 1, soso = 0, bad = -1
}

extension KSession : Comparable {
    private static let DEFAULT_DATE = Date(timeIntervalSince1970: 0)

    public static func <(lhs: KSession, rhs: KSession) -> Bool {
        let lStarts = lhs.startsAt ?? KSession.DEFAULT_DATE, rStarts = rhs.startsAt ?? KSession.DEFAULT_DATE
        if (lStarts == rStarts) {
            return (lhs.title ?? "") < (rhs.title ?? "")
        }
        return lStarts < rStarts
    }
}

extension KVote {
    var sessionRating: KSessionRating? {
        return KSessionRating(rawValue: Int(self.rating))
    }
}

extension KAll {
    func findSpeaker(by id: String) -> KSpeaker? {
        for speaker in self.speaker ?? [] {
            if let speakerId = speaker.id, speakerId == id {
                return speaker
            }
        }

        return nil
    }
}

extension UIImageView {
    private static let PLACEHOLDER_IMAGE = UIImage.init(named: "user_default")?.circularImage()

    func loadUserIcon(url: String?) {
        let completionBlock: (UIImage?, Error?, SDImageCacheType, URL?) -> () = { (image, error, cacheType, imageURL) in
            if let existingImage = image {
                self.image = existingImage.circularImage()
            }
        }

        let nsUrl: URL?
        if let setUrl = url {
            nsUrl = URL(string: setUrl)
        } else {
            nsUrl = nil
        }

        self.sd_setImage(
            with: nsUrl,
            placeholderImage: UIImageView.PLACEHOLDER_IMAGE,
            completed: completionBlock)
    }
}
