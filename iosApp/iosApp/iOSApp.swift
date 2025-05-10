import SwiftUI
import GoogleMaps
import ComposeApp

@main
struct iOSApp: App {

    init() {
        KoinKt.doInitKoin()
        GMSServices.provideAPIKey(BuildSettings.googleMapsApiKey)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
