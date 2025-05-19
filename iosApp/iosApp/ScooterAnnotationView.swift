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
        
        // Enable user interaction
        self.isUserInteractionEnabled = true
        
        // Set a reasonable frame size
        self.frame = CGRect(x: 0, y: 0, width: 40, height: 40)
        
        // Initially hide the border
        self.layer.borderWidth = 2.0
        self.layer.borderColor = UIColor.green.cgColor
        self.layer.cornerRadius = self.bounds.width / 2
        self.layer.masksToBounds = true
        
        // Make the image larger
        self.image = self.image?.withRenderingMode(.alwaysOriginal)
        self.transform = CGAffineTransform(scaleX: 1.5, y: 1.5)
    }

    override var isSelected: Bool {
        didSet {
            // Update border when selection state changes
            self.layer.borderWidth = isSelected ? 3.0 : 0.0
            self.layer.borderColor = UIColor.green.cgColor
        }
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
}
