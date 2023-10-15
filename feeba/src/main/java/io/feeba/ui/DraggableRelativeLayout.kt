package io.feeba.ui

import android.content.Context
import android.content.res.Resources
import android.widget.RelativeLayout
import io.least.ui.dpToPx

internal class DraggableRelativeLayout(context: Context, private val params: Params) : RelativeLayout(context) {

    private var dismissing = false

    companion object {
        const val DRAGGABLE_DIRECTION_UP = 0
        const val DRAGGABLE_DIRECTION_DOWN = 1
    }
    internal data class Params (
        var posY: Int = 0,
        var maxYPos : Int = 0,
        var dragThresholdY : Int = 0, // Y value associated with trigger for onDragStart() callback
        var maxXPos : Int = 0,
        var height : Int = 0,
        var messageHeight : Int = 0,
        var dragDirection : Int = 0,
        var draggingDisabled : Boolean = false,
        var dismissingYVelocity : Int = 0,
        var offScreenYPos : Int = 0,
        var dismissingYPos : Int = 0,
    )

    private val MARGIN_PX_SIZE = dpToPx(28f)
    private val EXTRA_PX_DISMISS = dpToPx(64f)

    init {
        clipChildren = false

        params.offScreenYPos = params.messageHeight + params.posY + (Resources.getSystem().displayMetrics.heightPixels - params.messageHeight - params.posY) + EXTRA_PX_DISMISS
        params.dismissingYVelocity = dpToPx(3000f)
        if (params.dragDirection == DRAGGABLE_DIRECTION_UP) {
            params.offScreenYPos = -params.messageHeight - MARGIN_PX_SIZE
            params.dismissingYVelocity = -params.dismissingYVelocity
            params.dismissingYPos = params.offScreenYPos / 3
        } else params.dismissingYPos = params.messageHeight / 3 + params.maxYPos * 2

    }
}