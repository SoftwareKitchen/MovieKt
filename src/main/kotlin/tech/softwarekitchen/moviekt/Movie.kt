package tech.softwarekitchen.moviekt

import tech.softwarekitchen.moviekt.clips.audio.AudioClip
import tech.softwarekitchen.moviekt.clips.audio.AudioContainerClip
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.exception.FFMPEGDidntShutdownException
import tech.softwarekitchen.moviekt.exception.ImageSizeMismatchException
import tech.softwarekitchen.moviekt.exception.VideoIsClosedException
import java.awt.image.BufferedImage
import java.io.File
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
    private val videoRoot: VideoClip
) {
    private val numFrames = 1 + length * fps
    private var framesWritten = 0
    private val audioContainer = AudioContainerClip(length.toDouble())

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
    fun writeFrame(target: OutputStream, image: BufferedImage){

        if(framesWritten >= numFrames){
            throw VideoIsClosedException()
        }

        val buffer = IntArray(image.width * image.height)
        image.getRGB(0, 0, image.width, image.height, buffer, 0, image.width)

        buffer.toUIntArray().forEach { ival ->
            target.write(((ival / 65536u) % 256u).toInt())
            target.write(((ival / 256u) % 256u).toInt())
            target.write((ival  % 256u).toInt())
        }

        framesWritten++
    }

    fun getAudioContainer(): AudioContainerClip{
        return audioContainer
    }

    fun write(){
        Thread(this::log).start()

        val rawVideoName = name+"_temp.mp4"
        val rawAudioName = name+"_temp.m4a"

        val videoProcess = ProcessBuilder("ffmpeg"
            ,"-y"
            ,"-f","rawvideo"
            ,"-t","$length"
            ,"-pix_fmt","rgb24"
            ,"-s","${videoRoot.size.x}x${videoRoot.size.y}",
            "-r","$fps"
            ,"-i","pipe:0"
            ,"-c:v","libx264"
            ,"-profile:v","high444"
            ,"-level:v","3"
            ,"-crf","17"
            ,"-preset","veryslow"
            ,"-an",rawVideoName
        )
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .start()

        val videoOutputStream = videoProcess.outputStream

        while(framesWritten < numFrames){
            writeFrame(videoOutputStream, videoRoot.render(framesWritten,numFrames,framesWritten.toFloat() / fps))
        }

        videoOutputStream.flush()
        videoOutputStream.close()

        if(!videoProcess.waitFor(5, TimeUnit.SECONDS)){
            throw FFMPEGDidntShutdownException()
        }

        val audioProcess = ProcessBuilder(
            "ffmpeg",
            "-y",
            "-f","u16be",
            "-t","$length",
            "-i","pipe:0",
            "-ar","44100",
            "-c:a","libfdk_aac",
            "-vbr","5",
            rawAudioName
        )
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .start()

        val audioOutputStream = audioProcess.outputStream
        var audioFramesWritten = 0
        val numAudioFrames = 44100 * length + 1
        while(audioFramesWritten < numAudioFrames){
            val t = audioFramesWritten / 44100.0
            val v = audioContainer.getAt(t)
            val ampTranslated = (32767.0 * (v + 1)).toInt()

            audioOutputStream.write((ampTranslated / 256) % 256)
            audioOutputStream.write(ampTranslated % 256)
            audioFramesWritten++
        }

        audioOutputStream.flush()
        audioOutputStream.close()

        if(!audioProcess.waitFor(5, TimeUnit.SECONDS)){
            throw FFMPEGDidntShutdownException()
        }


        val mergeProcess = ProcessBuilder(
            "ffmpeg",
            "-i",
            rawVideoName,
            "-i",
            rawAudioName,
            "-c:v",
            "copy",
            "-map",
            "0:v",
            "-map",
            "1:a",
            "-y",
            "$name.mp4"
        )
            .inheritIO()
            .start()
        mergeProcess.waitFor()
        File(rawAudioName).delete()
        File(rawVideoName).delete()
    }
}
