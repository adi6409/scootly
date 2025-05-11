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
    @Published var providers: [Provider] = []
    @Published var scooters: [Scooter] = []
}
