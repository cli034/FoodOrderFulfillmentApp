package com.example.csschallenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.csschallenge.ui.theme.CSSChallengeTheme
import com.example.csschallenge.ui.orderEvents.OrderEventsViewModel
import com.example.csschallenge.ui.orderEvents.navigation.OrderEventsNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val orderEventsViewModel: OrderEventsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CSSChallengeTheme {
                OrderEventsNavHost(
                    viewModel = orderEventsViewModel,
                )
            }
        }
    }
}