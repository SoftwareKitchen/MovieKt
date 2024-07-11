package tech.softwarekitchen.moviekt.filter

import tech.softwarekitchen.moviekt.core.extension.MovieKtVideoExtensionContainer
import tech.softwarekitchen.moviekt.core.video.VideoClip
import tech.softwarekitchen.moviekt.util.Pixel
import java.awt.image.BufferedImage

class MovieKtFilterContainer(
    override val videoClip: VideoClip
): MovieKtVideoExtensionContainer {
    private val filters = ArrayList<VideoClipFilter>()

    fun addFilter(filter: VideoClipFilter){
        filters.add(filter)
    }

    override fun pixel(img: BufferedImage, x: Int, y: Int, prev: Pixel): Pixel {
        var pixel = prev
        filters.forEach{ pixel = it.filter(img, x,y, pixel) }
        return pixel
    }
}

fun VideoClip.filterChain(): MovieKtFilterContainer{
    return getExtensionContainer<MovieKtFilterContainer>(::MovieKtFilterContainer)
}
