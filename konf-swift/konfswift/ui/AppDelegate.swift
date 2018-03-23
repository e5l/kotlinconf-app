import UIKit
import Groot
import konfSwiftFramework

@UIApplicationMain
class AppDelegate: CoreDataResponderBase {
    private static let GENERATE_ID_ONCE_KEY = "generateIdOnce"
    static let UUID_KEY = "vendorId"

    var window: UIWindow?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?
    ) -> Bool {
        initValueTransformers()
        generateUuidIfNeeded()
        return true
    }

    private func initValueTransformers() {
        ValueTransformer.grt_setValueTransformer(withName: "StringToDate") { rawValue in
            guard let str = rawValue as? String else { return rawValue }
            return parseDate(from: str)
        }
    }

    private func generateUuidIfNeeded() {
        doOnce(key: AppDelegate.GENERATE_ID_ONCE_KEY) {
            let uuid = "ios-" + (UIDevice.current.identifierForVendor ?? UUID()).uuidString
            UserDefaults.standard.set(uuid, forKey: AppDelegate.UUID_KEY)
            return true
        }
    }

    var userUuid: String {
        // Should be already set in `generateUuidIfNeeded`
        return UserDefaults.standard.string(forKey: AppDelegate.UUID_KEY)!
    }

    static var me: AppDelegate {
        return UIApplication.shared.delegate as! AppDelegate
    }
}

fileprivate func doOnce(key: String, f: () -> Bool) {
    let userDefaults = UserDefaults.standard
    if (!userDefaults.bool(forKey: key)) {
        if (f()) {
            userDefaults.set(true, forKey: key)
        }
    }
}
