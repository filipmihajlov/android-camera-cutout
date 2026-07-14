package com.filipmihajlov.camera.cutout.domain

import android.view.WindowInsets

interface CameraCutoutReader {
    fun read(windowInsets: WindowInsets?): CameraCutoutInfo?
}
