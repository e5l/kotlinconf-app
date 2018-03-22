package org.jetbrains.kotlinconf.ui

import kotlinx.cinterop.*
import org.jetbrains.kotlinconf.data.*
import org.jetbrains.kotlinconf.util.*
import platform.UIKit.*
import platform.Foundation.*
import platform.CoreData.*
import kotlin.system.exitProcess

val appDelegate: AppDelegate by lazy {
    UIApplication.sharedApplication.delegate.uncheckedCast<AppDelegate>() // as AppDelegate
}

@ExportObjCClass
class AppDelegate : UIResponder(), UIApplicationDelegateProtocol {

    companion object : UIResponderMeta(), UIApplicationDelegateProtocolMeta {}

    private val GENERATE_ID_ONCE_KEY = "generateIdOnce"
    private val UUID_KEY = "vendorId"

    private var _window: UIWindow? = null

    val managedObjectContext: NSManagedObjectContext by lazy {
        val managedObjectContext = NSManagedObjectContext(NSMainQueueConcurrencyType)
        managedObjectContext.persistentStoreCoordinator = persistentStoreCoordinator
        managedObjectContext
    }

    // Should be already set in `generateUuidIfNeeded`
    val userUuid: String
        get() = NSUserDefaults.standardUserDefaults.stringForKey(UUID_KEY)!!

    private val applicationDocumentsDirectory: NSURL by lazy {
        NSFileManager.defaultManager
                .URLsForDirectory(NSDocumentDirectory, inDomains = NSUserDomainMask)
                .lastObject!!.uncheckedCast<NSURL>()
    }

    private val managedObjectModel: NSManagedObjectModel by lazy {
        val modelURL = NSBundle.mainBundle.URLForResource("konfswift", withExtension = "momd")!!
        NSManagedObjectModel(modelURL)
    }

    private val persistentStoreCoordinator: NSPersistentStoreCoordinator by lazy {
        val coordinator = NSPersistentStoreCoordinator(managedObjectModel)
        val url = applicationDocumentsDirectory.URLByAppendingPathComponent("konfswift.sqlite")

        try {
            nsTry { errorPtr ->
                coordinator.addPersistentStoreWithType(
                        /*NSSQLiteStoreType*/ storeType = "SQLite",
                        configuration = null, URL = url, options = null,
                        error = errorPtr
                )
            }
        } catch (cause: NSErrorException) {
            val wrappedError = NSError.errorWithDomain("KonfSwift", code = 9999, userInfo = null)
            log("Unresolved error $wrappedError")
            exitProcess(0)
        }

        coordinator
    }

    override fun init() = initBy(AppDelegate())

    override fun window() = _window

    override fun setWindow(window: UIWindow?) { _window = window }

    override fun application(application: UIApplication, didFinishLaunchingWithOptions: NSDictionary?): Boolean {
        generateUuidIfNeeded()
        return true
    }

    override fun applicationDidBecomeActive(application: UIApplication) {
        refreshBadgeStatus()
    }

    override fun applicationWillTerminate(application: UIApplication) {
        saveContext()
    }

    private fun refreshBadgeStatus() {
        val service = KonfService(errorHandler = { log(it.localizedDescription) })
        service.shouldShowBadge { showBadge ->
            NSUserDefaults.standardUserDefaults.setBool(showBadge, forKey = AboutViewController.SHOW_BADGE_KEY)
        }
    }

    private fun generateUuidIfNeeded() {
        doOnce(GENERATE_ID_ONCE_KEY) {
            val uuid = "ios-" + (UIDevice.currentDevice.identifierForVendor ?: NSUUID()).UUIDString
            NSUserDefaults.standardUserDefaults.setObject(uuid.toNSString(), forKey = UUID_KEY)
            return@doOnce true
        }
    }

    private fun saveContext() {
        if (managedObjectContext.hasChanges) {
            managedObjectContext.save(null)
        }
    }
}

private fun doOnce(key: String, block: () -> Boolean) {
    val userDefaults = NSUserDefaults.standardUserDefaults
    if (userDefaults.boolForKey(key)) return

    if (block()) {
        userDefaults.setBool(true, forKey = key)
    }
}
