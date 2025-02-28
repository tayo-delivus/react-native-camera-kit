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
    private val bottom: Int
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
        }

    companion object {
        const val EVENT_NAME = "topReadCode"
    }
}
