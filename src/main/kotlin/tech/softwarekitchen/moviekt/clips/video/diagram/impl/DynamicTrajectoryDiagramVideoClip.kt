package tech.softwarekitchen.moviekt.clips.video.diagram.impl

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.diagram.DiagramAxisConfiguration
import tech.softwarekitchen.moviekt.clips.video.diagram.XYDiagramConfiguration
import tech.softwarekitchen.moviekt.clips.video.diagram.XYDiagramVideoClip
import java.awt.Color
import java.awt.image.BufferedImage

data class DynamicTrajectoryDiagramVideoClipConfiguration(
    override val xAxis: DiagramAxisConfiguration = DiagramAxisConfiguration(),
    override val yAxis: DiagramAxisConfiguration = DiagramAxisConfiguration(),
    override val grid: DynamicDiagramBackgroundGrid = DynamicDiagramBackgroundGrid.None,
    override val colors: DynamicLineDiagramColorConfiguration = DynamicLineDiagramColorConfiguration()
): XYDiagramConfiguration

class DynamicTrajectoryDiagramVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val providers: List<() -> List<Pair<Double, Double>>>,
    private val configuration: DynamicTrajectoryDiagramVideoClipConfiguration,
     tOffset: Float = 0f, visibilityDuration: Float? = null
): XYDiagramVideoClip(
    id, size, position, visible,
    configuration
) {
    companion object{
        val colors = listOf(Color.YELLOW, Color.BLUE)
    }

    override fun getData(): List<Pair<Double, Double>> {
        return providers.map{it()}.flatten()
    }

    override fun generateDataDisplay(size: Vector2i): BufferedImage {
        val (xScale, yScale) = getScreenMapper(size)
        val data = providers.map{it()}
        val maxIndex = data.maxOf { it.size }
        val image = BufferedImage(size.x,size.y,BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()



        for(i in 1 ..maxIndex){
            data.forEachIndexed{
                index, it ->
                if(it.size > i){
                    val col = colors[index % colors.size]
                    val srcX = xScale(it[i-1].first)
                    val srcY = yScale(it[i-1].second)
                    val tgtX = xScale(it[i].first)
                    val tgtY = yScale(it[i].second)
                    graphics.color = col
                    graphics.drawLine(srcX, srcY, tgtX, tgtY)
                }
            }
        }

        return image
    }
}
