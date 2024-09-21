import SwiftUI
import Shared

@main
struct iOSApp: App {

    @Environment(\.scenePhase) private var scenePhase

    var body: some Scene {
        WindowGroup {
            ContentView()
        }.onChange(of: scenePhase) { newPhase in
            switch newPhase {
            case .active:
                print("Приложение активно")
            case .inactive:
                print("Приложение неактивно")
                // Здесь можно выполнять действия, когда приложение приостановлено (например, входящий звонок)
            case .background:
                print("Приложение в фоне")
                // Здесь можно сохранять данные или освобождать ресурсы, когда приложение переходит в фоновый режим
            @unknown default:
                print("Неизвестное состояние")
            }
        }
    }
}
