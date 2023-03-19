package tech.softwarekitchen.moviekt.clips.video.shape

import tech.softwarekitchen.common.vector.Vector2
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.position.toStaticSizeProvider
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Polygon
import java.awt.image.BufferedImage

data class ArrowVideoClipConfiguration(
    val length: Double = 100.0,
    val width: Double = 30.0,
    val angle: Double = 0.0,
    val outlineWidth: Float = 3f,
    val outlineColor: Color = Color.WHITE,
    val fillColor: Color = Color.RED
)

class ArrowVideoClip(
    private val configuration: ArrowVideoClipConfiguration = ArrowVideoClipConfiguration(),
    tOffset: Float = 0f,
    visibilityDuration: Float? = null
): VideoClip(Vector2i(
    (Math.abs(Math.cos(configuration.angle)) * (configuration.width + configuration.outlineWidth * 2) + Math.abs(Math.sin(configuration.angle)) * (configuration.length + configuration.outlineWidth * 2)).toInt(),
    (Math.abs(Math.cos(configuration.angle)) * (configuration.length + configuration.outlineWidth * 2) + Math.abs(Math.sin(configuration.angle)) * (configuration.width + configuration.outlineWidth * 2)).toInt()
).toStaticSizeProvider()
    ,tOffset, visibilityDuration) {
    override fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float): BufferedImage {
        val img = generateEmptyImage(frameNo, nFrames, tTotal)
        val size = size(frameNo, nFrames, tTotal)

        val center = Vector2(size.x / 2.0, size.y / 2.0)
        val halfVector = Vector2(Math.sin(configuration.angle) * configuration.length / 2, Math.cos(configuration.angle) * configuration.length / 2)
        val halfSideVector = Vector2(Math.cos(configuration.angle) * configuration.width / 2, -Math.sin(configuration.angle) * configuration.width / 2)

        val arrowHeadLen = 0.2
        val arrowRelWidth = 0.3

        val arrowShape = Polygon()
        val arrowTip = center.plus(halfVector)
        val arrowTipBaseOuter1 = center.plus(halfVector.scale(1 - arrowHeadLen * 2)).plus(halfSideVector)

        val arrowTipBaseOuter2 = center.plus(halfVector.scale(1 - arrowHeadLen * 2)).minus(halfSideVector)
        val arrowTipBaseInner1 = center.plus(halfVector.scale(1 - arrowHeadLen * 2)).plus(halfSideVector.scale(arrowRelWidth))
        val arrowTipBaseInner2 = center.plus(halfVector.scale(1 - arrowHeadLen * 2)).minus(halfSideVector.scale(arrowRelWidth))
        val arrowEnd1 = center.minus(halfVector).plus(halfSideVector.scale(arrowRelWidth))
        val arrowEnd2 = center.minus(halfVector).minus(halfSideVector.scale(arrowRelWidth))
        arrowShape.addPoint(arrowTip.x.toInt(), arrowTip.y.toInt())
        arrowShape.addPoint(arrowTipBaseOuter1.x.toInt(), arrowTipBaseOuter1.y.toInt())
        arrowShape.addPoint(arrowTipBaseInner1.x.toInt(), arrowTipBaseInner1.y.toInt())
        arrowShape.addPoint(arrowEnd1.x.toInt(), arrowEnd1.y.toInt())
        arrowShape.addPoint(arrowEnd2.x.toInt(), arrowEnd2.y.toInt())
        arrowShape.addPoint(arrowTipBaseInner2.x.toInt(), arrowTipBaseInner2.y.toInt())
        arrowShape.addPoint(arrowTipBaseOuter2.x.toInt(), arrowTipBaseOuter2.y.toInt())

        val graphics = img.createGraphics()
        graphics.color = configuration.fillColor
        graphics.fillPolygon(arrowShape)
        graphics.color = configuration.outlineColor
        graphics.stroke = BasicStroke(configuration.outlineWidth)
        graphics.drawPolygon(arrowShape)

        return img
    }
}
