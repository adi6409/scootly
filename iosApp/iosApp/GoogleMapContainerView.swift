import SwiftUI

/// Thin wrapper to embed the actual map view
struct GoogleMapContainerView: View {
    @ObservedObject var mapData: ScooterMapData

    var body: some View {
        GoogleMapView(mapData: mapData)
            .edgesIgnoringSafeArea(.all)
    }
}
