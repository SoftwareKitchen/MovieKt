package tech.softwarekitchen.moviekt.clips.video.basic

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.clips.video.VideoTimestamp
import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.floor

class FileVideoClip(id: String, size: Vector2i, position: Vector2i, private val f: File, private val videoSize: Vector2i, private val offset: Double = 0.0, private val videoOffset: Vector2i = Vector2i(0,0)): VideoClip(id, size, position, true, volatile = true) {

    private var data: ByteArray
    private val bufferLength = 3
    private var bufferStart = 0

    init{
        if(!f.exists() || f.isDirectory){
            throw Exception()
        }

        data = loadBuffer(0)
    }

    private fun loadBuffer(start: Int): ByteArray{
        val process = ProcessBuilder(
            "ffmpeg",
            "-ss",
            "$start",
            "-t",
            "$bufferLength",
            "-i",
            f.absolutePath,
            "-f",
            "rawvideo",
            "-pix_fmt",
            "rgb24",
            "-"
        ).start()

        val stream = process.inputStream
        data = stream.readAllBytes()
        bufferStart = start
        return data
    }

    fun getAt(t: Double): BufferedImage {
        if((t - offset) < bufferStart || (t - offset) >= bufferStart + bufferLength){
            loadBuffer(floor((t - offset) - (t - offset) % bufferLength).toInt())
        }
        val baseIndex = 3 * videoSize.x * videoSize.y *((t - offset - bufferStart) * 30).toInt()
        val img = BufferedImage(getSize().x, getSize().y, BufferedImage.TYPE_INT_ARGB)
        if(baseIndex < 0 || baseIndex > data.size -1){
            println("!!!")
            return img
        }

        for(y in 0 until getSize().y){
            for(x in 0 until getSize().x){
                val index = baseIndex + ((y + videoOffset.y) * videoSize.x + x + videoOffset.x) * 3
                val rgb = 255u * 65536u * 256u + data[index].toUInt() * 65536u + data[index+1].toUInt() * 256u + data[index+2].toUInt()
                img.setRGB(x,y, rgb.toInt())
            }
        }

        return img
    }

    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val g = img.createGraphics()
        val content = getAt(t.t)
        g.drawImage(content,0,0,null)
    }
}
