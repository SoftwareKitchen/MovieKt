package tech.softwarekitchen.moviekt.clips.video.diagram

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.diagram.impl.DynamicDiagramBackgroundGrid
import java.awt.Color
import java.awt.image.BufferedImage

abstract class PointBasedDiagramVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val configuration: XYDiagramConfiguration,
    volatile: Boolean = false
): XYDiagramVideoClip(
    id, size, position, visible, configuration, volatile
) {
    protected fun drawBackgroundGrid(image: BufferedImage, totSize: Vector2i){
        val graphics = image.createGraphics()
        graphics.color = Color(255,255,255,128)
        when(configuration.grid){
            DynamicDiagramBackgroundGrid.None -> {}
            DynamicDiagramBackgroundGrid.X -> {
                for(item in getXLegendEntries(image.width)){
                    graphics.fillRect(item.pos-1,0,3 , totSize.y)
                }
            }
            DynamicDiagramBackgroundGrid.Y -> {
                for(item in getYLegendEntries(image.height)){
                    graphics.fillRect(0,item.pos-1,totSize.x , 3)
                }
            }
        }
    }
}
