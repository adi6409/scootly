////
////  MainViewControllerExtension.swift
////  iosApp
////
////  Created by עדי סטרויאנו on 11/05/2025.
////  Copyright © 2025 orgName. All rights reserved.
////
//import UIKit
//import SwiftUI
//import ComposeApp  // your Kotlin/Native framework
//
//extension MainViewControllerKt {
//    /// Overrides the Compose-for-iOS stub so that we fetch from
//    /// Koin-injected repos in Kotlin and feed data into our SwiftUI map
//    @objc
//    public static func MainViewController() -> UIViewController {
//        // 1️⃣ Create an ObservableObject for the map
//        let mapData = ScooterMapData()
//
//        // 2️⃣ In the background, load from your shared repos
//        DispatchQueue.global().async {
//            let providers = IosInjector().providerRepo.getProviders()
//            let scooters  = IosInjector().scooterRepo.getScooters(null)
//
//            // 3️⃣ Publish back to SwiftUI on main thread
//            DispatchQueue.main.async {
//                mapData.providers = providers as? [Provider] ?? []
//                mapData.scooters  = scooters  as? [Scooter]  ?? []
//            }
//        }
//
//        // 4️⃣ Host the SwiftUI map container
//        let root = GoogleMapContainerView(mapData: mapData)
//        return UIHostingController(rootView: root)
//    }
//}
