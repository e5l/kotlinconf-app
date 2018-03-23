import UIKit
import TagListView_ObjC

class SessionViewController : UIViewController, UITableViewDataSource, UITableViewDelegate {
    private let favoritesManager = FavoritesManager()

    var session: KSession!
    var speakers: [KSpeaker] = []

    @IBOutlet private weak var scrollView: UIScrollView!
    @IBOutlet private weak var titleLabel: UILabel!
    @IBOutlet private weak var timeLabel: UILabel!
    @IBOutlet private weak var descriptionLabel: UILabel!
    @IBOutlet private weak var usersTable: UITableView!
    @IBOutlet private weak var tags: TagListView!
    @IBOutlet private weak var favoriteButton: UIButton!

    override func viewWillAppear(_ animated: Bool) {
        guard let session = self.session else { return }

        titleLabel.text = session.title
        timeLabel.text = renderDates(startDate: session.startsAt, endDate: session.endsAt)
        descriptionLabel.text = session.desc
        usersTable.reloadData()

        updateFavoriteButtonTitle()

        speakers = fetchSpeakers()

        tags.removeAllTags()

        if let room = fetchRoom() {
            tags.addTag(room.name)
        }

        for categoryItem in fetchCategoryItems() {
            tags.addTag(categoryItem.name)
        }

        DispatchQueue.main.async {
            guard let usersTable = self.usersTable else { return }

            let height: CGFloat, itemCount = usersTable.numberOfRows(inSection: 0)
            if (itemCount == 0) {
                height = 0
            } else {
                height = CGFloat(itemCount) * usersTable.cellForRow(at: IndexPath(row: 0, section: 0))!.bounds.height
            }

            usersTable.frame.size.height = height

            self.scrollView.contentSize = CGSize(
                width: self.scrollView.contentSize.width,
                height: self.usersTable.frame.maxY + 10)
        }
    }

    private func updateFavoriteButtonTitle(isFavorite: Bool? = nil) {
        let shouldCheck = isFavorite ?? favoritesManager.isFavorite(session: session)
        favoriteButton.setTitle(shouldCheck ? "❤️" : "🖤", for: .normal)
    }

    @IBAction func favorited(_ sender: Any) {
        favoritesManager.toggleFavorite(for: session, errorHandler: createErrorHandler("Unable to send request")) {
            self.updateFavoriteButtonTitle(isFavorite: $0)
        }
    }

    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        super.prepare(for: segue, sender: sender)

        if segue.identifier == "Vote", let controller = segue.destination as? VoteViewController {
            controller.session = self.session
        }
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return speakers.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "User", for: indexPath) as! SessionUserTableViewCell
        cell.setup(for: speakers[indexPath.row])
        return cell
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let speaker = speakers[indexPath.row]

        let alert = UIAlertController(title: speaker.fullName, message: speaker.bio, preferredStyle: .actionSheet)

        for link in speaker.link ?? [] {
            guard let action = link.getAction() else { continue }
            alert.addAction(action)
        }

        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))

        self.present(alert, animated: true, completion: nil)
    }
}

fileprivate extension SessionViewController {
    func fetchRoom() -> KRoom? {
        let moc = AppDelegate.me.managedObjectContext

        let request = NSMakeFetchRequest(for: KRoom.self)
        request.predicate = NSPredicate(format: "id == %d", session.roomId)
        request.fetchLimit = 1

        return (try? moc.fetch(request))?.first
    }

    func fetchSpeakers() -> [KSpeaker] {
        let moc = AppDelegate.me.managedObjectContext

        let request = NSMakeFetchRequest(for: KSpeaker.self)
        request.predicate = NSPredicate(format: "id IN %@", session.speakerIds ?? [])

        let unsortedSpeakers: [KSpeaker] = (try? moc.fetch(request)) ?? []

        var sortedSpeakers = [KSpeaker]()
        sortedSpeakers.reserveCapacity(unsortedSpeakers.count)

        for speakerId in (session.speakerIds as? [String]) ?? [] {
            guard let speaker = (unsortedSpeakers.first { $0.id == speakerId }) else { continue }
            sortedSpeakers.append(speaker)
        }

        return sortedSpeakers
    }

    func fetchCategoryItems() -> [KCategoryItem] {
        let moc = AppDelegate.me.managedObjectContext

        let request = NSMakeFetchRequest(for: KCategoryItem.self)
        request.predicate = NSPredicate(format: "id IN %@", session.categoryItemIds ?? [])
        request.sortDescriptors = [ NSSortDescriptor(key: "id", ascending: true) ]

        return (try? moc.fetch(request)) ?? []
    }
}

fileprivate extension KLink {
    func getAction() -> UIAlertAction? {
        guard
            let linkType = self.linkType,
            linkType == "Twitter",
            let title = self.title,
            let urlText = self.url,
            let url = URL(string: urlText)
        else { return nil }

        // + (instancetype)actionWithTitle:(NSString *)title style:(UIAlertActionStyle)style handler:(void (^)(UIAlertAction *action))handler;
        return (UIAlertAction(title: "\(title): @\(url.lastPathComponent)", style: .default) { _ in
            // @property(class, nonatomic, readonly) UIApplication *sharedApplication;

            if #available(iOS 10.0, *) {
                // - (void)openURL:(NSURL *)url options:(NSDictionary<NSString *,id> *)options completionHandler:(void (^)(BOOL success))completion;
                UIApplication.shared.open(url, options: [:], completionHandler: nil)
            } else {
                // - (BOOL)openURL:(NSURL *)url;
                UIApplication.shared.openURL(url)
            }
        })
    }
}

class SessionUserTableViewCell : UITableViewCell {
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var icon: UIImageView!

    override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        doInit()
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        doInit()
    }

    private func doInit() {
        let bgColorView = UIView()
        bgColorView.backgroundColor = UIColor.clear
        self.selectedBackgroundView = bgColorView
    }

    func setup(for user: KSpeaker) {
        nameLabel.text = user.fullName ?? "Anonymous"
        icon.loadUserIcon(url: user.profilePicture)
    }
}
