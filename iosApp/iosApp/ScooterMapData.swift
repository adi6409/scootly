//
//  ScooterMapData.swift
//  iosApp
//
//  Created by עדי סטרויאנו on 10/05/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import Foundation
import Combine
import ComposeApp

class ScooterMapData: ObservableObject {
    @Published var providers: [Provider] = [] {
        didSet {
            print("ScooterMapData: providers updated, count: \(providers.count)")
        }
    }
    
    @Published var scooters: [Scooter] = [] {
        didSet {
            print("ScooterMapData: scooters updated, count: \(scooters.count)")
            if let firstScooter = scooters.first {
                print("ScooterMapData: First scooter - id: \(firstScooter.id), provider: \(firstScooter.providerName), lat: \(firstScooter.latitude), lng: \(firstScooter.longitude)")
            }
        }
    }
    
    // Callback for when a scooter is selected
    var onScooterSelected: ((Scooter?) -> Void)?
}
