package tech.softwarekitchen.moviekt.core.extension

import tech.softwarekitchen.moviekt.core.Movie
import tech.softwarekitchen.moviekt.core.video.VideoClip
import tech.softwarekitchen.moviekt.util.Pixel

interface MovieKtExtension{
    fun prepare(movie: Movie){}
    fun frame(t: Float){}
}

interface MovieKtVideoExtensionContainer{
    val videoClip: VideoClip

    fun pixel(x: Int, y: Int, prev: Pixel): Pixel {
        return prev
    }
}

