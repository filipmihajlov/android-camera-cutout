package com.filipmihajlov.camera.cutout.ui

import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat

@Composable
fun CameraCutoutInsetsEffect(
    onInsetsChanged: (WindowInsets?) -> Unit,
) {
    val view = LocalView.current

    DisposableEffect(view, onInsetsChanged) {
        fun updateCutoutInfo() {
            onInsetsChanged(view.rootWindowInsets)
        }

        val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            updateCutoutInfo()
        }

        val attachStateListener = object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(attachedView: View) {
                attachedView.post { updateCutoutInfo() }
                ViewCompat.requestApplyInsets(attachedView)
            }

            override fun onViewDetachedFromWindow(detachedView: View) = Unit
        }

        view.addOnAttachStateChangeListener(attachStateListener)
        view.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        view.post { updateCutoutInfo() }
        ViewCompat.requestApplyInsets(view)

        onDispose {
            view.removeOnAttachStateChangeListener(attachStateListener)
            if (view.viewTreeObserver.isAlive) {
                view.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
            }
        }
    }
}
