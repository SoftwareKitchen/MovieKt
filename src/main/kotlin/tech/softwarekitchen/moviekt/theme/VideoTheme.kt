package tech.softwarekitchen.moviekt.theme

import org.slf4j.Logger
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.Color

interface ThemedClip{
    val logger: Logger
    fun getPossibleThemeProperties(): List<String>
    fun set(key: String, value: Any)
    fun getChildren(): List<VideoClip>
    fun getVariant(): String?
    fun applyTheme(theme: VideoTheme){
        logger.trace("Applying theme to node")
        val keys = getPossibleThemeProperties()
        logger.trace("Themable properties: ${keys.size}")
        val variant = getVariant()
        keys.forEach{
            key ->
            if(variant == null){
                theme.get(key)?.let{
                    set(key, it)
                }
            }else{
                val vv = theme.get(key, variant) ?: theme.get(key)
                vv?.let{ set(key, it)}
            }

        }
        getChildren().forEach{it.applyTheme(theme)}
    }
}

data class ThemeData(val key: String, val variant: String?, val value: Any)

class VideoTheme {
    companion object{
        val VTPropertyKey_Variant = "theme-variant"
        val VTPropertyKey_FontColor = "font-color"
        val VTPropertyKey_FontSize = "font-size"
        val VTPropertyKey_BackgroundColor = "background-color"
        val VTPropertyKey_Font = "font"
        val VTPropertyKey_BorderColor = "border-color"
    }
    private val themeData = ArrayList<ThemeData>()

    fun set(key: String, value: Any, variant: String? = null){
        themeData.removeIf{it.key == key && it.variant == variant}
        themeData.add(ThemeData(key, variant, value))
    }

    fun get(key: String, variant: String? = null): Any?{
        return themeData.firstOrNull{it.key == key && it.variant == variant}?.value
    }
}
