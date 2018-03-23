import UIKit
import CoreData

open class CoreDataResponderBase : UIResponder, UIApplicationDelegate {
    public func applicationWillTerminate(_ application: UIApplication) {
        self.saveContext()
    }

    private lazy var applicationDocumentsDirectory: URL = {
        // @property(class, readonly, strong) NSFileManager *defaultManager;
        // - (NSArray<NSURL *> *)URLsForDirectory:(NSSearchPathDirectory)directory inDomains:(NSSearchPathDomainMask)domainMask;
        let urls = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
        return urls[urls.count-1]
    }()

    private lazy var managedObjectModel: NSManagedObjectModel = {
        let modelURL = Bundle.main.url(forResource: "konfswift", withExtension: "momd")!
        return NSManagedObjectModel(contentsOf: modelURL)!
    }()

    private lazy var persistentStoreCoordinator: NSPersistentStoreCoordinator = {
        let coordinator = NSPersistentStoreCoordinator(managedObjectModel: self.managedObjectModel)
        let url = self.applicationDocumentsDirectory.appendingPathComponent("SingleViewCoreData.sqlite")
        var failureReason = "There was an error creating or loading the application's saved data."
        do {
            try coordinator.addPersistentStore(ofType: NSSQLiteStoreType, configurationName: nil, at: url, options: nil)
        } catch {
            let wrappedError = NSError(domain: "KonfSwift", code: 9999, userInfo: [
                NSLocalizedDescriptionKey: "Failed to initialize the application's saved data",
                NSLocalizedFailureReasonErrorKey: failureReason,
                NSUnderlyingErrorKey: error
            ])

            NSLog("Unresolved error \(wrappedError), \(wrappedError.userInfo)")
            abort()
        }

        return coordinator
    }()

    lazy var managedObjectContext: NSManagedObjectContext = {
        let coordinator = self.persistentStoreCoordinator
        var managedObjectContext = NSManagedObjectContext(concurrencyType: .mainQueueConcurrencyType)
        managedObjectContext.persistentStoreCoordinator = coordinator
        return managedObjectContext
    }()

    func saveContext() {
        if managedObjectContext.hasChanges {
            try? managedObjectContext.save()
        }
    }
}
