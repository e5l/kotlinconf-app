import UIKit
import konfSwiftFramework

class SessionsViewController: UITableViewController {
    private static let SEND_ID_ONCE_KEY = "sendIfOnce"
    private lazy var konfService = AppDelegate.me.konfService
    private var mode: KSFKonfServiceSessionsListMode = .all
    private var sessionsTableData: [[KSFSession]] = []

    @IBOutlet weak var pullToRefresh: UIRefreshControl!

    @IBAction func tabSelected(_ sender: Any) {
        guard let segmentedControl = sender as? UISegmentedControl else { return }
        self.mode = (segmentedControl.selectedSegmentIndex == 0) ? .all : .favorites

        self.updateTableContent()
        if let tableView = self.tableView, sessionsTableData.count > 0 {
            tableView.scrollToRow(at: IndexPath(item: 0, section: 0), at: .top, animated: true)
        }
    }

    override func viewDidLoad() {
        if (mode == .all) {
            konfService.register().then(block: { (result) -> KSFStdlibUnit in
                self.refreshSessions(self)
                
                let userDefaults = UserDefaults.standard
                guard !userDefaults.bool(forKey: SessionsViewController.SEND_ID_ONCE_KEY) else { return KUnit }

                userDefaults.set(result as! Bool, forKey: SessionsViewController.SEND_ID_ONCE_KEY)
                return KUnit
            })
            
        } else {
            self.refreshFavorites()
            self.refreshVotes()
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        self.updateTableContent()
    }

    @IBAction func refreshSessions(_ sender: Any) {
        konfService.refresh().then { (result) -> KSFStdlibUnit in
            self.pullToRefresh?.endRefreshing()
            self.updateTableContent()
            return KUnit
        }.catch { (error) -> KSFStdlibUnit in
            self.showPopupText(title: "Failed to refresh")
            return KUnit
        }
    }

    private func refreshFavorites() {
        konfService.refresh().then { (result) -> KSFStdlibUnit in
            if (self.mode == .favorites) {
                self.updateTableContent()
            }
            return KUnit
        }.catch { (error) -> KSFStdlibUnit in
            return KUnit
        }
    }

    private func refreshVotes() {
        konfService.refresh()
    }
    
    /**
     * Prepare TableView state
     */
    
    private func updateTableContent() {
        switch self.mode {
        case .all:
            fillDataWith(sessions: konfService.sessions)
            break
        case .favorites:
            fillDataWith(sessions: konfService.sessions.filter({ (session) -> Bool in
                return konfService.isFavorite(session: session)
            }))
            break
        default:
            break
        }

        self.tableView?.reloadData()        
    }
    
    private func fillDataWith(sessions: [KSFSession]) {
        let sortedSessions = sessions.sorted(by: { (left, right) -> Bool in
            let byComparator = left.compareTo(other: right)
            if byComparator != 0 { return byComparator < 0 }
            if left.roomId != right.roomId { return left.roomId!.compare(right.roomId!).rawValue > 0 }
            if left.id != right.id { return left.id!.compare(right.id!).rawValue > 0 }
            return false
        })

        sessionsTableData = []
        sortedSessions.forEach({ (session) in
            if sessionsTableData.count == 0 ||
                sessionsTableData.last!.first!.startsAt!.compareTo(otherDate: session.startsAt!) != 0 {
                sessionsTableData.append([session]);
                return
            }
            
            sessionsTableData[sessionsTableData.count - 1].append(session)
        })
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
                if (sessionsTableData.count <= bucket || sessionsTableData[bucket].count <= row) { return }
                sessionViewController.session = sessionsTableData[bucket][row]
            default: break
        }
    }

    override func numberOfSections(in tableView: UITableView) -> Int {
        return sessionsTableData.count
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if sessionsTableData.count <= section { return 0 }
        return sessionsTableData[section].count
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "Session", for: indexPath) as! SessionsTableViewCell
        let bucket = indexPath.section
        let row = indexPath.row
        if sessionsTableData.count <= bucket || sessionsTableData[bucket].count <= row { return cell }
        cell.setup(for: sessionsTableData[bucket][row])
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
