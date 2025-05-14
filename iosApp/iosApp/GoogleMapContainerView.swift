import SwiftUI
import ComposeApp

/// Thin wrapper to embed the actual map view
struct GoogleMapContainerView: View {
    @ObservedObject var mapData: ScooterMapData
    
    init(mapData: ScooterMapData) {
        self.mapData = mapData
        print("GoogleMapContainerView: Init with \(mapData.scooters.count) scooters")
    }

    var body: some View {
        GoogleMapView(mapData: mapData)
            .edgesIgnoringSafeArea(.all)
            .onAppear {
                print("GoogleMapContainerView: onAppear with \(mapData.scooters.count) scooters")
            }
    }
}
