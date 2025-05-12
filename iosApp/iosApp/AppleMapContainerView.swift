import SwiftUI

/// Wrapper to embed the Apple MapKit map view with proper gesture handling
struct AppleMapContainerView: View {
    @ObservedObject var mapData: ScooterMapData
    
    init(mapData: ScooterMapData) {
        self.mapData = mapData
        print("AppleMapContainerView: Init with \(mapData.scooters.count) scooters")
    }

    var body: some View {
        GeometryReader { geometry in
            AppleMapView(mapData: mapData)
                .edgesIgnoringSafeArea(.all)
                // Disable SwiftUI gesture recognizers that might interfere with map gestures
                .contentShape(Rectangle())
                .allowsHitTesting(true)
                // Make the view take up the full available space
                .frame(width: geometry.size.width, height: geometry.size.height)
                .onAppear {
                    print("AppleMapContainerView: onAppear with \(mapData.scooters.count) scooters")
                }
        }
        // Disable SwiftUI's gesture handling to prevent interference
        .gesture(DragGesture(minimumDistance: 0).onChanged { _ in })
        .gesture(MagnificationGesture().onChanged { _ in })
        .gesture(RotationGesture().onChanged { _ in })
    }
}
