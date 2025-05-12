//
//  ScooterClusterRenderer.swift
//  iosApp
//
//  Created by עדי סטרויאנו on 12/05/2025.
//  Copyright © 2025 orgName. All rights reserved.
//


import Foundation
import GoogleMaps
import GoogleMapsUtils
import UIKit

/**
 * This class is kept for reference but is not currently used.
 * The marker rendering functionality has been moved directly into
 * the GoogleMapView.Coordinator class using the GMUClusterRendererDelegate protocol.
 */
class ScooterClusterRenderer: GMUDefaultClusterRenderer {
    
    // Default marker icon to use if no custom icon is available
    private let defaultMarkerIcon = UIImage(systemName: "mappin.circle.fill")?.withTintColor(.red, renderingMode: .alwaysOriginal)

    
    override func renderClusters(_ clusters: [any GMUCluster]) {
        super.renderClusters(clusters)
    }
}
