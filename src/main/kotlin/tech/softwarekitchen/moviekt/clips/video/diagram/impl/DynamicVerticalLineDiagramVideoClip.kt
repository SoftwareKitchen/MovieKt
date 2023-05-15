package tech.softwarekitchen.moviekt.clips.video.diagram.impl

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.diagram.PointBasedDiagramVideoClip
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Polygon
import java.awt.image.BufferedImage

class DynamicVerticalLineDiagramVideoClip(
    size: SizeProvider,
    private val dataProviders: List<() -> List<Double>>,
    private val configuration: DynamicLineDiagramVideoClipConfiguration = DynamicLineDiagramVideoClipConfiguration(),
    tOffset: Float = 0f,
    visibilityDuration: Float? = null,
): PointBasedDiagramVideoClip(size, tOffset, visibilityDuration, configuration = configuration) {
    companion object{
        val colors = listOf(Color.YELLOW, Color.BLUE)
        val fillColors = listOf(Color(255,255,0,64), Color(0,0,255,64))
    }

    override fun generateDataDisplay(size: Vector2i, frameNo: Int, nFrames: Int, tTotal: Float): BufferedImage {
        val image = BufferedImage(size.x,size.y,BufferedImage.TYPE_INT_ARGB)

        val data = dataProviders.map{it()}
        val (xScale, yScale) = getScreenMapper(frameNo, nFrames,tTotal, size)

        val longest = data.maxOf{it.size}
        val yMapped = (0..longest).map{yScale(it.toDouble() + 0.5)}

        val graphics = image.createGraphics()

        drawBackgroundGrid(frameNo, nFrames, tTotal, image, size)

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
