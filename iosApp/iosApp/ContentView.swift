import UIKit
import SwiftUI
import ComposeApp

// Shared instance to maintain state across recreations
private let sharedMapData = ScooterMapData()

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // ← Call the new, 3-arg version of MainViewController:
        MainViewControllerKt.MainViewController { providers, scooters, onScooterClick in
            print("ContentView: Received \(scooters.count) scooters from Kotlin")
            if let firstScooter = scooters.first {
                print("ContentView: First scooter - id: \(firstScooter.id), provider: \(firstScooter.providerName), lat: \(firstScooter.latitude), lng: \(firstScooter.longitude)")
            }
            
            // Use the shared instance to maintain state
            DispatchQueue.main.async {
                sharedMapData.providers = providers
                sharedMapData.scooters = scooters
                // Convert KotlinUnit returning function to Void returning function
                sharedMapData.onScooterSelected = { scooter in
                    print("Scooter Selected in ContentView: \(scooter?.providerName ?? "nil") (ID: \(scooter?.id ?? "nil"))")
                    
                    // Ensure we're on the main thread when calling back to Kotlin
                    DispatchQueue.main.async {
                        _ = onScooterClick(scooter)
                    }
                    
                    // Also print to verify the callback is being called
                    print("onScooterSelected callback executed")
                }
                print("ContentView: Set \(sharedMapData.scooters.count) scooters in ScooterMapData")
            }
            
            // Create a UIHostingController with gesture handling configuration
            // Use AppleMapContainerView since we don't have a Google Maps API key
            let hostingController = UIHostingController(rootView: AppleMapContainerView(mapData: sharedMapData))
            return hostingController
        }
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // This method is called when the SwiftUI view is updated
        // We don't need to do anything here as our shared data is updated directly
    }
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard)
    }
}
