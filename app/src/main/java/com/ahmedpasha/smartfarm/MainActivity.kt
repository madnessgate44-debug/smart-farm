package com.ahmedpasha.smartfarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahmedpasha.smartfarm.ui.navigation.FarmNavigation
import com.ahmedpasha.smartfarm.ui.theme.SmartFarmTheme
import com.ahmedpasha.smartfarm.ui.viewmodel.FarmViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartFarmTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val viewModel: FarmViewModel = viewModel()
                    FarmNavigation(viewModel = viewModel)
                }
            }
        }
    }
}