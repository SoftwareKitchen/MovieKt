package tech.softwarekitchen.moviekt.clips.video.diagram.impl

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.diagram.BarBasedDiagramConfiguration
import tech.softwarekitchen.moviekt.clips.video.diagram.BarBasedDiagramVideoClip
import java.awt.BasicStroke
import java.awt.Color
import java.awt.image.BufferedImage

class DynamicHorizontalSpanDiagramVideoClip(
    size: SizeProvider,
    private val dataProvider: () -> List<Pair<Double, Double>>,
    private val configuration: (Int, Int, Float) -> BarBasedDiagramConfiguration = {cur, tot, t -> BarBasedDiagramConfiguration()},
    tOffset: Float = 0f,
    visibilityDuration: Float? = null,
): BarBasedDiagramVideoClip(size, tOffset, visibilityDuration, configuration = configuration) {

    constructor(
        size: SizeProvider,
        dataProvider: () -> List<Pair<Double, Double>>,
        configuration: BarBasedDiagramConfiguration = BarBasedDiagramConfiguration(),
        tOffset: Float = 0f,
        visibilityDuration: Float? = null,
    ): this(size, dataProvider, {_,_,_ -> configuration}, tOffset, visibilityDuration)

    override fun generateDataDisplay(size: Vector2i, frameNo: Int, nFrames: Int, tTotal: Float): BufferedImage {
        val image = BufferedImage(size.x,size.y,BufferedImage.TYPE_INT_ARGB)
        val data = dataProvider()
        val xScale = getXScreenMapper(frameNo, nFrames, tTotal, size)
        val yScale = getYScreenMapper(frameNo, nFrames, tTotal, size, true)
        val dataMapped = data.map{Pair(xScale(it.first), xScale(it.second))}

        val graphics = image.createGraphics()

        graphics.color = Color(255,0,0,64)

        dataMapped.forEachIndexed{
            i, v ->
            graphics.fillRect(
                v.first,(yScale(i+1.0)),v.second - v.first,(yScale(i+0.0) - yScale(i+1.0))
            )
        }

        graphics.color = Color.WHITE
        graphics.stroke = BasicStroke(1f)

        dataMapped.forEachIndexed{
            i,v ->
            graphics.drawRect(
                v.first,(yScale(i+1.0)),v.second - v.first,(yScale(i+0.0) - yScale(i+1.0))
            )
        }

        return image
    }

    override fun getData(): List<Pair<Double, Double>> {
        return dataProvider().mapIndexed{i,v -> listOf(Pair(i.toDouble(),v.first), Pair(i.toDouble(), v.second))}.flatten()
    }
}
