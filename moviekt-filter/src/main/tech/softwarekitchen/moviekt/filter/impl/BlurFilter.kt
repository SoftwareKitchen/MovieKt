package tech.softwarekitchen.moviekt.filter.impl

import tech.softwarekitchen.moviekt.filter.VideoClipFilter
import tech.softwarekitchen.moviekt.util.Pixel
import java.awt.image.BufferedImage
import kotlin.math.ceil
import kotlin.math.hypot

class BlurFilter(val width: Float): VideoClipFilter {
    override fun filter(img: BufferedImage, x: Int, y: Int, prev: Pixel): Pixel {
        val wCeil = ceil(width).toInt()
        var n = 0
        var sumR = 0
        var sumG = 0
        var sumB = 0
        val a = prev.a

        for(dx in -wCeil..wCeil){
            for(dy in -wCeil..wCeil){
                val r = hypot(dx.toFloat(), dy.toFloat())
                if(r > width){ continue }
                val effX = x + dx
                if(effX < 0 || effX >= img.width){ continue }
                val effY = y + dy
                if(effY < 0 || effY >= img.height){ continue }
                val rgb = img.getRGB(effX, effY)
                sumR += (rgb / 65536) % 256
                sumG += (rgb / 256) % 256
                sumB += rgb % 256
                n++
            }
        }
        return Pixel(a, (sumR / n).toUByte(), (sumG / n).toUByte(), (sumB / n).toUByte())
    }
}
