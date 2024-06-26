package tech.softwarekitchen.moviekt.clips.video.shape

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.core.util.VideoTimestamp
import tech.softwarekitchen.moviekt.core.video.VideoClip
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Shape
import java.awt.image.BufferedImage

data class ShapePaintConfiguration(val shape: Shape, val strokeColor: Color, val fillColor: Color?, val strokeWidth: Int = 1)
class Dynamic2DSceneVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val shapeProvider: List<ShapePaintConfiguration>,
) : VideoClip(id, size, position, visible){
    override val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val graphics = img.createGraphics()
        shapeProvider.forEach{
            shape ->
            shape.fillColor?.let{
                graphics.color = it
                graphics.fill(shape.shape)
            }
            graphics.color = shape.strokeColor
            graphics.stroke = BasicStroke(shape.strokeWidth.toFloat())
            graphics.draw(shape.shape)
        }
    }
}
