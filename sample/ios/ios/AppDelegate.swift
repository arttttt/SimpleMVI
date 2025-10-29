import UIKit
import Shared // ← ваш KMM-модуль (имя может отличаться)

final class AppDelegate: NSObject, UIApplicationDelegate {

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {

        // strictMode = !DEBUG
        #if DEBUG
        let strictMode = false
        #else
        let strictMode = true
        #endif
        
        ConfigDslKt.configureSimpleMVI { builder in
            builder.strictMode = strictMode
        }

        return true
    }
}

