package tech.softwarekitchen.moviekt.clips.video.diagram.impl

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.diagram.PointBasedDiagramVideoClip
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Polygon
import java.awt.image.BufferedImage

class DynamicVerticalLineDiagramVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val dataProviders: List<() -> List<Double>>,
    private val configuration: DynamicLineDiagramVideoClipConfiguration = DynamicLineDiagramVideoClipConfiguration(),
): PointBasedDiagramVideoClip(id, size, position, visible, configuration = configuration) {
    companion object{
        val colors = listOf(Color.YELLOW, Color.BLUE)
        val fillColors = listOf(Color(255,255,0,64), Color(0,0,255,64))
    }

    override fun generateDataDisplay(size: Vector2i): BufferedImage {
        val image = BufferedImage(size.x,size.y,BufferedImage.TYPE_INT_ARGB)

        val data = dataProviders.map{it()}
        val (xScale, yScale) = getScreenMapper(size)

        val longest = data.maxOf{it.size}
        val yMapped = (0..longest).map{yScale(it.toDouble() + 0.5)}

        val graphics = image.createGraphics()

        drawBackgroundGrid(image, size)

        graphics.stroke = BasicStroke(1f)

        data.forEachIndexed{ index, it ->
            val dataMapped = it.map(xScale)

            configuration.colors.underGraphColor?.let{
                graphics.color = it
                for(i in 1 until data.size){
                    val shape = Polygon()
                    shape.addPoint(size.x, yMapped[i-1])
                    shape.addPoint(size.x, yMapped[i])
                    shape.addPoint(dataMapped[i], yMapped[i])
                    shape.addPoint(dataMapped[i-1], yMapped[i-1])

                    graphics.fill(shape)
                }
            }
            graphics.setStroke(BasicStroke(3f))
            graphics.color = colors[index % colors.size]
            for(i in 1 until dataMapped.size){
                graphics.drawLine(dataMapped[i-1], yMapped[i-1], dataMapped[i],yMapped[i])
            }
        }

        return image
    }

    override fun getData(): List<Pair<Double, Double>> {
        return dataProviders.mapIndexed{i,v -> v().map{ it -> Pair(it, i.toDouble() + 0.5)}}.flatten()
    }
}
