package com.filipmihajlov.camera.cutout.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import com.filipmihajlov.camera.cutout.enableCameraCutoutLayout
import com.filipmihajlov.camera.cutout.ui.CameraCutoutEmissionConfig
import com.filipmihajlov.camera.cutout.ui.CameraCutoutEmissionRepeatMode
import com.filipmihajlov.camera.cutout.ui.debug.CameraCutoutDebugRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.enableCameraCutoutLayout()
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                CameraCutoutDebugRoute(
                    emissionConfig = CameraCutoutEmissionConfig(
                        enabled = true,
                        travelPx = 100f,
                        durationMillis = 900,
                        repeatMode = CameraCutoutEmissionRepeatMode.Infinite,
                    ),
                )
            }
        }
    }
}
