package tech.softwarekitchen.moviekt.clips.video.diagram.impl

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.diagram.BarBasedDiagramConfiguration
import tech.softwarekitchen.moviekt.clips.video.diagram.BarBasedDiagramVideoClip
import tech.softwarekitchen.moviekt.clips.video.diagram.XYDiagramVideoClip
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Polygon
import java.awt.image.BufferedImage

class DynamicVerticalBarDiagramVideoClip(
    size: Vector2i,
    private val dataProvider: () -> List<Double>,
    private val configuration: BarBasedDiagramConfiguration = BarBasedDiagramConfiguration(),
    tOffset: Float = 0f,
    visibilityDuration: Float? = null,
): BarBasedDiagramVideoClip(size, tOffset, visibilityDuration, yAxis = configuration.yAxis, xAxis = configuration.xAxis, configuration = configuration) {

    override fun generateDataDisplay(size: Vector2i, frameNo: Int, nFrames: Int, tTotal: Float, tInternal: Float): BufferedImage {
        val image = BufferedImage(size.x,size.y,BufferedImage.TYPE_INT_ARGB)
        val data = dataProvider()
        val yScale = getScreenMapper(size)
        val dataMapped = data.map(yScale)

        val graphics = image.createGraphics()

        graphics.color = Color(255,0,0,64)

        val widthPerBar = size.x.toDouble() / dataMapped.size.toDouble()

        dataMapped.forEachIndexed{
            i, v ->
            graphics.fillRect((i * widthPerBar).toInt(),v,widthPerBar.toInt(),size.y - v)
        }

        graphics.color = Color.WHITE
        graphics.stroke = BasicStroke(1f)

        dataMapped.forEachIndexed{
            i,v ->
            graphics.drawRect((i * widthPerBar).toInt(),v,widthPerBar.toInt(),size.y - v+1)
        }

        return image
    }

    override fun getData(): List<Pair<Double, Double>> {
        return dataProvider().mapIndexed{i,v -> Pair(i.toDouble(),v)}
    }
}
