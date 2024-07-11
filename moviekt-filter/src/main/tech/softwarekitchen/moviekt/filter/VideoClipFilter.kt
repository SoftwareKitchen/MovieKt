package tech.softwarekitchen.moviekt.filter

import tech.softwarekitchen.moviekt.util.Pixel
import java.awt.image.BufferedImage

interface VideoClipFilter {
    fun filter(img: BufferedImage, x: Int, y: Int, prev: Pixel): Pixel
}
