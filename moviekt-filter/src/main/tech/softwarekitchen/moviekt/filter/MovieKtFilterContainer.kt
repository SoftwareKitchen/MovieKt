package tech.softwarekitchen.moviekt.filter

import tech.softwarekitchen.moviekt.core.extension.MovieKtVideoExtensionContainer
import tech.softwarekitchen.moviekt.core.video.VideoClip
import tech.softwarekitchen.moviekt.util.Pixel

class MovieKtFilterContainer(
    override val videoClip: VideoClip
): MovieKtVideoExtensionContainer {
    private val filters = ArrayList<VideoClipFilter>()

    fun addFilter(filter: VideoClipFilter){
        filters.add(filter)
    }

    override fun pixel(x: Int, y: Int, prev: Pixel): Pixel {
        var pixel = prev
        filters.forEach{ pixel = it.filter(x,y,videoClip.getSize().x, videoClip.getSize().y, pixel) }
        return pixel
    }
}

fun VideoClip.filterChain(): MovieKtFilterContainer{
    return getExtensionContainer<MovieKtFilterContainer>(::MovieKtFilterContainer)
}
