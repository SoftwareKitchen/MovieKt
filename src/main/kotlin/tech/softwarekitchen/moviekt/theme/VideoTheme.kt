package tech.softwarekitchen.moviekt.theme

import org.slf4j.Logger
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.Color

interface ThemedClip{
    val logger: Logger
    fun getPossibleThemeProperties(): List<String>
    fun set(key: String, value: Any)
    fun getChildren(): List<VideoClip>

    fun applyTheme(theme: VideoTheme){
        logger.trace("Applying theme to node")
        val keys = getPossibleThemeProperties()
        logger.trace("Themable properties: ${keys.size}")
        keys.forEach{
            key ->
            theme.get(key)?.let{
                set(key, it)
            }
        }
        getChildren().forEach{it.applyTheme(theme)}
    }
}

class VideoTheme {
    companion object{
        val VTPropertyKey_FontColor = "font-color"
        val VTPropertyKey_FontSize = "font-size"
    }
    private val themeData = HashMap<String, Any>()

    fun set(key: String, value: Any){
        themeData[key] = value
    }

    fun get(key: String): Any?{
        return themeData[key]
    }
}
