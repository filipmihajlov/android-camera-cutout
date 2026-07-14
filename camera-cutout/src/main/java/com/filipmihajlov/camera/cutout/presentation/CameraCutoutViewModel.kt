package com.filipmihajlov.camera.cutout.presentation

import android.view.WindowInsets
import androidx.lifecycle.ViewModel
import com.filipmihajlov.camera.cutout.data.AndroidCameraCutoutReader
import com.filipmihajlov.camera.cutout.domain.CameraCutoutReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CameraCutoutViewModel : ViewModel {
    private val cutoutReader: CameraCutoutReader

    constructor() : this(AndroidCameraCutoutReader())

    internal constructor(cutoutReader: CameraCutoutReader) : super() {
        this.cutoutReader = cutoutReader
    }

    private val _uiState = MutableStateFlow(CameraCutoutUiState())
    val uiState: StateFlow<CameraCutoutUiState> = _uiState.asStateFlow()

    fun onWindowInsetsChanged(windowInsets: WindowInsets?) {
        _uiState.value = CameraCutoutUiState(
            cutoutInfo = cutoutReader.read(windowInsets),
        )
    }
}
