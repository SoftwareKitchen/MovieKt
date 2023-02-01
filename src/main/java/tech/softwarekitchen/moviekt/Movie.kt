package tech.softwarekitchen.moviekt

import tech.softwarekitchen.moviekt.clips.Clip
import tech.softwarekitchen.moviekt.exception.FFMPEGDidntShutdownException
import tech.softwarekitchen.moviekt.exception.ImageSizeMismatchException
import tech.softwarekitchen.moviekt.exception.VideoIsClosedException
import java.awt.image.BufferedImage
import java.io.OutputStream
import java.util.concurrent.TimeUnit

data class Vector2i(val x: Int, val y: Int){
    fun plus(other: Vector2i): Vector2i{
        return Vector2i(x+other.x, y+other.y)
    }
}

class Movie(
    private val name: String,
    private val length: Int,
    private val fps: Int,
    private val size: Vector2i
) {
    private val numFrames = 1 + length * fps
    private val process: Process
    private var framesWritten = 0
    private val outputStream: OutputStream

    init{
        process = ProcessBuilder("ffmpeg"
            ,"-y"
            ,"-f","rawvideo"
            ,"-t","$length"
            ,"-pix_fmt","rgb24"
            ,"-s","${size.x}x${size.y}",
            "-r","$fps"
            ,"-i","pipe:0"
            ,"-c:v","libx264"
            ,"-profile:v","high444"
            ,"-level:v","3"
            ,"-crf","17"
            ,"-preset","veryslow"
            ,"-an",name
        )
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .start()
        outputStream = process.outputStream
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @Throws(ImageSizeMismatchException::class, VideoIsClosedException::class, FFMPEGDidntShutdownException::class)
    fun writeFrame(image: BufferedImage){
        if(image.width != size.x || image.height != size.y){
            throw ImageSizeMismatchException()
        }

        if(framesWritten >= numFrames){
            throw VideoIsClosedException()
        }

        val buffer = IntArray(size.x*size.y)
        image.getRGB(0, 0, size.x, size.y, buffer, 0, size.x)

        buffer.toUIntArray().forEach { ival ->
            outputStream.write(((ival / 65536u) % 256u).toInt())
            outputStream.write(((ival / 256u) % 256u).toInt())
            outputStream.write((ival  % 256u).toInt())
        }

        framesWritten++
        if(framesWritten == numFrames){
            outputStream.flush()
            outputStream.close()

            if(!process.waitFor(5, TimeUnit.SECONDS)){
                throw FFMPEGDidntShutdownException()
            }
        }
    }

    fun render(frameCallback: (Int,Int,Float) -> BufferedImage){
        while(framesWritten < numFrames){
            writeFrame(frameCallback(framesWritten,numFrames,framesWritten * (1 / fps.toFloat())))
        }
    }

    @Throws(ImageSizeMismatchException::class)
    fun render(rootFrame: Clip){
        if(rootFrame.size.x != size.x || rootFrame.size.y != size.y){
            throw ImageSizeMismatchException()
        }
        render(rootFrame::render)
    }
}
