package tech.softwarekitchen.moviekt.clips.video.image.svg.draw

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.image.svg.CoordinateMapper
import tech.softwarekitchen.moviekt.clips.video.image.svg.Scaler2D
import tech.softwarekitchen.moviekt.clips.video.image.svg.getFillColor
import tech.softwarekitchen.moviekt.clips.video.image.svg.mergeStyles
import tech.softwarekitchen.moviekt.clips.video.image.svg.model.SVGPath
import tech.softwarekitchen.moviekt.clips.video.image.svg.model.SVGStyle
import tech.softwarekitchen.moviekt.clips.video.image.svg.model.toInt
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.awt.image.DataBufferInt
import java.nio.IntBuffer

class SVGComplexPathDrawer: SVGPartialDrawer<SVGPath> {
    private val simpleDrawer = SVGSimplePathDrawer()
    override fun draw(
        it: SVGPath,
        size: Vector2i,
        target: Graphics2D,
        coordinateMapper: CoordinateMapper,
        scaler: Scaler2D,
        parentStyles: List<SVGStyle>
    ) {
        val styles = mergeStyles(parentStyles, it.styles)
        val fillBuffer = BooleanArray(size.x * size.y){false}
        val testArea = BufferedImage(size.x, size.y, TYPE_INT_ARGB)
        val testGraphics = testArea.createGraphics()
        val clearArea: () -> Unit = {
            for(x in 0 until size.x){
                for(y in 0 until size.y){
                    testArea.setRGB(x,y,0)
                }
            }
        }

        val pathSplit = it.split()

        pathSplit.forEachIndexed{
            pi, it ->
            clearArea()
            simpleDrawer.draw(it,size, testGraphics, coordinateMapper, scaler, parentStyles)
            val raw = (testArea.data.dataBuffer as DataBufferInt).data
            for(i in 0 until size.x * size.y){
                if(raw[i] != 0){
                    fillBuffer[i] = !fillBuffer[i]
                }
            }
        }

        clearArea()
        val fillColor = styles.getFillColor()!!
        for(x in 0 until size.x){
            for(y in 0 until size.y){
                val fillIndex = y * size.x + x
                if(fillBuffer[fillIndex]){
                    testArea.setRGB(x,y,fillColor.toInt())
                }
            }
        }
        target.drawImage(testArea,0,0,null)

        //Stroke
        simpleDrawer.drawCustom(it, size, target, coordinateMapper, scaler, parentStyles, true)

    }
}
