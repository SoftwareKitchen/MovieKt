package tech.softwarekitchen.moviekt.clips.video.basic

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.clips.video.VideoTimestamp
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.ref.WeakReference
import kotlin.math.floor

class FileVideoClip(id: String, size: Vector2i, position: Vector2i, private val f: File, private val videoSize: Vector2i, private val offset: Double = 0.0, private val videoOffset: Vector2i = Vector2i(0,0)): VideoClip(id, size, position, true, volatile = true) {

    override val logger: Logger = LoggerFactory.getLogger(javaClass)

    private lateinit var currentFrameData: ByteArray
    private var currentFrame = -1
    private val fps = 30 //FIXME
    private val videoStream: InputStream
    private var outOfData = false

    init{
        if(!f.exists() || f.isDirectory){
            throw Exception()
        }

        val pb = ProcessBuilder(
            "ffmpeg",
            "-i",
            f.absolutePath,
            "-f",
            "rawvideo",
            "-pix_fmt",
            "rgb24",
            "-"
        )
        logger.trace("Running ${pb.command().joinToString(" ")}")
        val process = pb.start()
        videoStream = process.inputStream
        Thread{
            process.waitFor()
            logger.debug("FFMPEG process shut down, closing stream")
            videoStream.close()
        }.start()
        loadFrame()
    }

    fun getFrameAt(t: Double): BufferedImage {
        val frame = (t * fps).toInt()
        if(frame < currentFrame){
            throw Exception("Not yet supported")
        }
        while(currentFrame < frame){
            logger.trace("Loading frame $currentFrame < $frame")
            loadFrame()
            logger.trace("Done")
        }

        val img = BufferedImage(getSize().x, getSize().y, BufferedImage.TYPE_INT_ARGB)

        for(y in 0 until getSize().y){
            for(x in 0 until getSize().x){
                val index = ((y + videoOffset.y) * videoSize.x + x + videoOffset.x) * 3
                val rgb = 255u * 65536u * 256u + currentFrameData[index].toUInt() * 65536u + currentFrameData[index+1].toUInt() * 256u + currentFrameData[index+2].toUInt()
                img.setRGB(x,y, rgb.toInt())
            }
        }

        return img
    }

    fun loadFrame(){
        currentFrame++
        if(outOfData){
            return
        }
        val pixelsPerFrame = videoSize.x * videoSize.y

        var data: ByteArray? = null
        val fetcher = Thread{
            data = videoStream.readNBytes(3 * pixelsPerFrame)
        }
        fetcher.start()

        for(i in 0 until 100){
            Thread.sleep(100)
            data?.let{
                currentFrameData = it
                return
            }
        }
        logger.warn("Needed to interrupt FFMPEG frame fetching after 10s")
        videoStream.close()
        fetcher.interrupt()
        outOfData = true
    }

    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val g = img.createGraphics()
        val content = getFrameAt(t.t)
        g.drawImage(content,0,0,null)
    }
}


private fun Long.formatMem(): String{
    return when{
        this < 1024 -> "$this"
        this < 1024 * 1024 -> "${this / 1024}k"
        this < 1024 * 1024 * 1024 -> "${this / (1024 * 1024)}M"
        else -> "${this / (1024 * 1024 * 1024)}G"
    }
}