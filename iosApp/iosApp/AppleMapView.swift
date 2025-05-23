import SwiftUI
import MapKit
import CoreLocation
import UIKit
import ComposeApp

struct AppleMapView: UIViewRepresentable {
    @ObservedObject var mapData: ScooterMapData

    func makeCoordinator() -> Coordinator { Coordinator(self, mapData: mapData) }

    func makeUIView(context: Context) -> MKMapView {
        let mapView = MKMapView()
        mapView.delegate = context.coordinator
        mapView.showsUserLocation = true
        mapView.isRotateEnabled = true
        mapView.isPitchEnabled = true
        mapView.isZoomEnabled = true
        mapView.isScrollEnabled = true
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
        var mapData: ScooterMapData
        var mapView: MKMapView?
        var locationManager = CLLocationManager()
        var hasCenteredOnce = false
        var lastScooterCount = 0
        var userLocation: CLLocation?
        
        init(_ parent: AppleMapView, mapData: ScooterMapData) {
            self.parent = parent
            self.mapData = mapData
            super.init()
            print("AppleMapView.Coordinator: Initialized with \(mapData.scooters.count) scooters")
        }

        // Function to find the nearest scooter to user's location
        private func findNearestScooter(to userLocation: CLLocation) -> Scooter? {
            if mapData.scooters.isEmpty { return nil }
            
            return mapData.scooters.min(by: { scooter1, scooter2 in
                let location1 = CLLocation(latitude: scooter1.latitude, longitude: scooter1.longitude)
                let location2 = CLLocation(latitude: scooter2.latitude, longitude: scooter2.longitude)
                return userLocation.distance(from: location1) < userLocation.distance(from: location2)
            })
        }

        func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
            guard let loc = locations.last else { return }
            userLocation = loc
        }

