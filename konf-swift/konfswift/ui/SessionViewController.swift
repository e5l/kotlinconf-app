import UIKit
import TagListView_ObjC
import konfSwiftFramework

class SessionViewController : UIViewController, UITableViewDataSource, UITableViewDelegate {
    private let favoritesManager = FavoritesManager()
    private let repository = KSFDataRepository(uuid: AppDelegate.me.userUuid)

    var session: KSFSession!
    var speakers: [KSFSpeaker] = []

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
        timeLabel.text = KSFUtil.renderInterval(start: session.startsAt!, end: session.endsAt!)
        descriptionLabel.text = session.description ?? ""
        usersTable.reloadData()

        updateFavoriteButtonTitle()

        speakers = repository.findSortedSpeakers(session: session)

        tags.removeAllTags()

        if let room = repository.findRoom(session: session) {
            tags.addTag(room.name)
        }

        for categoryItem in repository.findCategoryItems(session: session) {
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
        let shouldCheck = isFavorite ?? repository.isFavorite(session: session)
        favoriteButton.setTitle(shouldCheck ? "❤️" : "🖤", for: .normal)
    }

    @IBAction func favorited(_ sender: Any) {
        repository.toggleFavorite(session: session) {
            self.updateFavoriteButtonTitle(isFavorite: $0 != 0)
            return KSFStdlibUnit()
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

        // BUG?: speaker.links has type [Any]
        
//        for link in speaker.links ?? [] {
//            guard let action = link.getAction() else { continue }
//            alert.addAction(action)
//        }

        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))

        self.present(alert, animated: true, completion: nil)
    }
}

fileprivate extension KSFLink {
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

    func setup(for user: KSFSpeaker) {
        nameLabel.text = user.fullName ?? "Anonymous"
        icon.loadUserIcon(url: user.profilePicture)
    }
}
