package tech.softwarekitchen.moviekt.filter

import tech.softwarekitchen.moviekt.util.Pixel

interface VideoClipFilter {
    fun filter(x: Int, y: Int, xSize: Int, ySize: Int, pixel: Pixel): Pixel
}