        func updateAnnotations() {
            guard let mapView = mapView else { 
                print("AppleMapView: mapView is nil in updateAnnotations")
                return 
            }
            
            print("AppleMapView: updateAnnotations called with \(parent.mapData.scooters.count) scooters")
            
            // If this is the first time we're getting scooters and we have user location, focus on nearest scooter
            if !hasCenteredOnce && !mapData.scooters.isEmpty, let userLoc = userLocation {
                hasCenteredOnce = true
                
                // Find nearest scooter if available
                if let nearestScooter = findNearestScooter(to: userLoc) {
                    print("Nearest scooter: \(nearestScooter)")
                    // Center the map on the nearest scooter with a closer zoom level
                    let region = MKCoordinateRegion(
                        center: CLLocationCoordinate2D(
                            latitude: nearestScooter.latitude,
                            longitude: nearestScooter.longitude
                        ),
                        span: MKCoordinateSpan(latitudeDelta: 0.0005, longitudeDelta: 0.0005) // Closer zoom level
                    )
                    mapView.setRegion(region, animated: true)
                    
                    // Find and select the annotation for the nearest scooter
                    print("Called onScooterSelected from nearest scooter")
                    selectScooter(scooter: nearestScooter)
                } else {
                    // Center on user location if no scooters available with a wider view
                    let region = MKCoordinateRegion(
                        center: userLoc.coordinate,
                        span: MKCoordinateSpan(latitudeDelta: 0.01, longitudeDelta: 0.01)
                    )
                    mapView.setRegion(region, animated: true)
                }
            }
            
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
            
            print("AppleMapView: Rendering \(visibleScooters.count) of \(parent.mapData.scooters.count) scooters")
            
            // Debug: Print the first few visible scooters
            if !visibleScooters.isEmpty {
                for i in 0..<min(3, visibleScooters.count) {
                    let s = visibleScooters[i]
                    print("AppleMapView: Visible scooter \(i): id=\(s.id), provider=\(s.providerName), lat=\(s.latitude), lng=\(s.longitude)")
                }
            } else {
                print("AppleMapView: No visible scooters to render")
                
                // Print all scooters for debugging
                if !parent.mapData.scooters.isEmpty {
                    print("AppleMapView: All scooters:")
                    for i in 0..<min(3, parent.mapData.scooters.count) {
                        let s = parent.mapData.scooters[i]
                        print("AppleMapView: Scooter \(i): id=\(s.id), provider=\(s.providerName), lat=\(s.latitude), lng=\(s.longitude)")
                    }
                } else {
                    print("AppleMapView: No scooters available in mapData")
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
                    iconUrl: s.providerIcon,
                    isSelected: false
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
        
        // MARK: - UIGestureRecognizerDelegate
        
        func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool {
            // Only allow simultaneous recognition for specific gesture combinations
            if gestureRecognizer is UIPanGestureRecognizer && otherGestureRecognizer is UIPinchGestureRecognizer {
                return true
            }
            if gestureRecognizer is UIPinchGestureRecognizer && otherGestureRecognizer is UIPanGestureRecognizer {
                return true
            }
            return false
        }
        
        func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldReceive touch: UITouch) -> Bool {
            // Only handle touches on the map view itself, not on annotations or other subviews
            return touch.view == mapView
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
                var annotationView = mapView.dequeueReusableAnnotationView(withIdentifier: identifier) as? ScooterAnnotationView
                
                if annotationView == nil {
                    // Create our custom annotation view
                    annotationView = ScooterAnnotationView(annotation: scooterAnnotation, reuseIdentifier: identifier, coordinator: self)
                    annotationView?.canShowCallout = false
                    
                    // Make sure user interaction is enabled
                    annotationView?.isUserInteractionEnabled = true
                    
//                    print("AppleMapView: Created NEW ScooterAnnotationView with userInteractionEnabled: \(annotationView?.isUserInteractionEnabled ?? false)")
                } else {
                    // Update the annotation
                    annotationView?.annotation = scooterAnnotation
//                    print("AppleMapView: Reused ScooterAnnotationView with userInteractionEnabled: \(annotationView?.isUserInteractionEnabled ?? false)")
                }
                
                // Set a default icon immediately so annotation is visible
                let defaultImage = UIImage(systemName: "mappin.circle.fill")?.withTintColor(.red, renderingMode: .alwaysOriginal)
                annotationView?.image = defaultImage
                
                // Make sure the image is centered
                annotationView?.centerOffset = CGPoint.zero
                
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
        
        // Handle tap gesture on annotation view
        @objc func handleAnnotationTap(_ gesture: UITapGestureRecognizer) {
            print("AppleMapView: Direct tap on annotation detected")
            
            guard let annotationView = gesture.view as? MKAnnotationView,
                  let annotation = annotationView.annotation else {
                print("AppleMapView: Could not get annotation from tap gesture")
                return
            }
            
            // Manually trigger the selection
            mapView?.selectAnnotation(annotation, animated: true)
            
            // Handle the tap directly
            if let scooterAnnotation = annotation as? ScooterAnnotation {
                print("AppleMapView: Direct tap on ScooterAnnotation - \(scooterAnnotation.title ?? "nil")")
                
                // Find the corresponding scooter
                let matchingScooter = parent.mapData.scooters.first { scooter in
                    let scooterPosition = CLLocationCoordinate2D(latitude: scooter.latitude, longitude: scooter.longitude)
                    let positionMatches = abs(scooterAnnotation.coordinate.latitude - scooterPosition.latitude) < 0.0001 && 
                                          abs(scooterAnnotation.coordinate.longitude - scooterPosition.longitude) < 0.0001
                    let nameMatches = scooterAnnotation.title == scooter.providerName
                    
                    return positionMatches && nameMatches
                }
                
                // Call the callback with the selected scooter
                if let scooter = matchingScooter {
                    print("AppleMapView: Direct tap - Scooter selected: \(scooter.providerName) (ID: \(scooter.id))")
                    
                    // Make sure the callback exists
                    if parent.mapData.onScooterSelected != nil {
                        print("AppleMapView: Direct tap - Calling onScooterSelected callback")
                        selectScooter(scooter: scooter)
                    } else {
                        print("AppleMapView: Direct tap - onScooterSelected callback is nil")
                    }
                } else {
                    print("AppleMapView: Direct tap - No matching scooter found")
                }
            }
        }
        
        func mapView(_ mapView: MKMapView, didSelect view: MKAnnotationView) {
            print("AppleMapView: Annotation selected via mapView delegate - view type: \(type(of: view))")
            
            // Handle annotation selection
            if let scooterAnnotation = view.annotation as? ScooterAnnotation {
                print("AppleMapView: ScooterAnnotation found - \(scooterAnnotation.title ?? "nil") at \(scooterAnnotation.coordinate.latitude), \(scooterAnnotation.coordinate.longitude)")
                
                // Find the corresponding scooter
                let matchingScooter = parent.mapData.scooters.first { scooter in
                    let scooterPosition = CLLocationCoordinate2D(latitude: scooter.latitude, longitude: scooter.longitude)
                    let positionMatches = abs(scooterAnnotation.coordinate.latitude - scooterPosition.latitude) < 0.0001 && 
                                          abs(scooterAnnotation.coordinate.longitude - scooterPosition.longitude) < 0.0001
                    let nameMatches = scooterAnnotation.title == scooter.providerName
                    
                    if positionMatches && nameMatches {
                        return true
                    }
                    return false
                }
                
                // Call the callback with the selected scooter
                if let scooter = matchingScooter {
                    print("AppleMapView: Scooter selected: \(scooter.providerName) (ID: \(scooter.id))")
                    
                    // Make sure the callback exists
                    if parent.mapData.onScooterSelected != nil {
                        print("AppleMapView: Calling onScooterSelected callback")
                        selectScooter(scooter: scooter)
                    } else {
                        print("AppleMapView: onScooterSelected callback is nil")
                    }
                } else {
                    print("AppleMapView: No matching scooter found")
                }
            } else if let clusterAnnotation = view.annotation as? ClusterAnnotation {
                print("AppleMapView: ClusterAnnotation selected with \(clusterAnnotation.count) scooters")
            } else {
                print("AppleMapView: Unknown annotation type selected")
            }
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
        
        
        func selectScooter(scooter: Scooter) {
            if let annotation = mapView?.annotations.first(where: { annotation in
                guard let scooterAnnotation = annotation as? ScooterAnnotation else { return false }
                return abs(scooterAnnotation.coordinate.latitude - scooter.latitude) < 0.0001 &&
                       abs(scooterAnnotation.coordinate.longitude - scooter.longitude) < 0.0001 &&
                       scooterAnnotation.title == scooter.providerName
            }) {
                mapView?.selectAnnotation(annotation, animated: true)
            }
            
            parent.mapData.onScooterSelected?(scooter)
            
        }
    }
}

// MARK: - Custom Annotation Classes

class ScooterAnnotation: NSObject, MKAnnotation {
    let coordinate: CLLocationCoordinate2D
    let title: String?
    let subtitle: String?
    let iconUrl: String?
    let isSelected: Bool?
    
    init(coordinate: CLLocationCoordinate2D, title: String?, subtitle: String?, iconUrl: String?, isSelected: Bool?) {
        self.coordinate = coordinate
        self.title = title
        self.subtitle = subtitle
        self.iconUrl = iconUrl
        self.isSelected = isSelected
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
