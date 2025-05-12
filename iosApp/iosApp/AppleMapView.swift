import SwiftUI
import MapKit
import CoreLocation
import UIKit

struct AppleMapView: UIViewRepresentable {
    @ObservedObject var mapData: ScooterMapData

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    func makeUIView(context: Context) -> MKMapView {
        let mapView = MKMapView()
        mapView.delegate = context.coordinator
        mapView.showsUserLocation = true // Replaces isMyLocationEnabled
        mapView.isRotateEnabled = true   // Replaces rotateGestures
        mapView.isPitchEnabled = true    // Replaces tiltGestures
        mapView.isZoomEnabled = true     // Replaces zoomGestures
        mapView.isScrollEnabled = true   // Replaces scrollGestures
        mapView.isUserInteractionEnabled = true
        
        // Add explicit gesture recognizers to ensure they work properly
        let panGesture = UIPanGestureRecognizer(target: context.coordinator, action: #selector(Coordinator.handlePanGesture(_:)))
        panGesture.delegate = context.coordinator
        mapView.addGestureRecognizer(panGesture)
        
        let pinchGesture = UIPinchGestureRecognizer(target: context.coordinator, action: #selector(Coordinator.handlePinchGesture(_:)))
        pinchGesture.delegate = context.coordinator
        mapView.addGestureRecognizer(pinchGesture)
        
        let rotationGesture = UIRotationGestureRecognizer(target: context.coordinator, action: #selector(Coordinator.handleRotationGesture(_:)))
        rotationGesture.delegate = context.coordinator
        mapView.addGestureRecognizer(rotationGesture)

        // Location updates
        context.coordinator.mapView = mapView
        context.coordinator.locationManager.delegate = context.coordinator
        context.coordinator.locationManager.requestWhenInUseAuthorization()
        context.coordinator.locationManager.startUpdatingLocation()

        return mapView
    }

    func updateUIView(_ uiView: MKMapView, context: Context) {
        // Debug: Print the number of scooters in mapData
        print("updateUIView: ScooterMapData contains \(mapData.scooters.count) scooters")
        
        // Force update annotations when scooter data changes
        if context.coordinator.lastScooterCount != mapData.scooters.count {
            context.coordinator.lastScooterCount = mapData.scooters.count
            context.coordinator.updateAnnotations()
        }
    }

    class Coordinator: NSObject, MKMapViewDelegate, CLLocationManagerDelegate, UIGestureRecognizerDelegate {
        var parent: AppleMapView
        var mapView: MKMapView?
        var locationManager = CLLocationManager()
        var hasCenteredOnce = false
        var lastScooterCount = 0
        
        init(_ parent: AppleMapView) {
            self.parent = parent
            super.init()
        }

        func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
            guard let loc = locations.last, !hasCenteredOnce else { return }
            hasCenteredOnce = true
            
            // Center the map on user location (replaces GMSCameraPosition)
            let region = MKCoordinateRegion(
                center: loc.coordinate,
                span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01)
            )
            mapView?.setRegion(region, animated: true)
            updateAnnotations()
        }

        func updateAnnotations() {
            guard let mapView = mapView else { return }
            
            // Remove existing annotations
            let existingAnnotations = mapView.annotations.filter { !($0 is MKUserLocation) }
            mapView.removeAnnotations(existingAnnotations)
            
            // Get the visible region with a buffer
            let visibleRegion = mapView.region
            let expandedRegion = expandRegion(visibleRegion, byFactor: 0.2) // 20% buffer
            
            // Filter scooters to only include those within the expanded visible region
            let visibleScooters = parent.mapData.scooters.filter { scooter in
                let coordinate = CLLocationCoordinate2D(latitude: scooter.latitude, longitude: scooter.longitude)
                return coordinateIsInRegion(coordinate, region: expandedRegion)
            }
            
            print("Rendering \(visibleScooters.count) of \(parent.mapData.scooters.count) scooters")
            
            // Debug: Print the first few visible scooters
            if !visibleScooters.isEmpty {
                for i in 0..<min(3, visibleScooters.count) {
                    let s = visibleScooters[i]
                    print("Visible scooter \(i): id=\(s.id), provider=\(s.providerName), lat=\(s.latitude), lng=\(s.longitude)")
                }
            }
            
            // Create annotations for visible scooters
            var annotations: [ScooterAnnotation] = []
            
            for s in visibleScooters {
                let coordinate = CLLocationCoordinate2D(latitude: s.latitude, longitude: s.longitude)
                let annotation = ScooterAnnotation(
                    coordinate: coordinate,
                    title: s.providerName,
                    subtitle: s.id,
                    iconUrl: s.providerIcon
                )
                annotations.append(annotation)
            }
            
            // Add annotations to map
            mapView.addAnnotations(annotations)
            
            // Handle clustering
            clusterAnnotations(on: mapView, annotations: annotations)
            
            print("Added \(annotations.count) annotations to the map")
        }
        
        // Helper function to expand a region by a factor
        private func expandRegion(_ region: MKCoordinateRegion, byFactor factor: Double) -> MKCoordinateRegion {
            let latDelta = region.span.latitudeDelta * (1 + factor)
            let lonDelta = region.span.longitudeDelta * (1 + factor)
            return MKCoordinateRegion(
                center: region.center,
                span: MKCoordinateSpan(latitudeDelta: latDelta, longitudeDelta: lonDelta)
            )
        }
        
        // Helper function to check if a coordinate is within a region
        private func coordinateIsInRegion(_ coordinate: CLLocationCoordinate2D, region: MKCoordinateRegion) -> Bool {
            let halfLatDelta = region.span.latitudeDelta / 2.0
            let halfLonDelta = region.span.longitudeDelta / 2.0
            
            let minLat = region.center.latitude - halfLatDelta
            let maxLat = region.center.latitude + halfLatDelta
            let minLon = region.center.longitude - halfLonDelta
            let maxLon = region.center.longitude + halfLonDelta
            
            return (coordinate.latitude >= minLat &&
                    coordinate.latitude <= maxLat &&
                    coordinate.longitude >= minLon &&
                    coordinate.longitude <= maxLon)
        }
        
        // MARK: - Clustering Support
        
        private func clusterAnnotations(on mapView: MKMapView, annotations: [ScooterAnnotation]) {
            // This is a simple implementation of clustering
            // For production apps, consider using a more sophisticated algorithm
            // or a third-party library like Cluster (https://github.com/efremidze/Cluster)
            
            // Skip clustering if zoom level is high enough
            let region = mapView.region
            let span = region.span
            if span.latitudeDelta < 0.01 && span.longitudeDelta < 0.01 {
                // At high zoom levels, we already added individual annotations
                return
            }
            
            // Group annotations by proximity
            var clusters: [[ScooterAnnotation]] = []
            var processedAnnotations = Set<Int>()
            
            for (index, annotation) in annotations.enumerated() {
                if processedAnnotations.contains(index) { continue }
                
                var cluster: [ScooterAnnotation] = [annotation]
                processedAnnotations.insert(index)
                
                for (otherIndex, otherAnnotation) in annotations.enumerated() {
                    if processedAnnotations.contains(otherIndex) { continue }
                    
                    let distance = self.distance(from: annotation.coordinate, to: otherAnnotation.coordinate)
                    if distance <= 500 { // Distance in meters for clustering
                        cluster.append(otherAnnotation)
                        processedAnnotations.insert(otherIndex)
                    }
                }
                
                clusters.append(cluster)
            }
            
            // Remove individual annotations and add cluster annotations
            mapView.removeAnnotations(annotations)
            
            for cluster in clusters {
                if cluster.count > 1 {
                    // Calculate the center of the cluster
                    let coordinates = cluster.map { $0.coordinate }
                    let centerCoordinate = calculateCenter(for: coordinates)
                    
                    // Create a cluster annotation
                    let clusterAnnotation = ClusterAnnotation(
                        coordinate: centerCoordinate,
                        memberAnnotations: cluster,
                        count: cluster.count
                    )
                    mapView.addAnnotation(clusterAnnotation)
                } else {
                    // Add individual annotation
                    mapView.addAnnotation(cluster[0])
                }
            }
        }
        
        private func distance(from coord1: CLLocationCoordinate2D, to coord2: CLLocationCoordinate2D) -> CLLocationDistance {
            let location1 = CLLocation(latitude: coord1.latitude, longitude: coord1.longitude)
            let location2 = CLLocation(latitude: coord2.latitude, longitude: coord2.longitude)
            return location1.distance(from: location2)
        }
        
        private func calculateCenter(for coordinates: [CLLocationCoordinate2D]) -> CLLocationCoordinate2D {
            var totalLat: Double = 0
            var totalLon: Double = 0
            
            for coordinate in coordinates {
                totalLat += coordinate.latitude
                totalLon += coordinate.longitude
            }
            
            return CLLocationCoordinate2D(
                latitude: totalLat / Double(coordinates.count),
                longitude: totalLon / Double(coordinates.count)
            )
        }
        
        // MARK: - Gesture Handling
        
        @objc func handlePanGesture(_ gesture: UIPanGestureRecognizer) {
            guard let mapView = mapView else { return }
            
            switch gesture.state {
            case .began:
                // Disable other gesture recognizers during pan
                mapView.isScrollEnabled = false
            case .changed:
                // Handle pan movement
                let translation = gesture.translation(in: mapView)
                
                // Convert translation to coordinate offset
                let mapWidth = mapView.frame.size.width
                let mapHeight = mapView.frame.size.height
                let region = mapView.region
                
                // Calculate how much the map should move
                // For latitude, moving finger down (positive y) should move map down (negative latitude)
                let latitudeDelta = translation.y / mapHeight * region.span.latitudeDelta
                // For longitude, moving finger right (positive x) should move map left (negative longitude)
                let longitudeDelta = -translation.x / mapWidth * region.span.longitudeDelta
                
                // Create new center coordinate
                let newCenter = CLLocationCoordinate2D(
                    latitude: region.center.latitude + latitudeDelta,
                    longitude: region.center.longitude + longitudeDelta
                )
                
                // Update map region
                mapView.setCenter(newCenter, animated: false)
                
                // Reset translation for continuous movement
                gesture.setTranslation(.zero, in: mapView)
                
            case .ended, .cancelled, .failed:
                // Re-enable scrolling
                mapView.isScrollEnabled = true
                // Update annotations after pan gesture ends
                updateAnnotations()
            default:
                break
            }
        }
        
        @objc func handlePinchGesture(_ gesture: UIPinchGestureRecognizer) {
            guard let mapView = mapView else { return }
            
            switch gesture.state {
            case .began:
                // Disable built-in zoom during our custom pinch
                mapView.isZoomEnabled = false
            case .changed:
                // Get the pinch scale
                let scale = gesture.scale
                
                // Get current region
                var region = mapView.region
                
                // Calculate new span based on pinch scale
                // Scale < 1 means zoom in, scale > 1 means zoom out
                let factor = 1.0 / scale
                
                region.span.latitudeDelta *= factor
                region.span.longitudeDelta *= factor
                
                // Update map region
                mapView.setRegion(region, animated: false)
                
                // Reset scale for continuous zooming
                gesture.scale = 1.0
                
            case .ended, .cancelled, .failed:
                // Re-enable zooming
                mapView.isZoomEnabled = true
                // Update annotations after zoom gesture ends
                updateAnnotations()
            default:
                break
            }
        }
        
        @objc func handleRotationGesture(_ gesture: UIRotationGestureRecognizer) {
            guard let mapView = mapView else { return }
            
            switch gesture.state {
            case .began:
                // Disable built-in rotation during our custom rotation
                mapView.isRotateEnabled = false
            case .changed:
                // Get the rotation angle
                let rotation = gesture.rotation
                
                // Apply rotation to the map's camera
                var camera = mapView.camera
                camera.heading += rotation * (180 / .pi) // Convert radians to degrees
                mapView.setCamera(camera, animated: false)
                
                // Reset rotation for continuous rotating
                gesture.rotation = 0
                
            case .ended, .cancelled, .failed:
                // Re-enable rotation
                mapView.isRotateEnabled = true
                // Update annotations after rotation gesture ends
                updateAnnotations()
            default:
                break
            }
        }
        
        // MARK: - UIGestureRecognizerDelegate
        
        func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool {
            // Allow simultaneous recognition of gestures
            // This is important for complex gestures like pinch-and-pan
            return true
        }
        
        // MARK: - MKMapViewDelegate methods
        
        func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
            // Skip user location annotation
            if annotation is MKUserLocation {
                return nil
            }
            
            // Handle cluster annotations
            if let clusterAnnotation = annotation as? ClusterAnnotation {
                let identifier = "cluster"
                var annotationView = mapView.dequeueReusableAnnotationView(withIdentifier: identifier) as? MKMarkerAnnotationView
                
                if annotationView == nil {
                    annotationView = MKMarkerAnnotationView(annotation: clusterAnnotation, reuseIdentifier: identifier)
                    annotationView?.displayPriority = .defaultHigh
                    annotationView?.markerTintColor = .blue
                }
                
                annotationView?.annotation = clusterAnnotation
                annotationView?.glyphText = "\(clusterAnnotation.count)"
                return annotationView
            }
            
            // Handle scooter annotations
            if let scooterAnnotation = annotation as? ScooterAnnotation {
                let identifier = "scooter"
                var annotationView = mapView.dequeueReusableAnnotationView(withIdentifier: identifier)
                
                if annotationView == nil {
                    annotationView = MKAnnotationView(annotation: scooterAnnotation, reuseIdentifier: identifier)
                    annotationView?.canShowCallout = true
                }
                
                annotationView?.annotation = scooterAnnotation
                
                // Set a default icon immediately so annotation is visible
                annotationView?.image = UIImage(systemName: "mappin.circle.fill")?.withTintColor(.red, renderingMode: .alwaysOriginal)
                
                // Load custom icon for the annotation
                loadAnnotationIcon(for: scooterAnnotation.title ?? "", url: scooterAnnotation.iconUrl) { [weak annotationView] image in
                    // Only update if we got a valid image and the annotation view still exists
                    if let image = image, let annotationView = annotationView {
                        DispatchQueue.main.async {
                            annotationView.image = image
                        }
                    }
                }
                
                return annotationView
            }
            
            return nil
        }
        
        func mapView(_ mapView: MKMapView, regionDidChangeAnimated animated: Bool) {
            // Update annotations when map region changes
            updateAnnotations()
        }
        
        // MARK: - Icon loading utilities
        
        private var iconCache = [String: UIImage]()
        private let markerIconSizePx: CGFloat = 48.0
        
        private func loadAnnotationIcon(for providerName: String, url: String?, completion: @escaping (UIImage?) -> Void) {
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
    }
}

// MARK: - Custom Annotation Classes

class ScooterAnnotation: NSObject, MKAnnotation {
    let coordinate: CLLocationCoordinate2D
    let title: String?
    let subtitle: String?
    let iconUrl: String?
    
    init(coordinate: CLLocationCoordinate2D, title: String?, subtitle: String?, iconUrl: String?) {
        self.coordinate = coordinate
        self.title = title
        self.subtitle = subtitle
        self.iconUrl = iconUrl
        super.init()
    }
}

class ClusterAnnotation: NSObject, MKAnnotation {
    let coordinate: CLLocationCoordinate2D
    let memberAnnotations: [ScooterAnnotation]
    let count: Int
    
    var title: String? {
        return "\(count) scooters"
    }
    
    var subtitle: String? {
        return nil
    }
    
    init(coordinate: CLLocationCoordinate2D, memberAnnotations: [ScooterAnnotation], count: Int) {
        self.coordinate = coordinate
        self.memberAnnotations = memberAnnotations
        self.count = count
        super.init()
    }
}
