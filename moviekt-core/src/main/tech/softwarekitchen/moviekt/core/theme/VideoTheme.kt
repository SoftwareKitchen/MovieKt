package tech.softwarekitchen.moviekt.theme

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
