package org.jetbrains.kotlinconf.ui

import io.ktor.common.client.*
import kotlinx.cinterop.*
import org.jetbrains.kotlinconf.api.*
import org.jetbrains.kotlinconf.data.*
import org.jetbrains.kotlinconf.util.*
import platform.CoreData.*
import platform.Foundation.*
import platform.UIKit.*

@ExportObjCClass
@Suppress("CONFLICTING_OVERLOADS", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "RETURN_TYPE_MISMATCH_ON_INHERITANCE")
class SessionsViewController(aDecoder: NSCoder) :
        UIViewController(aDecoder),
        NSFetchedResultsControllerDelegateProtocol,
        UITableViewDataSourceProtocol,
        UITableViewDelegateProtocol {
    private companion object {
        private val SEND_ID_ONCE_KEY = "sendIfOnce"
    }

    private val repository by lazy {
        DataRepository(appDelegate.userUuid)
    }

    private var mode: SessionsListMode = SessionsListMode.ALL

    private var _fetchedResults: Map<Date, List<Session>>? = null

    lateinit var pullToRefresh: UIRefreshControl

    @ObjCOutlet
    lateinit var tableView: UITableView

    private val sessions: Map<Date, List<Session>>
        get() {
            _fetchedResults?.let { return it }

            var seq = AppContext.allData?.sessions.orEmpty().asSequence()
            if (mode == SessionsListMode.FAVORITES) {
                val favorites = repository.getFavoriteSessionIds()
                seq = seq.filter { it.id in favorites }
            }

            val fetched = seq
                    .sortedWith(SessionsComparator)
                    .sortedBy { it.roomId }
                    .sortedBy { it.id }

            _fetchedResults = seq.toList().groupBy { it.startsAt!! }
            return _fetchedResults!!
        }

    private fun sessionByIndex(i: Int) = sessions.toList().flatMap { it.second }.getOrNull(i)

    override fun initWithCoder(aDecoder: NSCoder) = initBy(SessionsViewController(aDecoder))

    @ObjCAction
    fun tabSelected(sender: ObjCObject?) {
        val segmentedControl = sender.uncheckedCast<UISegmentedControl>()

        mode = if (segmentedControl.selectedSegmentIndex == 0L) SessionsListMode.ALL else SessionsListMode.FAVORITES
        updateResults()
    }

    override fun debugDescription() = "SessionsViewController"

    override fun viewDidLoad() {
        pullToRefresh = UIRefreshControl()
        pullToRefresh.addTarget(this,
                action = NSSelectorFromString("refreshSessions:"),
                forControlEvents = UIControlEventValueChanged)
        tableView.backgroundView = pullToRefresh

        val loadSessions = AppContext.sessionsModels.orEmpty().isEmpty()

        if (loadSessions) {
            refreshSessions(this, showProgressPopup = true)
        } else {
            refreshFavorites()
            refreshVotes()
        }

        registerUuid()
    }

    override fun viewWillAppear(animated: Boolean) {
        updateResults()
    }

    private fun registerUuid() {
        // We have to register uuid each time we enter our app, cause the user db may be reset on server
        runSuspend {
            KotlinConfApi.createUser(appDelegate.userUuid)
        }
    }

    @ObjCAction
    fun refreshSessions(sender: ObjCObject?) {
        refreshSessions(sender, showProgressPopup = false)
    }

    private fun refreshSessions(sender: ObjCObject?, showProgressPopup: Boolean) {
        val progressPopup = if (showProgressPopup) showIndeterminateProgress("Loading sessions…") else null

        fun hideProgress() {
            progressPopup?.hideAnimated(true)
            pullToRefresh.endRefreshing()
        }

        repository.updateSessions {
            hideProgress()
            updateResults()
        }
    }

    private fun refreshFavorites() {
        repository.updateFavorites {
            if (mode == SessionsListMode.FAVORITES) {
                updateResults()
            }
        }
    }

    private fun refreshVotes() {
        repository.updateVotes()
    }

    private fun updateResults() {
        _fetchedResults = null

        try {
            tableView.reloadData()
        } catch (e: NSErrorException) {
            println(e.toString())
            showPopupText("Unable to load sessions.")
        }
    }

    override fun prepareForSegue(segue: UIStoryboardSegue, sender: ObjCObject?) {
        super.prepareForSegue(segue, sender = sender)

        if (segue.identifier == "ShowSession" && sender != null) {
            val selectedCell = sender.uncheckedCast<SessionsTableViewCell>()
            val selectedPath = tableView.indexPathForCell(selectedCell) ?: return

            val sessionViewController = segue.destinationViewController.uncheckedCast<SessionViewController>()
            val row = selectedPath.row
            sessionViewController.session = sessionByIndex(row.toInt()) ?: throw RuntimeException("Session index out of bounds")
        }
    }

    override fun numberOfSectionsInTableView(tableView: UITableView): Long {
        return sessions.size.toLong()
    }

    override fun tableView(tableView: UITableView, didSelectRowAtIndexPath: NSIndexPath) {
        tableView.deselectRowAtIndexPath(didSelectRowAtIndexPath, animated = false)
    }

    override fun tableView(tableView: UITableView, numberOfRowsInSection: Long): Long {
        return sessions.toList()[numberOfRowsInSection.toInt()].second.size.toLong()
    }

    override fun tableView(tableView: UITableView, cellForRowAtIndexPath: NSIndexPath): UITableViewCell {
        val cell = tableView.dequeueReusableCellWithIdentifier(
                "Session", forIndexPath = cellForRowAtIndexPath).uncheckedCast<SessionsTableViewCell>()
        val row = cellForRowAtIndexPath.row
        val session = sessionByIndex(row.toInt())
        if (session != null) {
            cell.setup(session)
        }
        return cell
    }

    override fun tableView(tableView: UITableView, titleForHeaderInSection: Long): String? {
        val date = sessions.toList()[titleForHeaderInSection.toInt()].first
        return renderWeekdayTime(date)
    }
}

private enum class SessionsListMode {
    ALL, FAVORITES
}

@ExportObjCClass
class SessionsTableViewCell(aDecoder: NSCoder) : UITableViewCell(aDecoder) {
    @ObjCOutlet
    lateinit var titleLabel: UILabel
    @ObjCOutlet
    lateinit var subtitleLabel: UILabel

    override fun initWithCoder(aDecoder: NSCoder) = initBy(SessionsTableViewCell(aDecoder))

    fun setup(session: Session) {
        titleLabel.text = session.title
        subtitleLabel.text = session.description
    }
}

@ExportObjCClass
class BreakTableViewCell(aDecoder: NSCoder) : UITableViewCell(aDecoder) {
    @ObjCOutlet
    lateinit var titleLabel: UILabel

    override fun initWithCoder(aDecoder: NSCoder) = initBy(BreakTableViewCell(aDecoder))

    init {
        backgroundColor = UIColor.colorWithPatternImage(UIImage.imageNamed("striped_bg")!!)
    }

    fun setup(session: Session) {
        titleLabel.text = session.title
    }
}
