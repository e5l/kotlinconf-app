import UIKit
import konfSwiftFramework

class VoteViewController : UIViewController {
    //private let votesManager = VotesManager()
    private let repository = KSFDataRepository(uuid: AppDelegate.me.userUuid)

    var session: KSFSession!
    

    @IBOutlet private weak var titleLabel: UILabel!

    @IBOutlet weak var titleBackground: UIView!
    @IBOutlet weak var goodButton: UIButton!
    @IBOutlet weak var sosoButton: UIButton!
    @IBOutlet weak var badButton: UIButton!

    override func viewDidLoad() {
        for view in [goodButton, sosoButton, badButton, titleBackground] {
            view?.layer.cornerRadius = 5
        }
    }

    private func highlightRatingButtons(rating: KSFSessionRating? = nil) {
        let currentRating = rating ?? repository.getRating(session: session)

        let buttons: [KSFSessionRating: UIButton] = [
            .good: goodButton,
            .ok: sosoButton,
            .bad: badButton
        ]

        for (buttonRating, button) in buttons {
            button.backgroundColor = (buttonRating == currentRating)
                ? UIColor.orange
                : UIColor.groupTableViewBackground
        }
    }

    override func viewWillAppear(_ animated: Bool) {
        guard let session = self.session else { return }
        titleLabel.text = session.title

        // Buttons are not highlighted without this for some reason
        DispatchQueue.main.async {
            self.highlightRatingButtons()
        }
    }

    private func reportRating(_ rating: KSFSessionRating) {
        guard let session = self.session else { return }

        repository.setRating(
            session: session,
            rating: rating,
            onError: { error in
                self.showPopupText(title: "Can't set rating")
                return KSFStdlibUnit()
            },
            onComplete: { newRating in
            self.highlightRatingButtons(rating: newRating)
            self.showPopupText(
                title: newRating != nil ? "Thank you for the feedback!" : "Your vote was cleared.")
                return KSFStdlibUnit()
            }
        )
    }

    @IBAction private func goodPressed(_ sender: Any) {
        reportRating(.good)
    }

    @IBAction private func sosoPressed(_ sender: Any) {
        reportRating(.ok)
    }

    @IBAction private func badPressed(_ sender: Any) {
        reportRating(.bad)
    }
}
