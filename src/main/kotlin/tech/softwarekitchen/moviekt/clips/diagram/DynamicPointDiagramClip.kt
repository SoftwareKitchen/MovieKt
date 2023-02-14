package tech.softwarekitchen.moviekt.clips.diagram

import tech.softwarekitchen.common.vector.Vector2i
import java.awt.Color
import java.awt.image.BufferedImage

data class XYDataPoint(val x: Double, val y: Double)
class DynamicPointDiagramClip(
    base: Vector2i,
    size: Vector2i,
    tOffset: Float,
    private val dataProvider: () -> List<XYDataPoint>,
    configuration: XYDiagramConfiguration = XYDiagramConfiguration(),
    visibilityDuration: Float? = null
): XYDiagramClip(
    base, size, tOffset, visibilityDuration,yAxis = configuration.yAxis, xAxis = configuration.xAxis,
    configuration = configuration,

) {
    override fun generateDataDisplay(
        size: Vector2i,
        frameNo: Int,
        nFrames: Int,
        tTotal: Float,
        tInternal: Float
    ): BufferedImage {
        val img = BufferedImage(size.x,size.y,BufferedImage.TYPE_INT_ARGB)
        drawBackgroundGrid(img)

        val graphics = img.createGraphics()
        val (xScale, yScale) = getScreenMapper(size)

        val data = dataProvider()
        val bounds = getDataBounds()
        graphics.color = Color.WHITE
        data.forEach {
            val x = xScale(it.x)
            val y = yScale(it.y)
            graphics.drawLine(x-5,y-5,x+5,y+5)
            graphics.drawLine(x-5,y+5,x+5,y-5)
        }

        return img
    }

    override fun getData(): List<Pair<Double, Double>> {
        return dataProvider().map{Pair(it.x, it.y)}
    }
}
