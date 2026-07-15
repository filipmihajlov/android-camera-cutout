package com.filipmihajlov.camera.cutout.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.filipmihajlov.camera.cutout.presentation.CameraCutoutViewModel

@Composable
fun CameraCutoutContentEmissionOverlay(
    modifier: Modifier = Modifier,
    emissionConfig: CameraCutoutEmissionConfig = CameraCutoutEmissionConfig(),
    contentAlignment: CameraCutoutEmissionAlignment = CameraCutoutEmissionAlignment.Center,
    viewModel: CameraCutoutViewModel = viewModel(),
    content: @Composable () -> Unit,
) {
    CameraCutoutInsetsEffect(
        onInsetsChanged = viewModel::onWindowInsetsChanged,
    )

    val uiState by viewModel.uiState.collectAsState()
    val cutoutInfo = uiState.cutoutInfo

    if (cutoutInfo?.hasCutout == true) {
        CameraCutoutContentEmitter(
            cutoutInfo = cutoutInfo,
            modifier = modifier,
            emissionConfig = emissionConfig,
            contentAlignment = contentAlignment,
            content = content,
        )
    }
}
