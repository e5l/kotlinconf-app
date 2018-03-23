import UIKit

class VoteViewController : UIViewController {
    private let votesManager = VotesManager()

    var session: KSession!

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

    private func highlightRatingButtons(rating: KSessionRating? = nil) {
        let currentRating = rating ?? votesManager.getRating(for: session)

        let buttons: [KSessionRating: UIButton] = [
            .good: goodButton,
            .soso: sosoButton,
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

    private func reportRating(_ rating: KSessionRating) {
        guard let session = self.session else { return }

        votesManager.setRating(
            for: session,
            rating: rating,
            errorHandler: createErrorHandler("Unable to send vote")
        ) { newRating in
            self.highlightRatingButtons(rating: newRating)
            self.showPopupText(
                title: newRating != nil ? "Thank you for the feedback!" : "Your vote was cleared.")
        }
    }

    @IBAction private func goodPressed(_ sender: Any) {
        reportRating(.good)
    }

    @IBAction private func sosoPressed(_ sender: Any) {
        reportRating(.soso)
    }

    @IBAction private func badPressed(_ sender: Any) {
        reportRating(.bad)
    }
}
