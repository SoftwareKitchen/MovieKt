package tech.softwarekitchen.moviekt.core.extension

import tech.softwarekitchen.moviekt.core.Movie
import tech.softwarekitchen.moviekt.core.video.VideoClip
import tech.softwarekitchen.moviekt.util.Pixel
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt

interface MovieKtExtension{
    fun prepare(movie: Movie){}
    fun frame(t: Float){}
}

interface MovieKtVideoExtensionContainer{
    val videoClip: VideoClip

    fun pixel(img: BufferedImage, x: Int, y: Int, pixel: Pixel): Pixel {
        return pixel
    }
}

