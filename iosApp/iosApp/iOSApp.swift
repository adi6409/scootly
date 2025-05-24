import SwiftUI
import ComposeApp
import CoreLocation
import FirebaseCore
import FirebaseAuth
import FirebaseFirestore
import GoogleSignIn

class LocationManager: NSObject, CLLocationManagerDelegate, ObservableObject {
    private let locationManager = CLLocationManager()
    
    override init() {
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.requestWhenInUseAuthorization()
    }
}

@main
struct iOSApp: App {
    @StateObject private var locationManager = LocationManager()
    
    init() {
        FirebaseApp.configure()
        KoinKt.doInitKoin()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
