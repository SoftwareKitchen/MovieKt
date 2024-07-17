package tech.softwarekitchen.moviekt.clips.video.text

import tech.softwarekitchen.common.vector.Vector2i
import kotlin.math.floor

class TimeFormattedTextVideoClip(
    id: String, size: Vector2i, position: Vector2i,
    visible: Boolean, configuration: StaticTextVideoClipConfiguration
): TextVideoClip(id, size, position, visible, configuration) {
    companion object{
        val PropertyKey_Time = "Time"
    }

    private val timeProperty = VideoClipProperty(PropertyKey_Time, 0f, this::translateTime)

    init{
        registerProperty(timeProperty)
        translateTime(null)
    }

    private fun translateTime(ignored: Any?){
        val v = timeProperty.v
        val hours = floor(v / 3600).toInt()
        val rest = v - hours * 3600
        val minutes = floor(rest / 60).toInt()
        val rest2 = rest - minutes * 60
        val seconds = floor(rest2).toInt()
        val textValue = "${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        set(PropertyKey_Text, textValue)
    }
}
