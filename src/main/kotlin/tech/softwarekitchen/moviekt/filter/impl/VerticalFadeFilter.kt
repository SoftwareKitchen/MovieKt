package tech.softwarekitchen.moviekt.filter.impl

import tech.softwarekitchen.moviekt.filter.VideoClipFilter
import tech.softwarekitchen.moviekt.util.Pixel

class VerticalFadeFilter(
    val midSize: Int, val padding: Int
): VideoClipFilter {
    override fun filter(x: Int, y: Int, xSize: Int, ySize: Int, pixel: Pixel): Pixel {
        val y0 = padding
        val y1 = (ySize - midSize) / 2
        val y2 = (ySize  + midSize) / 2
        val y3 = ySize - padding

        val filtered = when{
            y < y0 -> Pixel(0u,0u,0u,0u)
            y > y3 -> Pixel(0u,0u,0u,0u)
            y < y1 -> {
                val ratio = (y - y0).toDouble() / (y1 - y0)
                val a2 = (pixel.a.toDouble() * ratio).toUInt().toUByte()
                Pixel(a2, pixel.r, pixel.g, pixel.b)
            }
            y > y2 -> {
                val ratio = (y3 - y).toDouble() / (y3-y2)
                val a2 = (pixel.a.toDouble() * ratio).toUInt().toUByte()
                Pixel(a2, pixel.r, pixel.g, pixel.b)
            }
            else -> pixel
        }

        return filtered
    }
}