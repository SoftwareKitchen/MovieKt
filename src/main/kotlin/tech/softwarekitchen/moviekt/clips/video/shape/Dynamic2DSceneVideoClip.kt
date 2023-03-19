package tech.softwarekitchen.moviekt.clips.video.shape

import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.Shape
import java.awt.image.BufferedImage

class Dynamic2DSceneVideoClip(
    size: SizeProvider,
    tOffset: Float = 0f,
    visibilityDuration: Float? = null,
    private val shapeProvider: (Int, Int, Float) -> List<Shape>
) : VideoClip(size,tOffset,visibilityDuration){
    override fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float): BufferedImage {
        val img = generateEmptyImage(frameNo, nFrames, tTotal)
        val graphics = img.createGraphics()
        shapeProvider(frameNo, nFrames, tTotal).forEach{
            graphics.draw(it)
        }
        return img
    }
}
