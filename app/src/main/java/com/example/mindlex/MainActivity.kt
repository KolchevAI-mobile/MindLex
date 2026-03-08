package com.example.mindlex.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.mindlex.feature.root.MindLexAppNavHost
import com.example.mindlex.ui.theme.MindLexTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MindLexTheme {
                val navController = rememberNavController()
                MindLexAppNavHost(navController = navController)
            }
        }
    }
}