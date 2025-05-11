//
//  BuildSettings.swift
//  iosApp
//
//  Created by עדי סטרויאנו on 10/05/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import Foundation

enum BuildSettings {
    static var googleMapsApiKey: String {
        guard let key = Bundle.main.infoDictionary?["GOOGLE_MAPS_API_KEY"] as? String else {
            fatalError("Missing GOOGLE_MAPS_API_KEY in Info.plist")
        }
        return key
    }
}
