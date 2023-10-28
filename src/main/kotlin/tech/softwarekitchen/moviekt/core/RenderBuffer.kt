package tech.softwarekitchen.moviekt.core

import tech.softwarekitchen.common.vector.Rectangle2i
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.clips.video.VideoTimestamp
import tech.softwarekitchen.moviekt.dsl.*
import tech.softwarekitchen.moviekt.util.Pixel
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.awt.image.DataBufferInt

private val ZeroAlpha = 0u.toUByte()
private val FullAlpha = 255u.toUByte()

private fun Vector2i.withSize(size: Vector2i): Rectangle2i{
    return Rectangle2i(x, y, size.x, size.y)
}

//TODO Limit buffers to required size only for mem opt
private class LayerBuffer(
    private var depth: Int,
    private val clip: VideoClip,
    private val onChange: (Int, Int, Int) -> Unit
){
    private val size = clip.getSize()
    @OptIn(ExperimentalUnsignedTypes::class)
    private var buffer = UByteArray(4 * size.x * size.y)
    private var position: Vector2i = clip.getPosition()
    private var visible: Boolean = false
    private var cache: BufferedImage
    private val sublayers: MutableList<LayerBuffer>
    private val depthMap = Array(size.x){Array(size.y){-1} }

    init{
        clip.addRemoveChildListener(this::removeSublayer)
        clip.addAddChildListeners(this::addSublayer)
        val clipSize = clip.getSize()
        cache = BufferedImage(clipSize.x, clipSize.y, TYPE_INT_ARGB)
        if(clip.isVisible()){
            clip.renderContent(cache, VideoTimestamp(0.0,0,0))
        }

        sublayers = clip.getChildren().reversed().mapIndexed{
            depth, child ->
            LayerBuffer(
                depth,
                child,
                this::onPixelChange
            )
        }.toMutableList()
    }

    private fun removeSublayer(childClip: VideoClip){
        val sublayer = sublayers.first{it.clip == childClip}
        val sublayerDepth = sublayers.indexOf(sublayer)
        val pixelsToRemove = ArrayList<Pair<Int, Int>>()
        sublayers.remove(sublayer)
        for(i in sublayerDepth until sublayers.size){
            sublayers[i].updateDepth(i)
        }
        for(x in 0 until size.x){
            for(y in 0 until size.y){
                if(depthMap[x][y] == sublayerDepth){
                    pixelsToRemove.add(Pair(x,y))
                    depthMap[x][y] = -1
                    onPixelChange(x,y,0)
                }
                if(depthMap[x][y] > sublayerDepth){
                    depthMap[x][y] = depthMap[x][y] - 1
                }
            }
        }
    }

    private fun addSublayer(childClip: VideoClip){
        for(x in 0 until size.x){
            for(y in 0 until size.y){
                if(depthMap[x][y] >= sublayers.size){
                    depthMap[x][y] = depthMap[x][y] + 1
                }
            }
        }
        val sublayer = LayerBuffer(
            sublayers.size,
            childClip,
            this::onPixelChange
        )
        sublayers.add(sublayer)
        sublayer.init()
    }

    private fun updateDepth(depth: Int){
        this.depth = depth
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
        return innerY * cache.width + innerX
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun onPixelChange(x: Int, y: Int, depth: Int){
        if(x < 0 || x >= cache.width || y < 0 || y >= cache.height || x >= size.x || y >= size.y){
            return
        }
        val linearIndex = y * cache.width + x
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
                    r = (((r * a) + (_r * (255u - a))) / 255u).toUByte()
                    g = (((g * a) + (_g * (255u - a))) / 255u).toUByte()
                    b = (((b * a) + (_b * (255u - a))) / 255u).toUByte()
                    a = ((a + ((255u - a) * _a)) / 255u).toUByte()
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
                    r = (((r * a) + (_r * (255u - a))) / 255u).toUByte()
                    g = (((g * a) + (_g * (255u - a))) / 255u).toUByte()
                    b = (((b * a) + (_b * (255u - a))) / 255u).toUByte()
                    a = ((a + ((255u - a) * _a)) / 255u).toUByte()
                }
                depthMap[x][y] = sublayers.size
            }

            if(clip.getOpacity() < 1f){
                a = (a.toInt() * clip.getOpacity()).toUInt().toUByte()
            }

            val pixel = Pixel(a,r,g,b)
            val filtered = clip.runThroughFilter(x, y, cache.width, cache.height,pixel)

            buffer[4 * linearIndex] = filtered.a
            buffer[4 * linearIndex + 1] = filtered.r
            buffer[4 * linearIndex + 2] = filtered.g
            buffer[4 * linearIndex + 3] = filtered.b

            onChange(x+position.x,y+position.y,this.depth)
        }
    }

    fun update(t: VideoTimestamp, forceRepaint: Boolean){
        //Do not repaint invisible nodes & their children
        if(!clip.isVisible() && !clip.hasVisibilityChanged()){
            clip.clearRepaintFlags()
            return
        }
        val needsForceRepaint = forceRepaint || (clip.isVisible() && clip.hasVisibilityChanged())

        if(!clip.needsRepaint() && !forceRepaint){
            return
        }
        sublayers.forEach{
            it.update(t, needsForceRepaint)
        }
        if(!clip.needsRepaint() && !forceRepaint){
            return
        }

        val cacheData = (cache.raster.dataBuffer as DataBufferInt).data
        val s = clip.getSize()
        val img = BufferedImage(s.x, s.y, TYPE_INT_ARGB)
        if(clip.isVisible()){
            clip.renderContent(img, t)
        }

        val previousSize = Vector2i(cache.width, cache.height)
        val sizeChanged = s.x != cache.width || s.y != cache.height
        if(sizeChanged){
            buffer = UByteArray(4 * s.x * s.y)
        }
        cache = img //Needs to be set before the calculations, since we need the current size for them

        val previousPosition = position
        val p = clip.getPosition()
        position = p
        val positionChanged = !p.equals(previousPosition)

        if(positionChanged || sizeChanged){
            //Old - New = Overhead that needs to be cleared
            val oldRectangle = previousPosition.withSize(previousSize)
            val newRectangle = p.withSize(s)

            val toClear = oldRectangle.sub(newRectangle)
            toClear.forEach{
                for(x in 0 until it.width){
                    for(y in 0 until it.height){
                        onChange(it.x0 + x, it.y0 + y, this.depth)
                    }
                }
            }
        }

        val fullRepaintRequired = clip.hasOpacityChanged() || positionChanged || sizeChanged || clip.hasVisibilityChanged() || forceRepaint
        clip.clearRepaintFlags()

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

    fun getByte(index: Int): UByte{
        return buffer[index]
    }
}
class RenderBuffer(
    val root: VideoClip
) {
    private val size = root.getSize()
    val resultBuffer = ByteArray(size.x * size.y * 3)
    private val rootLayer = LayerBuffer(0, root, this::onPixelChange)

    init{
        rootLayer.init()
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun onPixelChange(x: Int, y: Int, depth: Int){
        val linearIndex = y * size.x + x
        resultBuffer[linearIndex * 3] = rootLayer.getByte(linearIndex*4+1).toByte()
        resultBuffer[linearIndex * 3+1] = rootLayer.getByte(linearIndex*4+2).toByte()
        resultBuffer[linearIndex * 3+2] = rootLayer.getByte(linearIndex*4+3).toByte()
    }

    fun update(t: VideoTimestamp){
        rootLayer.update(t, false)
    }
}

fun main(){
    movie {
        theme{
            fontColor = Color.WHITE
        }
        chapter {
            scene {
                length = 5
                text {
                    text = "Foo"
                }
            }
        }
        chapter{
            scene{
                length = 5
                text{
                    text = "Bar"
                }
            }
        }
    }
}