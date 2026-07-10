package dev.stefano.enuventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.stefano.enuventory.ui.components.EnuBottomBar
import dev.stefano.enuventory.ui.theme.EnuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isAdminState = true
            var currentRoute by remember { mutableStateOf("home") }

            EnuTheme {
                Scaffold(
                    bottomBar = {
                        EnuBottomBar(
                            isAdmin = isAdminState,
                            currentRoute = currentRoute,
                            onItemClick = { selectedItem ->
                                currentRoute = selectedItem.route
                            }
                        )
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        when (currentRoute) {
                            "home" -> Text("Ini home")
                            "history" -> Text("Ini history")
                            "approval" -> Text("Ini approval")
                            "settings" -> Text("Ini settings")
                        }
                    }
                }
            }
        }
    }
}
