package org.jetbrains.kotlinconf.ui

import libs.*
import kotlinx.cinterop.*
import org.jetbrains.kotlinconf.*
import org.jetbrains.kotlinconf.data.*
import org.jetbrains.kotlinconf.model.*
import org.jetbrains.kotlinconf.util.*
import platform.UIKit.*
import platform.Foundation.*
import platform.CoreGraphics.*
import platform.CoreData.*


@ExportObjCClass
class SessionViewController(aDecoder: NSCoder) : UIViewController(aDecoder) {
    private companion object {
        val RATING_IMAGES = mapOf(
                SessionRating.GOOD to Pair("good", "good_selected"),
                SessionRating.OK to Pair("soso", "soso_selected"),
                SessionRating.BAD to Pair("bad", "bad_selected"))
    }

    private val favoritesManager = FavoritesManager()
    private val votesManager = VotesManager()

    lateinit var session: Session

    @ObjCOutlet
    private lateinit var scrollView: UIScrollView

    @ObjCOutlet
    private lateinit var titleLabel: UILabel
    @ObjCOutlet
    private lateinit var timeLabel: UILabel
    @ObjCOutlet
    private lateinit var tagsLabel: UILabel
    @ObjCOutlet
    private lateinit var descriptionLabel: UILabel

    @ObjCOutlet
    private lateinit var headerView: UIView
    @ObjCOutlet
    private lateinit var userNameStackView: UIStackView
    @ObjCOutlet
    private lateinit var userNamesLabel: UILabel
    @ObjCOutlet
    private lateinit var userIcon1: UIImageView
    @ObjCOutlet
    private lateinit var userIcon2: UIImageView

    @ObjCOutlet
    private lateinit var favoriteButton: UIBarButtonItem

    @ObjCOutlet
    private lateinit var sessionForm: UIView
    @ObjCOutlet
    private lateinit var goodButton: UIButton
    @ObjCOutlet
    private lateinit var sosoButton: UIButton
    @ObjCOutlet
    private lateinit var badButton: UIButton

    override fun initWithCoder(aDecoder: NSCoder) = initBy(SessionViewController(aDecoder))

    override fun viewDidLoad() {
        for (view in listOf(goodButton, sosoButton, badButton)) {
            view.layer.setCornerRadius(5.0)
        }

        for (view in listOf(userIcon1, userIcon2)) {
            val width: CGFloat = view.frame.useContents { size }.width
            view.applyCornerRadius(width / 2)
        }
    }

    override fun viewWillAppear(animated: Boolean) {
        titleLabel.text = session.title
        timeLabel.text = renderInterval(start = session.startsAt!!, end = session.endsAt!!)
        descriptionLabel.text = session.description

        updateFavoriteButtonTitle()
        setupSpeakers()

        val tags = fetchCategoryItems().mapNotNull { it.name }.toMutableList()
        fetchRoom()?.name?.let { tags.add(0, it) }
        tagsLabel.text = tags.joinToString()

        DispatchQueue.main.async {
            highlightRatingButtons()
        }
    }

    private fun setupSpeakers() {
        val speakers = fetchSpeakers()
        userNamesLabel.text = speakers.mapNotNull { it.fullName }.joinToString()

        if (speakers.size == 1) {
            userIcon1.hidden = false
            userIcon2.hidden = true

            userIcon1.loadUserIcon(speakers[0].profilePicture)
        } else if (speakers.size == 2) {
            userIcon1.hidden = false
            userIcon2.hidden = false

            // sic!
            userIcon2.loadUserIcon(speakers[0].profilePicture)
            userIcon1.loadUserIcon(speakers[1].profilePicture)
        } else {
            userIcon1.hidden = true
            userIcon2.hidden = true
        }

        for (constraint in headerView.constraints.toList<NSLayoutConstraint>()) {
            if (constraint.secondItem?.uncheckedCast<UIView>() == userNameStackView
                    && constraint.secondAttribute == NSLayoutAttributeTrailing
            ) {
                constraint.constant = if (speakers.size < 3) (56.0 + 56.0 / 2.0 + 10.0 + 20.0) else 20.0
            }
        }
    }

    private fun highlightRatingButtons(rating: SessionRating? = null) {
        val currentRating = rating ?: votesManager.getRating(session)

        val buttons: Map<SessionRating, UIButton> = mapOf(
                SessionRating.GOOD to goodButton,
                SessionRating.OK to sosoButton,
                SessionRating.BAD to badButton
        )

        for ((buttonRating, button) in buttons) {
            val imageNames = RATING_IMAGES.getValue(buttonRating)
            val isSelected = buttonRating == currentRating

            button.setImage(
                    UIImage.imageNamed(if (isSelected) imageNames.second else imageNames.first),
                    forState = UIControlStateNormal)
        }
    }

