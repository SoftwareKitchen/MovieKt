package tech.softwarekitchen.moviekt.examples

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.Movie
import tech.softwarekitchen.moviekt.animation.discrete.RepeatingIteratorAnimation
import tech.softwarekitchen.moviekt.animation.position.toStaticSizeProvider
import tech.softwarekitchen.moviekt.clips.audio.FileAudioClip
import tech.softwarekitchen.moviekt.clips.video.image.ImageSlideshowVideoClip
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>){
    val fps = 25

    // Create image files
    val images = (0 until 1000).map{String.format("/some/path/image%04d.png",it)}
    val files = images.map(::File)

    // Create slideshow clip
    val rootClip = ImageSlideshowVideoClip(
        //ID
        "Root",
        // Video size
        Vector2i(1920,1080),
        Vector2i(0,0),
        // Images
        files,
        // Time per image
        1 / fps.toFloat()
    )

    val movie = Movie(
        // file name, .mp4 will be appended automatically
        "myvideo",
        // video length in seconds
        10,
        // frames per second
        fps,
        // root clip - we only have this one
        rootClip
    )

    // If you want audio:
    val audioContainer = movie.getAudioContainer()
    val audio = FileAudioClip("/path/to/some/music")
    audioContainer.addClip(audio)

    //Add Image slider
    movie.addAnimation(
        RepeatingIteratorAnimation(
            "Root",
            ImageSlideshowVideoClip.PropertyKey_ImageIndex,
            0.5f,
            files.size
        )
    )

    // Run rendering
    movie.write()
}
