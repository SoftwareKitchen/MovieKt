package tech.softwarekitchen.moviekt.clips.video.diagram.bar

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.diagram.bar.BarBasedDiagramConfiguration
import tech.softwarekitchen.moviekt.clips.video.diagram.bar.BarBasedDiagramVideoClip
import java.awt.Color
import java.awt.image.BufferedImage

data class ColoredDataProvider(val provider: () -> List<Double>, val color: Color)

class DynamicVerticalStackedBarDiagramVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    providers: List<ColoredDataProvider>,
    private val configuration: BarBasedDiagramConfiguration
): BarBasedDiagramVideoClip(id, size, position, visible, configuration = configuration,volatile = true)
{
    companion object{
        val PropertyKey_Providers = "Providers"
    }

    override val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val providerProperty = VideoClipProperty(PropertyKey_Providers, providers,this::markDirty)
    init{
        registerProperty(providerProperty)
    }

    fun updateProviders(providers: List<ColoredDataProvider>){
        providerProperty.set(providers)
    }
    override fun getData(): List<Pair<Double, Double>> {
        val data = providerProperty.v.map{it.provider()}
        return data[0].indices.map{index -> Pair(index.toDouble(), data.sumOf{it[index]})}
    }

    override fun generateDataDisplay(size: Vector2i): BufferedImage {
        val image = BufferedImage(size.x, size.y, BufferedImage.TYPE_INT_ARGB)
        val data = providerProperty.v.map{Pair(it.provider(), it.color)}
        val xScale = getXScreenMapper(size)
        val yScale = getYScreenMapper(size)
        val numDataPoints = data[0].first.size
        val currentHeight = Array(numDataPoints){0.0}
        val graphics = image.createGraphics()


        data.forEach{
            graphics.color = it.second
            for(i in 0 until it.first.size){
                val x0 = xScale(i.toDouble())
                val x1 = xScale((i+1).toDouble())
                val y0 = yScale(currentHeight[i])
                val y1 = yScale(currentHeight[i] + it.first[i])
                currentHeight[i] += it.first[i]

                graphics.fillRect(x0,y1,x1-x0,y0-y1)
            }
        }
        return image
    }
}