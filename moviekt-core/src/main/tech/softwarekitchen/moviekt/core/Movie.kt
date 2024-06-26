package tech.softwarekitchen.moviekt.core

import org.slf4j.LoggerFactory
import tech.softwarekitchen.moviekt.core.audio.AudioClip
import tech.softwarekitchen.moviekt.core.exception.FFMPEGDidntShutdownException
import tech.softwarekitchen.moviekt.core.exception.ImageSizeMismatchException
import tech.softwarekitchen.moviekt.core.exception.NodeNotFoundException
import tech.softwarekitchen.moviekt.core.exception.VideoIsClosedException
import tech.softwarekitchen.moviekt.core.extension.MovieKtExtension
import tech.softwarekitchen.moviekt.core.render.RenderBuffer
import tech.softwarekitchen.moviekt.core.util.VideoTimestamp
import tech.softwarekitchen.moviekt.core.video.VideoClip
import tech.softwarekitchen.moviekt.theme.VideoTheme
import java.io.File
import java.io.OutputStream
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

private data class ActiveMutation(val node: VideoClip, val uuid: String, val start: Float, val duration: Float)

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
    private val videoRoot: VideoClip,
    private val audioRoot: AudioClip,
    private val extensions: List<MovieKtExtension> = listOf()
) {
    private lateinit var videoStart: LocalDateTime
    private val numVideoFrames = 1 + length * fps
    private var videoFramesWritten = 0
    private lateinit var audioStart: LocalDateTime
    private var audioFramesWritten = 0
    private val numAudioFrames = 44100 * length + 1
    private var mergeDone = false
    private val frameCallbacks = ArrayList<RenderCallback>()
    private val onceCallbacks = ArrayList<OnceCallback>()
    private var theme: VideoTheme? = null
    private val logger = LoggerFactory.getLogger(javaClass)
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

    /**
     * Write an image as frame into the video
     * @param image the image
     * @throws ImageSizeMismatchException image size doesn't match video size
     * @throws VideoIsClosedException the video is already closed due to enough frames provided (1 + length\[s\] * fps)
     * @throws FFMPEGDidntShutdownException enough frames were provided, but FFMPEG didn't shutdown itself
     */
    @Throws(ImageSizeMismatchException::class, VideoIsClosedException::class, FFMPEGDidntShutdownException::class)
    fun writeFrame(target: OutputStream, image: ByteArray){
        if(videoFramesWritten >= numVideoFrames){
            throw VideoIsClosedException()
        }
        target.write(image)
        videoFramesWritten++
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

    fun addCallback(timing: RenderCallbackTiming, action: (Int, Int, Float) -> Unit): RenderCallback {
        val cb = RenderCallback(action, timing)
        frameCallbacks.add(cb)
        return cb
    }

    data class OnceCallback(val action: () -> Unit, val at: Float)
    fun addOnceCallback(action: () -> Unit, at: Float){
        onceCallbacks.add(OnceCallback(action, at))
    }

    fun <T>readRoot(op: (VideoClip) -> T): T{
        return op(videoRoot)
    }

    fun onRoot(op: (VideoClip) -> Unit){
        op(videoRoot)
    }

    fun write(){
        videoStart = LocalDateTime.now()
        Thread(this::log).start()

        //Prepare
        logger.info("Starting video preparations")
        theme?.let{
            logger.info("Applying theme")
            videoRoot.applyTheme(it)
        }
        theme ?: run {
            logger.info("No theme provided")
        }
        extensions.forEach{
            it.prepare(this)
        }

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

        val activeMutations = ArrayList<ActiveMutation>()


        val renderBuffer = RenderBuffer(videoRoot)

        while (videoFramesWritten < numVideoFrames) {
            val t = videoFramesWritten / fps.toFloat()

            val toExecute = onceCallbacks.filter { it.at <= t }.toSet()
            toExecute.forEach { it.action() }
            onceCallbacks.removeAll(toExecute)

            //Remove expired
            val expired = activeMutations.filter { t > it.start + it.duration }
            activeMutations.removeAll(expired)

            expired.forEach {
                it.node.removeMutation(it.uuid)
            }

            //Run active
            activeMutations.forEach {
                val kf = (t - it.start) / it.duration
                it.node.applyKeyframe(it.uuid, kf)
            }


            activeMutations.removeAll(expired)

            extensions.forEach{
                it.frame(t)
            }

            frameCallbacks.forEach { it.execute(RenderCallbackTiming.Pre, videoFramesWritten, numVideoFrames, t) }

            renderBuffer.update(VideoTimestamp(t.toDouble(), videoFramesWritten, numVideoFrames))
            writeFrame(videoOutputStream, renderBuffer.resultBuffer)

            frameCallbacks.forEach { it.execute(RenderCallbackTiming.Post, videoFramesWritten, numVideoFrames, t) }
        }


        videoOutputStream.flush()
        videoOutputStream.close()

        if(!videoProcess.waitFor(5, TimeUnit.SECONDS)){
            throw FFMPEGDidntShutdownException()
        }

        audioStart = LocalDateTime.now()

        when(audioRoot.numChannels){
            1 -> {
                val audioProcess = ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-f", "u16be",
                    "-t", "$length",
                    "-i", "pipe:0",
                    "-ar", "44100",
                    "-c:a", "libfdk_aac",
                    "-vbr", "5",
                    rawAudioName
                )
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start()

                val audioOutputStream = audioProcess.outputStream
                while(audioFramesWritten < numAudioFrames){
                    val t = audioFramesWritten / 44100.0
                    val v = audioRoot.getAt(t)
                    val ampTranslated = v.map{(32767.0 * (it + 1)).toInt()}

                    audioOutputStream.write((ampTranslated[0] / 256) % 256)
                    audioOutputStream.write(ampTranslated[0] % 256)
                    audioFramesWritten++
                }

                audioOutputStream.flush()
                audioOutputStream.close()

                if(!audioProcess.waitFor(5, TimeUnit.SECONDS)){
                    throw FFMPEGDidntShutdownException()
                }
            }
            2 -> {
                val rawLeft = rawAudioName.replace(".m4a", "-left.m4a")
                val rawRight = rawAudioName.replace(".m4a", "-right.m4a")
                val audioProcessLeft = ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-f", "u16be",
                    "-t", "$length",
                    "-i", "pipe:0",
                    "-ar", "44100",
                    "-c:a", "libfdk_aac",
                    "-vbr", "5",
                    rawLeft
                )
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start()
                val audioProcessRight = ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-f", "u16be",
                    "-t", "$length",
                    "-i", "pipe:0",
                    "-ar", "44100",
                    "-c:a", "libfdk_aac",
                    "-vbr", "5",
                    rawRight
                )
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start()

                val audioOutputStreamLeft = audioProcessLeft.outputStream
                val audioOutputStreamRight = audioProcessRight.outputStream
                while(audioFramesWritten < numAudioFrames){
                    val t = audioFramesWritten / 44100.0
                    val v = audioRoot.getAt(t)
                    val ampTranslated = v.map{(32767.0 * it + 1).toInt()}

                    audioOutputStreamLeft.write((ampTranslated[0] / 256) % 256)
                    audioOutputStreamLeft.write(ampTranslated[0] % 256)
                    audioOutputStreamRight.write((ampTranslated[1] / 256) % 256)
                    audioOutputStreamRight.write(ampTranslated[1] % 256)
                    audioFramesWritten++
                }

                audioOutputStreamLeft.flush()
                audioOutputStreamLeft.close()
                audioOutputStreamRight.flush()
                audioOutputStreamRight.close()

                if(!audioProcessLeft.waitFor(5, TimeUnit.SECONDS)){
                    throw FFMPEGDidntShutdownException()
                }
                if(!audioProcessRight.waitFor(5, TimeUnit.SECONDS)){
                    throw FFMPEGDidntShutdownException()
                }

                //Merge audio
                val audioProcessMerge = ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-i", rawLeft,
                    "-i", rawRight,
                    "-filter_complex", "[0:a][1:a]join=inputs=2:channel_layout=stereo[a]",
                    "-map", "[a]",
                    rawAudioName
                )
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start()
                if(!audioProcessMerge.waitFor(30, TimeUnit.SECONDS)){
                    throw FFMPEGDidntShutdownException()
                }
                //File(rawLeft).delete()
                //File(rawRight).delete()

            }
            else -> throw Exception("Currently only 1 (mono) or 2 (stereo) channels are supported")
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

    fun setTheme(theme: VideoTheme){
        this.theme = theme
    }
}
