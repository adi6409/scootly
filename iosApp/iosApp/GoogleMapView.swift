import SwiftUI
import GoogleMaps
import GoogleMapsUtils
import CoreLocation
import UIKit

struct GoogleMapView: UIViewRepresentable {
    @ObservedObject var mapData: ScooterMapData

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    func makeUIView(context: Context) -> GMSMapView {
        let mapView = GMSMapView()
        mapView.delegate = context.coordinator
        mapView.settings.myLocationButton   = true
        mapView.isMyLocationEnabled         = true
        mapView.settings.compassButton      = true
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
        // Debug: Print the number of scooters in mapData
        print("updateUIView: ScooterMapData contains \(mapData.scooters.count) scooters")
        
        // Force update markers when scooter data changes
        if context.coordinator.lastScooterCount != mapData.scooters.count {
            context.coordinator.lastScooterCount = mapData.scooters.count
            context.coordinator.updateMarkers()
        }
    }

    class Coordinator: NSObject, GMSMapViewDelegate, CLLocationManagerDelegate, GMUClusterRendererDelegate {
        var parent: GoogleMapView
        var mapView: GMSMapView?
        var locationManager = CLLocationManager()
        var clusterManager: GMUClusterManager?
        var hasCenteredOnce = false
        var lastScooterCount = 0

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
                // Create a default renderer
                let renderer = GMUDefaultClusterRenderer(
                    mapView: mapView,
                    clusterIconGenerator: iconGen
                )
                
                // Configure the renderer to use custom icons
                renderer.delegate = self
                clusterManager = GMUClusterManager(
                    map: mapView,
                    algorithm: algo,
                    renderer: renderer
                )
                
                // Set up delegate for map gestures
                mapView.delegate = self
                
                // Set up tap handlers for the cluster manager
                clusterManager?.setMapDelegate(self)
            }
            guard mapView.frame.size != .zero else {
                DispatchQueue.main.async { self.updateMarkers() }
                return
            }
            clusterManager?.clearItems()

            print("Rendering \(parent.mapData.scooters.count) scooters")
            
            // Debug: Print the first few scooters
            if !parent.mapData.scooters.isEmpty {
                for i in 0..<min(3, parent.mapData.scooters.count) {
                    let s = parent.mapData.scooters[i]
                    print("Scooter \(i): id=\(s.id), provider=\(s.providerName), lat=\(s.latitude), lng=\(s.longitude)")
                }
            }
            
            // Add ALL scooters to the map without any filtering
            let totalScooters = parent.mapData.scooters.count
            
            for s in parent.mapData.scooters {
                let lat = s.latitude
                let lng = s.longitude
                let position = CLLocationCoordinate2D(latitude: lat, longitude: lng)
                
                // Add the marker without any bounds check
                let item = POIItem(
                    position: position,
                    name: s.providerName,
                    iconUrl: s.providerIcon
                )
                clusterManager?.add(item)
            }
            
            print("Added \(totalScooters) markers to the map")
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
        
        // MARK: - GMSMapViewDelegate methods for gesture handling
        
        func mapView(_ mapView: GMSMapView, didChange position: GMSCameraPosition) {
            // This is called when the camera position changes during gestures
            // We can use this to update markers as the map moves
            // Update markers when camera position changes
            updateMarkers()
        }
        
        func mapView(_ mapView: GMSMapView, idleAt position: GMSCameraPosition) {
            // Called when the map becomes idle after gestures
            updateMarkers()
        }
        
        func mapView(_ mapView: GMSMapView, willMove gesture: Bool) {
            // Called when the map is about to move
            // The gesture parameter is true if the movement is due to a gesture
            // and false if it's due to programmatic animation
        }
        
