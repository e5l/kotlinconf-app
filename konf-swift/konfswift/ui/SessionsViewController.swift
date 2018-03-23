import UIKit
import CoreData

class SessionsViewController: UITableViewController {
    private static let SEND_ID_ONCE_KEY = "sendIfOnce"

    private let favoritesManager = FavoritesManager()

    private var mode: SessionsListMode = .all

    private var _fetchedResultsController: NSFetchedResultsController<KSession>? = nil

    @IBOutlet weak var pullToRefresh: UIRefreshControl!

    private var fetchedResultsController: NSFetchedResultsController<KSession> {
        if let existingController = _fetchedResultsController {
            return existingController
        }

        let request: NSFetchRequest<KSession> = NSMakeFetchRequest(for: KSession.self)
        request.sortDescriptors = [
            NSSortDescriptor(key: "endsAt", ascending: true),
            NSSortDescriptor(key: "id", ascending: true)]

        if (self.mode == .favorites) {
            let favorites = favoritesManager.getFavoriteItemIds()
            request.predicate = NSPredicate(format: "id IN %@", favorites)
        }

        let moc = AppDelegate.me.managedObjectContext
        let fetchedResultsController = NSFetchedResultsController(
            fetchRequest: request, managedObjectContext: moc, sectionNameKeyPath: nil, cacheName: nil)
        fetchedResultsController.delegate = self

        _fetchedResultsController = fetchedResultsController
        return fetchedResultsController
    }

    @IBAction func tabSelected(_ sender: Any) {
        guard let segmentedControl = sender as? UISegmentedControl else { return }
        self.mode = (segmentedControl.selectedSegmentIndex == 0) ? .all : .favorites
        _fetchedResultsController = nil
        self.updateResults()
        if let tableView = self.tableView, tableView.numberOfRows(inSection: 0) > 0 {
            tableView.scrollToRow(at: IndexPath(item: 0, section: 0), at: .top, animated: true)
        }
    }

    override func viewDidLoad() {
        updateResults()

        let moc = AppDelegate.me.managedObjectContext.asPrivateThreadContext()
        DispatchQueue.global().async {
            let loadSessions = (try? moc.count(for: NSMakeFetchRequest(for: KSession.self))) ?? 0 == 0

            DispatchQueue.main.async {
                if (loadSessions) {
                    self.refreshSessions(self)
                } else {
                    self.refreshFavorites()
                    self.refreshVotes()
                }
            }
        }

        registerUuidIfNeeded()
    }

    private func registerUuidIfNeeded() {
        let userDefaults = UserDefaults.standard
        guard !userDefaults.bool(forKey: SessionsViewController.SEND_ID_ONCE_KEY) else { return }

        KonfService(errorHandler: { _ in }).registerUser(uuid: AppDelegate.me.userUuid) {
            userDefaults.set(true, forKey: SessionsViewController.SEND_ID_ONCE_KEY)
        }
    }

    @IBAction func refreshSessions(_ sender: Any) {
        let service = KonfService(errorHandler: createErrorHandler("Unable to load sessions") {
            self.pullToRefresh?.endRefreshing()
        })

        KonfLoader(service).updateSessions {
            self.pullToRefresh?.endRefreshing()
            self.updateResults()
        }
    }

    private func refreshFavorites() {
        let service = KonfService(errorHandler: { NSLog($0.localizedDescription) })
        KonfLoader(service).updateFavorites {
            if (self.mode == .favorites) {
                self.updateResults()
            }
        }
    }

    private func refreshVotes() {
        let service = KonfService(errorHandler: { NSLog($0.localizedDescription) })
        KonfLoader(service).updateVotes()
    }

    private func updateResults() {
        do {
            try fetchedResultsController.performFetch()
            self.tableView?.reloadData()
        } catch {
            showPopupText(title: "Unable to load sessions.")
        }
    }

    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        super.prepare(for: segue, sender: sender)

        switch (segue.identifier ?? "") {
            case "ShowSession":
                guard
                    let selectedCell = sender as? SessionsTableViewCell,
                    let selectedPath = tableView?.indexPath(for: selectedCell)
                else { return }

                let sessionViewController = segue.destination as! SessionViewController
                sessionViewController.session = fetchedResultsController.object(at: selectedPath)
            default: break
        }
    }

    override func numberOfSections(in tableView: UITableView) -> Int {
        return fetchedResultsController.sections?.count ?? 0
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        guard let sections = fetchedResultsController.sections else { return 0 }
        return sections[section].numberOfObjects
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "Session", for: indexPath) as! SessionsTableViewCell
        let session = fetchedResultsController.object(at: indexPath)
        cell.setup(for: session)
        return cell
    }
}

fileprivate enum SessionsListMode {
    case all, favorites
}

extension SessionsViewController : NSFetchedResultsControllerDelegate {
    func controller(
        _ controller: NSFetchedResultsController<NSFetchRequestResult>,
        didChange sectionInfo: NSFetchedResultsSectionInfo,
        atSectionIndex sectionIndex: Int,
        for type: NSFetchedResultsChangeType
    ) {
        switch type {
            case .insert:
                tableView.insertSections(IndexSet(integer: sectionIndex), with: .fade)
            case .delete:
                tableView.deleteSections(IndexSet(integer: sectionIndex), with: .fade)
            case .move:
                break
            case .update:
                break
        }
    }

    func controller(
        _ controller: NSFetchedResultsController<NSFetchRequestResult>,
        didChange anObject: Any,
        at indexPath: IndexPath?,
        for type: NSFetchedResultsChangeType,
        newIndexPath: IndexPath?
    ) {
        switch type {
            case .insert:
                tableView.insertRows(at: [newIndexPath!], with: .fade)
            case .delete:
                tableView.deleteRows(at: [indexPath!], with: .fade)
            case .update:
                tableView.reloadRows(at: [indexPath!], with: .fade)
            case .move:
                tableView.moveRow(at: indexPath!, to: newIndexPath!)
        }
    }

    func controllerWillChangeContent(_ controller: NSFetchedResultsController<NSFetchRequestResult>) {
        tableView.separatorStyle = .singleLine
        tableView.beginUpdates()
    }

    func controllerDidChangeContent(_ controller: NSFetchedResultsController<NSFetchRequestResult>) {
        tableView.endUpdates()
    }
}

class SessionsTableViewCell : UITableViewCell {
    @IBOutlet private weak var dateLabel: UILabel!
    @IBOutlet private weak var titleLabel: UILabel!
    @IBOutlet private weak var icon: UIImageView!

    func setup(for session: KSession) {
        titleLabel.text = session.title
        dateLabel.text = renderDate(session.startsAt)
        icon.loadUserIcon(url: session.profilePicture)
    }
}

class BreakTableViewCell : UITableViewCell {
    @IBOutlet weak var titleLabel: UILabel!

    private lazy var setBackground: () -> () = {
        self.backgroundColor = UIColor(patternImage: UIImage(named: "striped_bg")!)
        return {}
    }()

    func setup(for session: KSession) {
        setBackground()
        titleLabel.text = session.title
    }
}
