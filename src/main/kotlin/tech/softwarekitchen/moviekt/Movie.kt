package tech.softwarekitchen.moviekt

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.Clip
import tech.softwarekitchen.moviekt.exception.FFMPEGDidntShutdownException
import tech.softwarekitchen.moviekt.exception.ImageSizeMismatchException
import tech.softwarekitchen.moviekt.exception.VideoIsClosedException
import java.awt.image.BufferedImage
import java.io.OutputStream
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit


/**
 * Movie wrapper class
 * @param name target file name
 * @param length video length in seconds
 * @param fps frames per second
 * @param size width and height of the video
 */
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
        Thread(this::log).start()
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

    private fun log(){
        val startTime = LocalDateTime.now()
        while(framesWritten < numFrames){
            val now = LocalDateTime.now()
            val elapsed = Duration.between(startTime, now)
            val doneRatio = framesWritten.toDouble() / numFrames.toDouble()
            val remaining = when(doneRatio){
                0.0 -> "???"
                else -> (elapsed.toSeconds() * (1.0 - doneRatio) / doneRatio).toInt().toString()
            }

            println("${String.format("%.2f",doneRatio * 100)}% Elapsed ${elapsed.toSeconds()}s Left: ${remaining}s")
            Thread.sleep(5000)
        }
        println("--- Done ---")
    }

    /**
     * Write an image as frame into the video
     * @param image the image
     * @throws ImageSizeMismatchException image size doesn't match video size
     * @throws VideoIsClosedException the video is already closed due to enough frames provided (1 + length\[s\] * fps)
     * @throws FFMPEGDidntShutdownException enough frames were provided, but FFMPEG didn't shutdown itself
     */
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

    /**
     * Render the entire video via callback function
     * @param frameCallback Callback that provides a required frame with parameters (zero based number of Frames: Int, total number of frames in video: Int, frame time: Float)
     */
    fun render(frameCallback: (Int,Int,Float) -> BufferedImage){
        while(framesWritten < numFrames){
            writeFrame(frameCallback(framesWritten,numFrames,framesWritten * (1 / fps.toFloat())))
        }
    }

    /**
     * Render the entire video via root clip
     * @param rootFrame the video's root element
     * @throws ImageSizeMismatchException clip size doesn't match video size
     */
    @Throws(ImageSizeMismatchException::class)
    fun render(rootFrame: Clip){
        if(rootFrame.size.x != size.x || rootFrame.size.y != size.y){
            throw ImageSizeMismatchException()
        }
        render(rootFrame::render)
    }
}
