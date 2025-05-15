//
//  ScooterAnnotationView.swift
//  iosApp
//
//  Created on 14/05/2025.
//

import UIKit
import MapKit

class ScooterAnnotationView: MKAnnotationView {
    weak var coordinator: AppleMapView.Coordinator?
    
    init(annotation: MKAnnotation?, reuseIdentifier: String?, coordinator: AppleMapView.Coordinator) {
        self.coordinator = coordinator
        super.init(annotation: annotation, reuseIdentifier: reuseIdentifier)
        
        // Enable user interaction - this is critical
        self.isUserInteractionEnabled = true
        
        // Set a large frame to increase the tap target
        let hitboxSize: CGFloat = 80.0 // Even larger touch target
        self.frame = CGRect(
            x: -hitboxSize/2,
            y: -hitboxSize/2,
            width: hitboxSize,
            height: hitboxSize
        )
        
        // Add a button that covers the entire view for easier tapping
        let button = UIButton(type: .custom)
        button.backgroundColor = UIColor.clear
        button.addTarget(self, action: #selector(handleTap), for: .touchUpInside)
        self.addSubview(button)
        
        // Debug visual indicator
        self.layer.borderWidth = 2.0
        self.layer.borderColor = UIColor.green.cgColor
        
        print("ScooterAnnotationView: Initialized with userInteractionEnabled: \(self.isUserInteractionEnabled)")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        // Ensure subviews (like our button) cover the entire view
        for subview in subviews {
            if let button = subview as? UIButton {
                // Make button cover the entire view plus padding
                let padding: CGFloat = 20.0
                button.frame = CGRect(
                    x: -padding,
                    y: -padding,
                    width: bounds.width + (padding * 2),
                    height: bounds.height + (padding * 2)
                )
            }
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    // Override hitTest to make it easier to tap on the annotation
    override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        print("ScooterAnnotationView: hitTest called with point: \(point)")
        
        // Make the hit area larger
        let largerBounds = CGRect(
            x: -30, // Increased padding
            y: -30,
            width: bounds.width + 60,
            height: bounds.height + 60
        )
        
        if largerBounds.contains(point) {
            print("ScooterAnnotationView: Hit detected in larger bounds")
            // Return the button if it exists, otherwise return self
//            return subviews.first { $0 is UIButton } ?? self
            // Hitting the larger bounds is great!!! That's what we want! Send the click to the button
            for subview in subviews {
                if let button = subview as? UIButton {
                    print("ScooterAnnotationView: Returning button for hit test")
//                    return button
// Don't just return the button, execute the button's action
                    button.sendActions(for: .touchUpInside)
                    return nil // Return nil to indicate that the event has been handled
                }
            }
        }
        
        return super.hitTest(point, with: event)
    }
    
    // Override point(inside:with:) to make the touch area larger
    override func point(inside point: CGPoint, with event: UIEvent?) -> Bool {
        // Make the hit area much larger
        let largerBounds = CGRect(
            x: -30, // Increased padding
            y: -30,
            width: bounds.width + 60,
            height: bounds.height + 60
        )
        
        let isInside = largerBounds.contains(point)
        print("ScooterAnnotationView: Point is \(isInside ? "inside" : "outside") larger bounds")
        return isInside
    }
    
    @objc func handleTap() {
        print("ScooterAnnotationView: Tap detected")
        
        guard let annotation = self.annotation as? ScooterAnnotation,
              let mapView = coordinator?.mapView else {
            print("ScooterAnnotationView: Missing annotation or mapView")
            return
        }
        
        print("ScooterAnnotationView: Selecting annotation: \(annotation.title ?? "nil")")
        
        // First select the annotation visually
        mapView.selectAnnotation(annotation, animated: true)
        
        // Find the corresponding scooter
        guard let coordinator = coordinator else {
            print("ScooterAnnotationView: Missing coordinator")
            return
        }
        
        let mapData = coordinator.parent.mapData
        let matchingScooter = mapData.scooters.first { scooter in
            let scooterPosition = CLLocationCoordinate2D(latitude: scooter.latitude, longitude: scooter.longitude)
            let positionMatches = abs(annotation.coordinate.latitude - scooterPosition.latitude) < 0.0001 && 
                                  abs(annotation.coordinate.longitude - scooterPosition.longitude) < 0.0001
            let nameMatches = annotation.title == scooter.providerName
            
            return positionMatches && nameMatches
        }
        
        // Call the callback with the selected scooter
        if let scooter = matchingScooter {
            print("ScooterAnnotationView: Scooter found: \(scooter.providerName) (ID: \(scooter.id))")
            
            // Make sure the callback exists
            if mapData.onScooterSelected != nil {
                print("ScooterAnnotationView: Calling onScooterSelected callback")
                DispatchQueue.main.async {
                    mapData.onScooterSelected?(scooter)
                }
            } else {
                print("ScooterAnnotationView: onScooterSelected callback is nil")
            }
        } else {
            print("ScooterAnnotationView: No matching scooter found")
        }
    }
}
