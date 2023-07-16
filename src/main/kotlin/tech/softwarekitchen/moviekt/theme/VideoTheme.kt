package tech.softwarekitchen.moviekt.theme

import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.Color

interface ThemedClip{
    fun getEmptyThemeProperties(): List<String>
    fun set(key: String, value: Any)
    fun getChildren(): List<VideoClip>

    fun applyTheme(theme: VideoTheme){
        val keys = getEmptyThemeProperties()
        keys.forEach{
            key ->
            val data = theme.get(key) ?: throw Exception()
            set(key, data)
        }
        getChildren().forEach{it.applyTheme(theme)}
    }
}

class VideoTheme {
    companion object{
        val VTPropertyKey_FontColor = "font-color"
    }
    private val themeData = HashMap<String, Any>()

    fun withFontColor(color: Color): VideoTheme{
        themeData[VTPropertyKey_FontColor] = color
        return this
    }

    fun get(key: String): Any?{
        return themeData[key]
    }
}
