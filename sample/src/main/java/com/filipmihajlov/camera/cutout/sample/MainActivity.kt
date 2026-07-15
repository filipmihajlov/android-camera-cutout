package com.filipmihajlov.camera.cutout.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.filipmihajlov.camera.cutout.enableCameraCutoutLayout
import com.filipmihajlov.camera.cutout.ui.CameraCutoutContentEmissionOverlay
import com.filipmihajlov.camera.cutout.ui.CameraCutoutEmissionConfig
import com.filipmihajlov.camera.cutout.ui.CameraCutoutEmissionMotion
import com.filipmihajlov.camera.cutout.ui.CameraCutoutEmissionRepeatMode
import com.filipmihajlov.camera.cutout.ui.debug.CameraCutoutDebugRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.enableCameraCutoutLayout()
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val emissionConfig = CameraCutoutEmissionConfig(
                    enabled = true,
                    travelPx = 300f,
                    durationMillis = 3000,
                    repeatMode = CameraCutoutEmissionRepeatMode.Infinite,
                    motion = CameraCutoutEmissionMotion.InstaxPrint(),
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    CameraCutoutDebugRoute(
                        emissionConfig = CameraCutoutEmissionConfig(),
                    )

                    CameraCutoutContentEmissionOverlay(
                        modifier = Modifier.fillMaxSize(),
                        emissionConfig = emissionConfig,
                    ) {
                        Card(
                            modifier = Modifier.size(
                                width = 96.dp,
                                height = 124.dp,
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Text(
                                text = "PRINT",
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