        func mapView(_ mapView: GMSMapView, didTap marker: GMSMarker) -> Bool {
            print("GoogleMapView: Marker tapped")
            
            // Handle marker taps
            if let poiItem = marker.userData as? POIItem {
                print("GoogleMapView: POIItem found - \(poiItem.name)")
                
                // Find the corresponding scooter
                let matchingScooter = parent.mapData.scooters.first { scooter in
                    let scooterPosition = CLLocationCoordinate2D(latitude: scooter.latitude, longitude: scooter.longitude)
                    let positionMatches = abs(poiItem.position.latitude - scooterPosition.latitude) < 0.0001 && 
                                          abs(poiItem.position.longitude - scooterPosition.longitude) < 0.0001
                    let nameMatches = poiItem.name == scooter.providerName
                    
                    if positionMatches && nameMatches {
                        return true
                    }
                    return false
                }
                
                // Call the callback with the selected scooter
                if let scooter = matchingScooter {
                    print("GoogleMapView: Scooter selected: \(scooter.providerName) (ID: \(scooter.id))")
                    
                    // Make sure the callback exists
                    if parent.mapData.onScooterSelected != nil {
                        print("GoogleMapView: Calling onScooterSelected callback")
                        parent.mapData.onScooterSelected?(scooter)
                    } else {
                        print("GoogleMapView: onScooterSelected callback is nil")
                    }
                } else {
                    print("GoogleMapView: No matching scooter found")
                }
            } else {
                print("GoogleMapView: Marker userData is not a POIItem")
            }
            
            return false // Return false to allow the default info window
        }
        
        // MARK: - GMUClusterRendererDelegate methods
        
        func renderer(_ renderer: GMUClusterRenderer, willRenderMarker marker: GMSMarker) {
            // Check if this is a marker for a cluster item (not a cluster)
            if let poiItem = marker.userData as? POIItem {
                // Set marker properties
                marker.title = poiItem.name
                
                // Set a default icon immediately so marker is visible
                marker.icon = UIImage(systemName: "mappin.circle.fill")?.withTintColor(.red, renderingMode: .alwaysOriginal)
                
                // Load custom icon for the marker directly
                loadMarkerIcon(for: poiItem.name, url: poiItem.iconUrl) { [weak marker] image in
                    // Only update if we got a valid image and the marker still exists
                    if let image = image, let marker = marker {
                        DispatchQueue.main.async {
                            marker.icon = image
                        }
                    }
                }
            }
        }
        
        func renderer(_ renderer: GMUClusterRenderer, markerFor object: Any) -> GMSMarker? {
            // Return nil to use the default marker created by the renderer
            return nil
        }

        // MARK: - Icon loading utilities
        
        private var iconCache = [String: UIImage]()
        private let markerIconSizePx: CGFloat = 48.0
        
        private func loadMarkerIcon(for providerName: String, url: String?, completion: @escaping (UIImage?) -> Void) {
            // Return cached icon if available
            if let cachedIcon = iconCache[providerName] {
                completion(cachedIcon)
                return
            }
            
            // Use default icon if URL is nil
            guard let iconUrl = url, let url = URL(string: iconUrl) else {
                let defaultIcon = UIImage(systemName: "mappin.circle.fill")?.withTintColor(.red, renderingMode: .alwaysOriginal)
                iconCache[providerName] = defaultIcon
                completion(defaultIcon)
                return
            }
            
            // Download and cache icon
            URLSession.shared.dataTask(with: url) { [weak self] data, response, error in
                guard let self = self, let data = data, error == nil,
                      let image = UIImage(data: data) else {
                    DispatchQueue.main.async {
                        let defaultIcon = UIImage(systemName: "mappin.circle.fill")?.withTintColor(.red, renderingMode: .alwaysOriginal)
                        self?.iconCache[providerName] = defaultIcon
                        completion(defaultIcon)
                    }
                    return
                }
                
                // Resize image to appropriate size for marker
                let resizedImage = self.resizeImage(image: image, targetSize: CGSize(width: self.markerIconSizePx, height: self.markerIconSizePx))
                
                // Cache and return the icon
                DispatchQueue.main.async {
                    self.iconCache[providerName] = resizedImage
                    completion(resizedImage)
                }
            }.resume()
        }
        
        private func resizeImage(image: UIImage, targetSize: CGSize) -> UIImage {
            let size = image.size
            let widthRatio = targetSize.width / size.width
            let heightRatio = targetSize.height / size.height
            let ratio = min(widthRatio, heightRatio)
            
            let newSize = CGSize(width: size.width * ratio, height: size.height * ratio)
            let rect = CGRect(origin: .zero, size: newSize)
            
            UIGraphicsBeginImageContextWithOptions(newSize, false, 0)
            image.draw(in: rect)
            let newImage = UIGraphicsGetImageFromCurrentImageContext()
            UIGraphicsEndImageContext()
            
            return newImage ?? image
        }
        
        class POIItem: NSObject, GMUClusterItem {
            let position: CLLocationCoordinate2D
            let name: String
            let iconUrl: String?
            
            init(position: CLLocationCoordinate2D, name: String, iconUrl: String? = nil) {
                self.position = position
                self.name = name
                self.iconUrl = iconUrl
            }
        }
    }
}
