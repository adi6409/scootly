import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // ← Call the new, 1-arg version of MainViewController:
        MainViewControllerKt.MainViewController { providers, scooters in
            let mapData = ScooterMapData()
            mapData.providers = providers
            mapData.scooters  = scooters
            return UIHostingController(rootView: GoogleMapContainerView(mapData: mapData))
        }
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard)
    }
}
