package cl.duoc.levelupgamer

import ui.home.HomeScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duoc.levelupgamer.ui.theme.LevelupgamerTheme
import ui.app.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LevelupgamerTheme {
                AppNavHost()
            }
        }
    }
}
