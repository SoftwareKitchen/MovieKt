package tech.softwarekitchen.moviekt.clips.video.diagram.bar

import tech.softwarekitchen.common.vector.Vector2i
import java.awt.BasicStroke
import java.awt.Color
import java.awt.image.BufferedImage

class DynamicHorizontalStackedBarDiagramVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val dataProvider: () -> List<Double>,
    private val configuration: BarBasedDiagramConfiguration,
): BarBasedDiagramVideoClip(id, size,position, visible, configuration, volatile = true) {

    override fun generateDataDisplay(size: Vector2i): BufferedImage {
        val image = BufferedImage(size.x,size.y,BufferedImage.TYPE_INT_ARGB)
        val data = dataProvider()
        val xScale = getXScreenMapper(size)
        val dataMapped = data.mapIndexed{ i,v -> data.subList(0, i).sum()}.map(xScale)

        val graphics = image.createGraphics()

        graphics.color = Color(255,0,0,64)

        dataMapped.forEachIndexed{
            i, v ->
            val base = if(i == 0){ 0 } else { dataMapped[i-1] }
            val wid = dataMapped[i] - base
            graphics.fillRect(
                base,0,wid,size.y-1
            )
        }

        graphics.color = Color.WHITE
        graphics.stroke = BasicStroke(1f)

        dataMapped.forEachIndexed{
             i,v ->
            val base = if(i == 0){ 0 } else { dataMapped[i-1] }
            val wid = dataMapped[i] - base
            graphics.drawRect(
                base,0,wid,size.y-1
            )
        }

        return image
    }

    override fun getData(): List<Pair<Double, Double>> {
        return dataProvider().mapIndexed{i,v -> Pair(i.toDouble(),v)}
    }
}
