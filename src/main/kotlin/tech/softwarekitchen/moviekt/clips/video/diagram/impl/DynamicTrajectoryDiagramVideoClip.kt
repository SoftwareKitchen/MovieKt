package tech.softwarekitchen.moviekt.clips.video.diagram.impl

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.VideoClip
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
    private val providers: List<() -> List<Pair<Double, Double>>>,
    private val configuration: DynamicTrajectoryDiagramVideoClipConfiguration,
    size: SizeProvider, tOffset: Float = 0f, visibilityDuration: Float? = null
): XYDiagramVideoClip(
    size, tOffset, visibilityDuration,
    configuration
) {
    companion object{
        val colors = listOf(Color.BLUE, Color.YELLOW)
    }

    override fun getData(): List<Pair<Double, Double>> {
        return providers.map{it()}.flatten()
    }

    override fun generateDataDisplay(size: Vector2i, frameNo: Int, nFrames: Int, tTotal: Float): BufferedImage {
        val (xScale, yScale) = getScreenMapper(size)
        val data = providers.map{it()}
        val maxIndex = data.maxOf { it.size }
        val image = BufferedImage(size.x,size.y,BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()



        for(i in 1 ..maxIndex){
            data.forEach{
                if(it.size > i){
                    val col = colors[i % colors.size]
                    val srcX = xScale(it[i-1].first)
                    val srcY = xScale(it[i-1].first)
                    val tgtX = xScale(it[i].first)
                    val tgtY = xScale(it[i].first)
                    graphics.color = col
                    graphics.drawLine(srcX, srcY, tgtX, tgtY)
                }
            }
        }

        return image
    }
}
