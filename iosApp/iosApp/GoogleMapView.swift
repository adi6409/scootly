import SwiftUI
import GoogleMaps
import GoogleMapsUtils
import CoreLocation

struct GoogleMapView: UIViewRepresentable {
    @ObservedObject var mapData: ScooterMapData

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    func makeUIView(context: Context) -> GMSMapView {
        let mapView = GMSMapView(frame: .zero)
        mapView.delegate = context.coordinator
        mapView.settings.myLocationButton   = true
        mapView.isMyLocationEnabled         = true
        mapView.settings.scrollGestures     = true
        mapView.settings.zoomGestures       = true
        mapView.settings.tiltGestures       = true
        mapView.settings.rotateGestures     = true
        mapView.isUserInteractionEnabled    = true

        // Location updates
        context.coordinator.mapView         = mapView
        context.coordinator.locationManager.delegate = context.coordinator
        context.coordinator.locationManager.requestWhenInUseAuthorization()
        context.coordinator.locationManager.startUpdatingLocation()

        return mapView
    }

    func updateUIView(_ uiView: GMSMapView, context: Context) {
        context.coordinator.updateMarkers()
    }

    class Coordinator: NSObject, GMSMapViewDelegate, CLLocationManagerDelegate {
        var parent: GoogleMapView
        var mapView: GMSMapView?
        var locationManager = CLLocationManager()
        var clusterManager: GMUClusterManager?
        var hasCenteredOnce = false

        init(_ parent: GoogleMapView) {
            self.parent = parent
            super.init()
        }

        func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
            guard let loc = locations.last, !hasCenteredOnce else { return }
            hasCenteredOnce = true
            let camera = GMSCameraPosition.camera(
                withLatitude: loc.coordinate.latitude,
                longitude: loc.coordinate.longitude,
                zoom: 15.0
            )
            mapView?.animate(to: camera)
            updateMarkers()
        }

        func updateMarkers() {
            guard let mapView = mapView else { return }
            if clusterManager == nil {
                let iconGen  = GMUDefaultClusterIconGenerator()
                let algo     = GMUNonHierarchicalDistanceBasedAlgorithm()
                let renderer = GMUDefaultClusterRenderer(
                    mapView: mapView,
                    clusterIconGenerator: iconGen
                )
                clusterManager = GMUClusterManager(
                    map: mapView,
                    algorithm: algo,
                    renderer: renderer
                )
            }
            guard mapView.frame.size != .zero else {
                DispatchQueue.main.async { self.updateMarkers() }
                return
            }
            clusterManager?.clearItems()

            let region = mapView.projection.visibleRegion()
            let bounds = GMSCoordinateBounds(
                coordinate: region.nearLeft,
                coordinate: region.farRight
            )
            let expanded = expand(bounds: bounds, by: 0.01)

            for s in parent.mapData.scooters {
                if let lat = s.latitude as? CLLocationDegrees,
                   let lng = s.longitude as? CLLocationDegrees,
                   expanded.contains(CLLocationCoordinate2D(latitude: lat, longitude: lng))
                {
                    let item = POIItem(
                        position: CLLocationCoordinate2D(latitude: lat, longitude: lng),
                        name: s.providerName
                    )
                    clusterManager?.add(item)
                }
            }
            clusterManager?.cluster()
        }

        private func expand(bounds: GMSCoordinateBounds, by buffer: CLLocationDegrees) -> GMSCoordinateBounds {
            let sw = CLLocationCoordinate2D(
                latitude: bounds.southWest.latitude - buffer,
                longitude: bounds.southWest.longitude - buffer
            )
            let ne = CLLocationCoordinate2D(
                latitude: bounds.northEast.latitude + buffer,
                longitude: bounds.northEast.longitude + buffer
            )
            return GMSCoordinateBounds(coordinate: sw, coordinate: ne)
        }

        class POIItem: NSObject, GMUClusterItem {
            let position: CLLocationCoordinate2D
            let name: String
            init(position: CLLocationCoordinate2D, name: String) {
                self.position = position
                self.name = name
            }
        }
    }
}
