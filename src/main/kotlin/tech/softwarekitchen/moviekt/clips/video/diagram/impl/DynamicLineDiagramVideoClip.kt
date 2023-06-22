package tech.softwarekitchen.moviekt.clips.video.diagram.impl

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.diagram.DiagramAxisConfiguration
import tech.softwarekitchen.moviekt.clips.video.diagram.PointBasedDiagramVideoClip
import tech.softwarekitchen.moviekt.clips.video.diagram.XYDiagramConfiguration
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Polygon
import java.awt.image.BufferedImage

enum class DynamicDiagramBackgroundGrid{
    None, X, Y
}

data class DynamicLineDiagramColorConfiguration(
    val underGraphColor: Color? = null
)

data class DynamicLineDiagramVideoClipConfiguration(
    override val xAxis: DiagramAxisConfiguration = DiagramAxisConfiguration(),
    override val yAxis: DiagramAxisConfiguration = DiagramAxisConfiguration(),
    override val grid: DynamicDiagramBackgroundGrid = DynamicDiagramBackgroundGrid.None,
    override val colors: DynamicLineDiagramColorConfiguration = DynamicLineDiagramColorConfiguration()
): XYDiagramConfiguration

class DynamicLineDiagramVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val dataProvider: () -> List<Double>,
    private val configuration: DynamicLineDiagramVideoClipConfiguration = DynamicLineDiagramVideoClipConfiguration()
): PointBasedDiagramVideoClip(id, size, position, visible, configuration = configuration, volatile = true) {

    override fun generateDataDisplay(size: Vector2i): BufferedImage {
        val image = BufferedImage(size.x,size.y,BufferedImage.TYPE_INT_ARGB)
        val data = dataProvider()
        val (xScale, yScale) = getScreenMapper(size)

        val xMapped = data.indices.map{xScale(it.toDouble())}
        val dataMapped = data.map(yScale)

        val graphics = image.createGraphics()

        drawBackgroundGrid(image, size)

        graphics.color = Color(255,0,0,64)
        graphics.stroke = BasicStroke(1f)

        configuration.colors.underGraphColor?.let{
            graphics.color = it
            for(i in 1 until data.size){
                val shape = Polygon()
                shape.addPoint(xMapped[i-1], size.y)
                shape.addPoint(xMapped[i],size.y)
                shape.addPoint(xMapped[i],dataMapped[i])
                shape.addPoint(xMapped[i-1], dataMapped[i-1])

                graphics.fill(shape)
            }
        }
        graphics.setStroke(BasicStroke(3f))
        graphics.color = Color(255,0,0,255)
        for(i in 1 until data.size){
            graphics.drawLine(xMapped[i-1],dataMapped[i-1],xMapped[i],dataMapped[i])
        }

        return image
    }

    override fun getData(): List<Pair<Double, Double>> {
        return dataProvider().mapIndexed{i,v -> Pair(i.toDouble(),v)}
    }
}
