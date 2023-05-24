package tech.softwarekitchen.moviekt

import tech.softwarekitchen.moviekt.animation.MovieKtAnimation
import tech.softwarekitchen.moviekt.clips.audio.AudioContainerClip
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.exception.FFMPEGDidntShutdownException
import tech.softwarekitchen.moviekt.exception.ImageSizeMismatchException
import tech.softwarekitchen.moviekt.exception.VideoIsClosedException
import java.awt.image.BufferedImage
import java.io.File
import java.io.OutputStream
import java.nio.IntBuffer
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
    private lateinit var videoStart: LocalDateTime
    private val numVideoFrames = 1 + length * fps
    private var videoFramesWritten = 0
    private lateinit var audioStart: LocalDateTime
    private var audioFramesWritten = 0
    private val numAudioFrames = 44100 * length + 1
    private val audioContainer = AudioContainerClip(length.toDouble())
    private var mergeDone = false
    private val frameCallbacks = ArrayList<RenderCallback>()
    private val onceCallbacks = ArrayList<OnceCallback>()
    private val animations = ArrayList<MovieKtAnimation<*>>()
    private fun log(){
        while(videoFramesWritten < numVideoFrames){
            if(this::videoStart.isInitialized) {
                val now = LocalDateTime.now()
                val elapsed = Duration.between(videoStart, now)
                val doneRatio = videoFramesWritten.toDouble() / numVideoFrames.toDouble()
                val remaining = when (doneRatio) {
                    0.0 -> "???"
                    else -> (elapsed.toSeconds() * (1.0 - doneRatio) / doneRatio).toInt().toString()
                }

                println(
                    "[1/3] Video ${
                        String.format(
                            "%.2f",
                            doneRatio * 100
                        )
                    }% Elapsed ${elapsed.toSeconds()}s Left: ${remaining}s"
                )
            }else{
                println("Waiting for FFMPEG to start up")
            }
            Thread.sleep(3000)
        }

        while(audioFramesWritten < numAudioFrames){
            if(this::audioStart.isInitialized) {
                val now = LocalDateTime.now()
                val elapsed = Duration.between(audioStart, now)
                val doneRatio = audioFramesWritten.toDouble() / numAudioFrames.toDouble()
                val remaining = when (doneRatio) {
                    0.0 -> "???"
                    else -> (elapsed.toSeconds() * (1.0 - doneRatio) / doneRatio).toInt().toString()
                }

                println(
                    "[2/3] Audio ${
                        String.format(
                            "%.2f",
                            doneRatio * 100
                        )
                    }% Elapsed ${elapsed.toSeconds()}s Left: ${remaining}s"
                )
            }else{
                println("Waiting for FFMPEG to start up")
            }
            Thread.sleep(3000)
        }

        while(!mergeDone){
            println("[3/3] Merge - Waiting")
            Thread.sleep(3000)
        }

        println("--- Done ---")
    }

    private val buffer = IntArray(videoRoot.getSize().x * videoRoot.getSize().y)
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
        if(videoFramesWritten >= numVideoFrames){
            throw VideoIsClosedException()
        }

        image.getRGB(0, 0, image.width, image.height, buffer, 0, image.width)
        sendFrame(target)
    }

    fun repeatFrame(target: OutputStream){
        sendFrame(target)
    }

    private fun sendFrame(target: OutputStream){
        buffer.forEach { ival ->
            val uival = ival.toUInt()
            target.write(((uival / 65536u) % 256u).toInt())
            target.write(((uival / 256u) % 256u).toInt())
            target.write((uival  % 256u).toInt())
        }

        videoFramesWritten++
    }

    fun getAudioContainer(): AudioContainerClip{
        return audioContainer
    }

    enum class RenderCallbackTiming{
        Pre, Post
    }
    class RenderCallback(private val action: (Int, Int, Float) -> Unit, private val timing: RenderCallbackTiming, private var isActive: Boolean = true){
        fun suspend(){
            isActive = false
        }
        fun resume(){
            isActive = true
        }

        fun execute(timing: RenderCallbackTiming, frameNo: Int, frameCt: Int, t: Float){
            if(isActive && timing == this.timing){
                action(frameNo, frameCt, t)
            }
        }
    }

    fun addCallback(timing: RenderCallbackTiming, action: (Int, Int, Float) -> Unit): RenderCallback{
        val cb = RenderCallback(action, timing)
        frameCallbacks.add(cb)
        return cb
    }

    data class OnceCallback(val action: () -> Unit, val at: Float)
    fun addOnceCallback(action: () -> Unit, at: Float){
        onceCallbacks.add(OnceCallback(action, at))
    }

    fun write(){
        videoStart = LocalDateTime.now()
        Thread(this::log).start()

        val rawVideoName = name+"_temp.mp4"
        val rawAudioName = name+"_temp.m4a"

        val videoProcess = ProcessBuilder("ffmpeg"
            ,"-y"
            ,"-f","rawvideo"
            ,"-t","$length"
            ,"-pix_fmt","rgb24"
            ,"-s","${videoRoot.getSize().x}x${videoRoot.getSize().y}",
            "-r","$fps"
            ,"-i","pipe:0"
            ,"-c:v","libx264"
            ,"-profile:v","high444"
            ,"-level:v","3"
            ,"-crf","17"
            ,"-preset","veryslow"
            ,"-an",rawVideoName
        )
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .start()

        val videoOutputStream = videoProcess.outputStream

        var mappedAnimations = animations.associateWith{
            videoRoot.findById(it.nodeId)
        }

        while(videoFramesWritten < numVideoFrames){
            val t = videoFramesWritten / fps.toFloat()

            val toExecute = onceCallbacks.filter{it.at <= t}.toSet()
            toExecute.forEach{it.action()}
            onceCallbacks.removeAll(toExecute)

            mappedAnimations.forEach{
                animData ->
                if(animData.key.isApplicable(t)){
                    animData.value.forEach{
                        target ->
                        target.set(animData.key.property, animData.key.get(t))
                    }
                }
            }

            mappedAnimations = mappedAnimations.filterKeys {
                !it.isFinished(t)
            }

            frameCallbacks.forEach{it.execute(RenderCallbackTiming.Pre, videoFramesWritten, numVideoFrames, t)}

            if(videoRoot.needsRepaint()){
                writeFrame(videoOutputStream, videoRoot.get())
            }else{
                repeatFrame(videoOutputStream)
            }

            frameCallbacks.forEach{it.execute(RenderCallbackTiming.Post, videoFramesWritten, numVideoFrames, t)}
        }

        videoOutputStream.flush()
        videoOutputStream.close()

        if(!videoProcess.waitFor(5, TimeUnit.SECONDS)){
            throw FFMPEGDidntShutdownException()
        }

        audioStart = LocalDateTime.now()
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
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .start()

        val audioOutputStream = audioProcess.outputStream
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
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .start()
        mergeProcess.waitFor()
        mergeDone = true
        File(rawAudioName).delete()
        File(rawVideoName).delete()
    }

    fun addAnimation(anim: MovieKtAnimation<*>){
        animations.add(anim)
    }
}
