package com.cresup.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.cresup.app.presentation.ui.navigation.NavGraph
import com.cresup.app.presentation.ui.theme.CresUpTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CresUpTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CresUpTheme.colors.background
                ) {
                    NavGraph()
                }
            }
        }
    }
}