    private fun updateFavoriteButtonTitle(isFavorite: Boolean? = null) {
        val shouldCheck = isFavorite ?: favoritesManager.isFavorite(session)
        favoriteButton.image = UIImage.imageNamed(if (shouldCheck) "star_full" else "star_empty")
    }

    @ObjCAction
    fun favorited(sender: ObjCObject?) {
        val progressPopup = showIndeterminateProgress("Submitting…")

        favoritesManager.toggleFavorite(session) {
            progressPopup.hideAnimated(true)
            updateFavoriteButtonTitle(isFavorite = it)
        }
    }

    private fun reportRating(rating: SessionRating) {
        val progressPopup = showIndeterminateProgress("Submitting…")

        val errorHandler = createErrorHandler("Unable to send vote") { progressPopup.hideAnimated(true) }
        val wrappedErrorHandler = { error: NSError ->
            progressPopup.hideAnimated(true)

            when (error) {
                VotesManager.EARLY_SUBMITTION_ERROR ->
                    showPopupText("The session has not started yet.")
                VotesManager.LATE_SUBMITTION_ERROR ->
                    showPopupText("You cannot vote for this session any longer.")
                else -> errorHandler(error)
            }
        }

        votesManager.setRating(session, rating, wrappedErrorHandler) { newRating ->
            progressPopup.hideAnimated(true)
            highlightRatingButtons(newRating)
            showPopupText(if (newRating != null) "Thank you for your feedback!" else "Your vote was removed.")
        }
    }

    @ObjCAction
    private fun goodPressed(sender: ObjCObject?) {
        reportRating(SessionRating.GOOD)
    }

    @ObjCAction
    private fun sosoPressed(sender: ObjCObject?) {
        reportRating(SessionRating.OK)
    }

    @ObjCAction
    private fun badPressed(sender: ObjCObject?) {
        reportRating(SessionRating.BAD)
    }
}

fun SessionViewController.fetchRoom(): Room? {
    val moc = appDelegate.managedObjectContext

    val request = NSFetchRequest.fetchRequestWithEntityName("KRoom")
    request.predicate = NSPredicate.predicateWithFormat("id == %d",
            argumentArray = NSArray.arrayWithObject(NSNumber.numberWithInteger(session.roomId!!.toLong())))
    request.fetchLimit = 1

    return moc.executeFetchRequest(request, error = null)?.firstObject?.uncheckedCast<Room>()
}

fun SessionViewController.fetchSpeakers(): List<Speaker> {
    val moc = appDelegate.managedObjectContext

    val request = NSFetchRequest.fetchRequestWithEntityName("KSpeaker")
    request.predicate = NSPredicate.predicateWithFormat(
            "id IN %@", argumentArray = nsArrayOf(session.speakers.uncheckedCast())
    )

    val unsortedSpeakers = moc.executeFetchRequest(request, error = null).toList<Speaker>()

    val sortedSpeakers = mutableListOf<Speaker>()
    for (speakerId in session.speakers!!) {
        speakerId ?: continue

        val speaker = unsortedSpeakers.firstOrNull { it.id == speakerId } ?: continue
        sortedSpeakers += speaker
    }

    return sortedSpeakers
}

fun SessionViewController.fetchCategoryItems(): List<CategoryItem> {
    if ((session.categoryItems?.count() ?: 0L) == 0L) return emptyList()

    val moc = appDelegate.managedObjectContext

    val request = NSFetchRequest.fetchRequestWithEntityName("KCategoryItem")
    request.predicate = NSPredicate.predicateWithFormat(
            "id IN %@", argumentArray = nsArrayOf(session.categoryItems.uncheckedCast())
    )
    request.sortDescriptors = nsArrayOf(NSSortDescriptor.sortDescriptorWithKey("id", ascending = true))

    return moc.executeFetchRequest(request, error = null).toList()
}

class SessionUserTableViewCell(aDecoder: NSCoder) : UITableViewCell(aDecoder) {
    @ObjCOutlet
    lateinit var nameLabel: UILabel
    @ObjCOutlet
    lateinit var icon: UIImageView

    override fun initWithCoder(aDecoder: NSCoder) = initBy(SessionUserTableViewCell(aDecoder))

    init {
        val bgColorView = UIView()
        bgColorView.backgroundColor = UIColor.clearColor
        this.selectedBackgroundView = bgColorView
    }

    fun setup(user: Speaker) {
        nameLabel.text = user.fullName ?: "Anonymous"
        icon.loadUserIcon(user.profilePicture)
    }
}
