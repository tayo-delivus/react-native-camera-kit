package com.rncamerakit.events

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

class ReadCodeEvent(
    surfaceId: Int,
    viewId: Int,
    private val codeStringValue: String?,
    private val codeFormat: String,
    private val left: Int,
    private val top: Int,
    private val right: Int,
    private val bottom: Int,
    private val viewLeft: Int,
    private val viewTop: Int,
    private val viewRight: Int,
    private val viewBottom: Int
) : Event<ReadCodeEvent>(surfaceId, viewId) {
    override fun getEventName(): String = EVENT_NAME

    override fun getEventData(): WritableMap =
        Arguments.createMap().apply {
            putString("codeFormat", codeFormat)
            putString("codeStringValue", codeStringValue)
            val coord: WritableMap = Arguments.createMap().apply {
                putInt("left", left)
                putInt("top", top)
                putInt("right", right)
                putInt("bottom", bottom)
            }
            putMap("coord", coord)
            val view: WritableMap = Arguments.createMap().apply {
                putInt("left", viewLeft)
                putInt("top", viewTop)
                putInt("right", viewRight)
                putInt("bottom", viewBottom)
            }
            putMap("view", view)
        }

    companion object {
        const val EVENT_NAME = "topReadCode"
    }
}
