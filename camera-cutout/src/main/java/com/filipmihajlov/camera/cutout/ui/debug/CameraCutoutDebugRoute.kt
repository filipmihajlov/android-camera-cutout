package com.filipmihajlov.camera.cutout.ui.debug

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.filipmihajlov.camera.cutout.presentation.CameraCutoutViewModel
import com.filipmihajlov.camera.cutout.ui.CameraCutoutEmissionConfig
import com.filipmihajlov.camera.cutout.ui.CameraCutoutInsetsEffect
import com.filipmihajlov.camera.cutout.ui.CameraCutoutOverlay

@Composable
fun CameraCutoutDebugRoute(
    modifier: Modifier = Modifier,
    emissionConfig: CameraCutoutEmissionConfig = CameraCutoutEmissionConfig(),
    viewModel: CameraCutoutViewModel = viewModel(),
) {
    CameraCutoutInsetsEffect(
        onInsetsChanged = viewModel::onWindowInsetsChanged,
    )

    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            CameraCutoutDebugContent(
                uiState = uiState,
                modifier = Modifier.padding(innerPadding),
            )
        }

        val cutoutInfo = uiState.cutoutInfo
        if (cutoutInfo?.hasCutout == true) {
            CameraCutoutOverlay(
                cutoutInfo = cutoutInfo,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxSize(),
                emissionConfig = emissionConfig,
            )
        }
    }
}
