import UIKit
import CoreData
import konfSwiftFramework

class SessionsViewController: UITableViewController {
    private static let SEND_ID_ONCE_KEY = "sendIfOnce"

    private let repository: KSFDataRepository = KSFDataRepository(uuid: AppDelegate.me.userUuid)

    private var mode: KSFDataRepositorySessionsListMode = .all

    @IBOutlet weak var pullToRefresh: UIRefreshControl!

    @IBAction func tabSelected(_ sender: Any) {
        guard let segmentedControl = sender as? UISegmentedControl else { return }
        self.mode = (segmentedControl.selectedSegmentIndex == 0) ? .all : .favorites

        self.updateResults()
        if let tableView = self.tableView, repository.sessions.count > 0 {
            tableView.scrollToRow(at: IndexPath(item: 0, section: 0), at: .top, animated: true)
        }
    }

    override func viewDidLoad() {
        let loadSessions = !repository.hasModels
        if (loadSessions) {
            self.refreshSessions(self)
        } else {
            self.refreshFavorites()
            self.refreshVotes()
        }
    }

    private func registerUuidIfNeeded() {
        let userDefaults = UserDefaults.standard
        guard !userDefaults.bool(forKey: SessionsViewController.SEND_ID_ONCE_KEY) else { return }
    
        KSFDataRepositoryCompanion().registerUser(uuid: AppDelegate.me.userUuid, onComplete: { (succ) in
            userDefaults.set(succ, forKey: SessionsViewController.SEND_ID_ONCE_KEY)
            return KSFStdlibUnit()
        })
    }

    @IBAction func refreshSessions(_ sender: Any) {
        repository.updateSessions {
            self.pullToRefresh?.endRefreshing()
            self.updateResults()
            self.registerUuidIfNeeded()
            return KSFStdlibUnit()
        }
    }

    private func refreshFavorites() {
        repository.updateFavorites {
            if (self.mode == .favorites) {
                self.updateResults()
            }
            return KSFStdlibUnit()
        }
    }

    private func refreshVotes() {
        repository.updateVotes {return KSFStdlibUnit()}
    }

    private func updateResults() {
        repository.fetchSessions(mode: self.mode)
        self.tableView?.reloadData()
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
                let bucket = selectedPath.section
                let row = selectedPath.row
                guard let session = repository.getSession(bucket: Int32(bucket), idx: Int32(row)) else { return }
                sessionViewController.session = session
            default: break
        }
    }

    override func numberOfSections(in tableView: UITableView) -> Int {
        return repository.sessions.count
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return Int(repository.getSessionBucketSize(bucket: Int32(section)))
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "Session", for: indexPath) as! SessionsTableViewCell
        let bucket = indexPath.section
        let row = indexPath.row
        guard let session = repository.getSession(bucket: Int32(bucket), idx: Int32(row)) else {
            return cell
        }

        cell.setup(for: session)
        return cell
    }
}

class SessionsTableViewCell : UITableViewCell {
    @IBOutlet private weak var dateLabel: UILabel!
    @IBOutlet private weak var titleLabel: UILabel!
    @IBOutlet private weak var icon: UIImageView!

    func setup(for session: KSFSession) {
        titleLabel.text = session.title
        dateLabel.text = KSFUtil.renderDate(date: session.startsAt!)
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
