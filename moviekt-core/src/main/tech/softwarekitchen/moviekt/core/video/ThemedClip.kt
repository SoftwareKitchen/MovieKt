package tech.softwarekitchen.moviekt.core.video

import org.slf4j.Logger
import tech.softwarekitchen.moviekt.theme.VideoTheme

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
