package tech.softwarekitchen.moviekt.theme

import tech.softwarekitchen.moviekt.core.Movie
import tech.softwarekitchen.moviekt.core.extension.MovieKtExtension
import tech.softwarekitchen.moviekt.core.extension.MovieKtVideoExtensionContainer
import tech.softwarekitchen.moviekt.core.video.VideoClip

class MovieKtThemeExtension: MovieKtExtension

class VideoClipThemeProperty<T>(name: String, initialValue: T, onChange: (T) -> Unit, converter: (Any) -> T = {it as T}): VideoClip.VideoClipProperty<T>(name, initialValue, onChange, converter)

class MovieKtThemeContainer(override val videoClip: VideoClip): MovieKtVideoExtensionContainer{
    private var theme: VideoTheme? = null
    private var variant: String? = null

    init{
        videoClip.addAddChildListeners {
            it.themeContainer().setTheme(theme)
        }
    }

    fun setVariant(variant: String?){
        this.variant = variant
        updateTheme()
    }

    fun setTheme(theme: VideoTheme?) {
        this.theme = theme
        updateTheme()
    }

    private fun updateTheme(){
        theme?.let{
            t ->
            val keys = t.getAvailableKeys()
            val kv = keys.map{Pair(it, t.get(it, variant))}
            kv.forEach{
                if(videoClip.has(it.first)) {
                    if (it.second == null) {
                        videoClip.reset(it.first)
                    } else {
                        videoClip.set(it.first, it.second!!)
                    }
                }
            }
        }

        videoClip.onChildren {
            it.themeContainer().setTheme(theme)
        }
    }
}

fun VideoClip.themeContainer(): MovieKtThemeContainer {
    return getExtensionContainer<MovieKtThemeContainer>(::MovieKtThemeContainer)
}

fun Movie.applyTheme(theme: VideoTheme?){
    this.onRoot { it.themeContainer().setTheme(theme) }
}