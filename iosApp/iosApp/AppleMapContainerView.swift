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
                .frame(width: geometry.size.width, height: geometry.size.height)
                .onAppear {
                    print("AppleMapContainerView: onAppear with \(mapData.scooters.count) scooters")
                }
                // Add a transparent overlay to handle gesture conflicts
                .overlay(
                    Color.clear
                        .contentShape(Rectangle())
                        .gesture(
                            DragGesture(minimumDistance: 0)
                                .onChanged { _ in }
                                .onEnded { _ in }
                        )
                )
        }
    }
}
