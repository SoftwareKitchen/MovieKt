package tech.softwarekitchen.moviekt.clips.video.diagram

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.diagram.impl.DynamicDiagramBackgroundGrid
import java.awt.Color
import java.awt.image.BufferedImage

abstract class PointBasedDiagramVideoClip(
    size: SizeProvider,
    tOffset: Float, visibilityDuration: Float? = null,
    private val configuration: (Int, Int, Float) -> XYDiagramConfiguration
): XYDiagramVideoClip(
    size, tOffset, visibilityDuration = visibilityDuration, configuration = configuration
) {
    constructor(
        size: SizeProvider,
        tOffset: Float, visibilityDuration: Float? = null,
        configuration: XYDiagramConfiguration
    ): this(size, tOffset, visibilityDuration, {_,_,_ -> configuration})

    protected fun drawBackgroundGrid(cur: Int, tot: Int, t: Float, image: BufferedImage, totSize: Vector2i){
        val graphics = image.createGraphics()
        graphics.color = Color(255,255,255,128)
        when(configuration(cur, tot, t).grid){
            DynamicDiagramBackgroundGrid.None -> {}
            DynamicDiagramBackgroundGrid.X -> {
                for(item in getXLegendEntries(cur,tot,t, image.width)){
                    graphics.fillRect(item.pos-1,0,3 , totSize.y)
                }
            }
            DynamicDiagramBackgroundGrid.Y -> {
                for(item in getYLegendEntries(cur,tot,t, image.height)){
                    graphics.fillRect(0,item.pos-1,totSize.x , 3)
                }
            }
        }
    }
}
