package tech.softwarekitchen.moviekt.core

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.awt.image.DataBufferInt
import kotlin.math.roundToInt

private val ZeroAlpha = 0u.toUByte()
private val FullAlpha = 255u.toUByte()

//TODO Limit buffers to required size only for mem opt
private class LayerBuffer(
    private val depth: Int,
    private val size: Vector2i,
    private val clip: VideoClip,
    private val onChange: (Int, Int, Int) -> Unit
){
    @OptIn(ExperimentalUnsignedTypes::class)
    val buffer = UByteArray(4 * size.x * size.y)
    private var position: Vector2i = clip.getPosition()
    private var cache: BufferedImage
    private val sublayers: List<LayerBuffer>
    private val depthMap = Array(size.x){Array(size.y){-1} }

    init{
        val clipSize = clip.getSize()
        cache = BufferedImage(clipSize.x, clipSize.y, TYPE_INT_ARGB)
        if(clip.isVisible()){
            clip.renderContent(cache)
        }

        sublayers = clip.getChildren().reversed().mapIndexed{
            depth, child ->
            LayerBuffer(
                depth,
                size,
                child,
                this::onPixelChange
            )
        }
    }

    fun init(){
        sublayers.forEach{it.init()}
        for(x in 0 until cache.width){
            for(y in 0 until cache.height){
                onPixelChange(x,y,0)
            }
        }
    }

    fun getIndexByParentIndex(x: Int, y: Int): Int?{
        val innerX = x - position.x
        if(innerX < 0 || innerX >= cache.width){
            return null
        }
        val innerY = y - position.y
        if(innerY < 0 || innerY >= cache.height){
            return null
        }
        return (y - position.y) * size.x + (x - position.x)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun onPixelChange(x: Int, y: Int, depth: Int){
        val linearIndex = y * size.x + x
        if(depthMap[x][y] < 0 || depth <= depthMap[x][y] ){
            var r: UByte = 0u
            var g: UByte = 0u
            var b: UByte = 0u
            var a: UByte = 0u

            for(index in sublayers.indices){
                if(!sublayers[index].clip.isVisible()){
                    continue
                }
                val sublayerIndex = sublayers[index].getIndexByParentIndex(x,y) ?: continue
                val _a = sublayers[index].buffer[4*sublayerIndex]
                if(_a != ZeroAlpha){
                    val _r = sublayers[index].buffer[4*sublayerIndex+1]
                    val _g = sublayers[index].buffer[4*sublayerIndex+2]
                    val _b = sublayers[index].buffer[4*sublayerIndex+3]
                    r = ((r * a) + (_r * (255u - a)) / 255u).toUByte()
                    g = ((g * a) + (_g * (255u - a)) / 255u).toUByte()
                    b = ((b * a) + (_b * (255u - a)) / 255u).toUByte()
                    a = (a + (255u - a) * (_a / 255u)).toUByte()
                }
                if(a == FullAlpha){
                    depthMap[x][y] = index
                    break
                }
            }

            if(a < FullAlpha){
                val innerIndex = y * cache.width + x
                val ownPixel = (cache.raster.dataBuffer as DataBufferInt).data[innerIndex].toUInt()
                val _a = ownPixel.shr(24) % 256u
                if(_a != 0u){
                    val _r = ownPixel.shr(16) % 256u
                    val _g = ownPixel.shr(8) % 256u
                    val _b = ownPixel % 256u
                    r = ((r * a) + (_r * (255u - a)) / 255u).toUByte()
                    g = ((g * a) + (_g * (255u - a)) / 255u).toUByte()
                    b = ((b * a) + (_b * (255u - a)) / 255u).toUByte()
                    a = (a + (255u - a) * (_a / 255u)).toUByte()
                }
                depthMap[x][y] = sublayers.size
            }

            buffer[4 * linearIndex] = a
            buffer[4 * linearIndex + 1] = r
            buffer[4 * linearIndex + 2] = g
            buffer[4 * linearIndex + 3] = b

            onChange(x+position.x,y+position.y,this.depth)
        }
    }

    //TODO Alpha change
    fun update(){
        if(!clip.needsRepaint()){
            return
        }
        sublayers.forEach{
            it.update()
        }
        if(!clip.needsRepaint()){
            return
        }

        val p = clip.getPosition()
        if(!p.equals(position)){
            //TODO
        }
        val s = clip.getSize()
        if(s.x != cache.width || s.y != cache.height){
            //TODO
        }
        val cacheData = (cache.raster.dataBuffer as DataBufferInt).data
        val img = BufferedImage(s.x, s.y, TYPE_INT_ARGB)
        if(clip.isVisible()){
            clip.renderContent(img)
        }
        val fullRepaintRequired = clip.hasOpacityChanged()
        clip.clearRepaintFlags()
        cache = img

        if(fullRepaintRequired){
            for(x in 0 until img.width){
                for(y in 0 until img.height){
                    onPixelChange(x,y,0)
                }
            }
            return
        }

        val updatedData = (img.raster.dataBuffer as DataBufferInt).data
        cacheData.indices.forEach{
            index ->
            if(cacheData[index] != updatedData[index]){
                val x = index % s.x
                val y = index / s.x
                onPixelChange(x,y,sublayers.size)
            }
        }
    }
}
class RenderBuffer(
    val root: VideoClip
) {
    private val size = root.getSize()
    val resultBuffer = ByteArray(size.x * size.y * 3)
    private val rootLayer = LayerBuffer(0,size, root, this::onPixelChange)

    init{
        rootLayer.init()
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun onPixelChange(x: Int, y: Int, depth: Int){
        val linearIndex = y * size.x + x
        resultBuffer[linearIndex * 3] = rootLayer.buffer[linearIndex*4+1].toByte()
        resultBuffer[linearIndex * 3+1] = rootLayer.buffer[linearIndex*4+2].toByte()
        resultBuffer[linearIndex * 3+2] = rootLayer.buffer[linearIndex*4+3].toByte()
    }

    fun update(){
        rootLayer.update()
    }
}
