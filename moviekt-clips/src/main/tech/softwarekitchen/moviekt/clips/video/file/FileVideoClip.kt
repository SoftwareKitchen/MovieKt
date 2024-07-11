package tech.softwarekitchen.moviekt.clips.video.file

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.core.util.VideoTimestamp
import tech.softwarekitchen.moviekt.core.video.VideoClip
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream

open class FileVideoClip(id: String, size: Vector2i, position: Vector2i, private val f: File, private val videoSize: Vector2i, private val offset: Double = 0.0, private val videoOffset: Vector2i = Vector2i(
    0,
    0
)
): VideoClip(id, size, position, true, volatile = true) {
    companion object {
        val PropertyKey_VideoTimeMapping = "VideoTimeMapping"
    }
    val pixelsPerFrame = videoSize.x * videoSize.y
    val bytesPerFrame = 3 * pixelsPerFrame
    val currentFrameData = ByteArray(bytesPerFrame)
    private var currentFrame = -1
    private val fps: Int
    private val videoStream: InputStream
    private var outOfData = false
    val duration: Double
    private val durationRegex: Regex

    private val mapperProperty =
        VideoClipProperty<(Double) -> Double>(PropertyKey_VideoTimeMapping, { it }, this::markDirty)

    init{
        durationRegex = "^(\\d+):(\\d+):(\\d+)\\.(\\d+)$".toRegex()

        registerProperty(mapperProperty)

        if(!f.exists() || f.isDirectory){
            throw Exception()
        }

        val probe = ProcessBuilder("ffprobe", f.absolutePath)
        val pp = probe.start()
        val lines = pp.errorReader().readLines()
        val r = pp.waitFor()
        if(r != 0){
            throw Exception("FFProbe non-zero return code $r")
            pp.errorReader().lines().forEach{
                println(it)
            }
        }
        val relevantLine = lines.first{ it.contains("Stream #0:0: Video") }
        val fpsPart = relevantLine.split(",").map{it.trim()}.first{it.endsWith("fps")}
        fps = fpsPart.replace("fps", "").trim().toInt()
        println("Video FPS $fps")
        val durationLine = lines.first{ it.trim().startsWith("Duration:")}
        val durationPart = durationLine.split(",")[0].replace("Duration:", "").trim()
        duration = parseFfprobeDuration(durationPart)

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

        val process = pb.start()
        videoStream = process.inputStream
        Thread{
            process.waitFor()
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
            loadFrame()
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

        try{
            videoStream.readNBytes(currentFrameData, 0, bytesPerFrame)
        }catch(ex: Exception){
            println("WARN No frame data, video might have ended")
            videoStream.close()
            outOfData = true

        }
    }

    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val mapper = mapperProperty.v
        val effT = mapper(t.t)

        val g = img.createGraphics()
        val content = getFrameAt(effT)
        g.drawImage(content,0,0,null)
    }
    private fun parseFfprobeDuration(duration: String): Double{
        println("Read video duration $duration")
        val match = durationRegex.matchEntire(duration) ?: throw Exception()
        if(match.groups.size != 5){ throw Exception() }
        val hours = match.groups[1]!!.value.toInt()
        val minutes = match.groups[2]!!.value.toInt()
        val seconds = match.groups[3]!!.value.toInt()
        val nano = match.groups[4]!!.value.toInt()
        return hours * 3600.0 + minutes * 60.0 + seconds * 1.0 + nano * 1e-9
    }
}