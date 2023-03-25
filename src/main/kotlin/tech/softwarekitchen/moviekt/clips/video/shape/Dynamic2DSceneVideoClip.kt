package tech.softwarekitchen.moviekt.clips.video.shape

import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Shape
import java.awt.image.BufferedImage

data class ShapePaintConfiguration(val shape: Shape, val strokeColor: Color, val fillColor: Color?, val strokeWidth: Int = 1)
class Dynamic2DSceneVideoClip(
    size: SizeProvider,
    private val shapeProvider: (Int, Int, Float) -> List<ShapePaintConfiguration>,
    tOffset: Float = 0f,
    visibilityDuration: Float? = null
) : VideoClip(size,tOffset,visibilityDuration){
    override fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float): BufferedImage {
        val img = generateEmptyImage(frameNo, nFrames, tTotal)
        val graphics = img.createGraphics()
        shapeProvider(frameNo, nFrames, tTotal).forEach{
            shape ->
            shape.fillColor?.let{
                graphics.color = it
                graphics.fill(shape.shape)
            }
            graphics.color = shape.strokeColor
            graphics.stroke = BasicStroke(shape.strokeWidth.toFloat())
            graphics.draw(shape.shape)
        }
        return img
    }
}
