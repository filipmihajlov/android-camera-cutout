package com.filipmihajlov.camera.cutout.presentation

import com.filipmihajlov.camera.cutout.domain.CameraCutoutInfo

data class CameraCutoutUiState(
    val cutoutInfo: CameraCutoutInfo? = null,
) {
    val isWaitingForInsets: Boolean = cutoutInfo == null
}
