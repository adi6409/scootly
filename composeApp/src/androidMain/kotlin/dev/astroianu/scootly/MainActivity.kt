package dev.astroianu.scootly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.astroianu.scootly.screens.list.ScooterListScreen
import dev.astroianu.scootly.screens.list.ScooterListViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ScooterListScreen(
                scooterListViewModel = ScooterListViewModel()
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
//    App()
    ScooterListScreen(
        scooterListViewModel = ScooterListViewModel()
    )
}