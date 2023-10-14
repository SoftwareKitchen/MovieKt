package tech.softwarekitchen.moviekt.dsl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.once.SetOnceAnimation
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.clips.video.basic.ContainerVideoClip
import tech.softwarekitchen.moviekt.clips.video.text.StaticTextVideoClipConfiguration
import tech.softwarekitchen.moviekt.clips.video.text.TextAnchor
import tech.softwarekitchen.moviekt.clips.video.text.TextVideoClip
import tech.softwarekitchen.moviekt.clips.video.util.FULLHD
import tech.softwarekitchen.moviekt.core.Movie
import tech.softwarekitchen.moviekt.layout.impl.CenterLayout
import tech.softwarekitchen.moviekt.layout.impl.VerticalLayout
import tech.softwarekitchen.moviekt.layout.impl.VerticalLayoutConfiguration
import tech.softwarekitchen.moviekt.theme.VideoTheme
import tech.softwarekitchen.moviekt.theme.VideoTheme.Companion.VTPropertyKey_FontColor
import tech.softwarekitchen.moviekt.theme.VideoTheme.Companion.VTPropertyKey_FontSize
import java.awt.Color
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

data class DslVideoConfiguration(
    var name: String = "Unnamed video",
    var fps: Int = 25,
    var size: Vector2i = FULLHD,
    val chapters: MutableList<DslChapterConfiguration> = ArrayList<DslChapterConfiguration>(),
    var theme: VideoTheme? = null
)

fun movie(conf: DslVideoConfiguration.() -> Unit) {
    val logger = LoggerFactory.getLogger("MovieKt-DSL-Movie")
    val c = DslVideoConfiguration()
    c.conf()

    logger.info("Creating video ${c.name}")

    val rootClip = ContainerVideoClip("_", c.size, Vector2i(0,0),true)

    val videoLength = c.chapters.sumOf{ it.scenes.sumOf{ it.length }}

    val movie = Movie(
        c.name,
        videoLength,
        c.fps,
        rootClip
    )

    c.theme?.let{movie.setTheme(it)}

    var currentOffset = 0
    logger.info("Preparing Video with ${c.chapters.size} chapters")
    c.chapters.forEach{
        logger.info("Preparing Chapter '${it.name}' with ${it.scenes.size} scenes")
        val chapterLength = it.scenes.sumOf{it.length}
        val id = UUID.randomUUID().toString()
        val chapterRoot = ContainerVideoClip(id, c.size, Vector2i(0,0), false)
        movie.addAnimation(SetOnceAnimation(id, VideoClip.PropertyKey_Visible, currentOffset.toFloat(), true))
        movie.addAnimation(SetOnceAnimation(id, VideoClip.PropertyKey_Visible, (currentOffset + chapterLength).toFloat(), false))
        rootClip.addChild(chapterRoot)

        var currentSceneOffset = 0
        it.scenes.forEach{
            logger.info("Preparing Scene '${it.name}' with ${it.getClips().size} clips")
            val sid = UUID.randomUUID().toString()
            val sceneRoot = ContainerVideoClip(sid, c.size, Vector2i(0,0), false)
            movie.addAnimation(SetOnceAnimation(sid, VideoClip.PropertyKey_Visible, (currentOffset + currentSceneOffset).toFloat(), true))
            movie.addAnimation(SetOnceAnimation(sid, VideoClip.PropertyKey_Visible, (currentOffset + currentSceneOffset + chapterLength).toFloat(), false))
            chapterRoot.addChild(sceneRoot)

            it.getClips().forEach(sceneRoot::addChild)

            currentSceneOffset += it.length
        }
        currentOffset += chapterLength
    }

    movie.write()
}

data class DslChapterConfiguration(
    var name: String = "Unnamed chapter",
    val scenes: MutableList<DslSceneConfiguration> = ArrayList<DslSceneConfiguration>()
)

fun DslVideoConfiguration.chapter(conf: DslChapterConfiguration.() -> Unit){
    val c = DslChapterConfiguration()
    c.conf()
    chapters.add(c)
}

abstract class DslClipContainer{
    private val children = ArrayList<VideoClip>()
    fun addClip(clip: VideoClip){
        children.add(clip)
    }
    fun getClips(): List<VideoClip>{
        return children.toList()
    }
}

class DslSceneConfiguration(
    var name: String = "Unnamed scene",
    var length: Int = 3
): DslClipContainer()

fun DslChapterConfiguration.scene(conf: DslSceneConfiguration.() -> Unit){
    val c = DslSceneConfiguration()
    c.conf()

    scenes.add(c)
}

data class DslThemeEntry(val key: String, val value: Any)

class DslTheme{
    private val items = ArrayList<DslThemeEntry>()

    fun getItems(): List<DslThemeEntry>{
        return items.toList()
    }

    var fontColor: Color
        get() = items.last{it.key == VTPropertyKey_FontColor}.value as Color
        set(v){
            items.add(DslThemeEntry(VTPropertyKey_FontColor, v))
        }
    var fontSize: Int
        get() = items.last{it.key == VTPropertyKey_FontSize}.value as Int
        set(v){
            items.add(DslThemeEntry(VTPropertyKey_FontSize, v))
        }
}

fun DslVideoConfiguration.theme(conf: DslTheme.() -> Unit){
    val c = DslTheme()
    c.conf()
    val theme = VideoTheme()
    c.getItems().forEach{
        theme.set(it.key, it.value)
    }
    this.theme = theme
}

data class DslTextVideoClipConfiguration(
    var size: Vector2i = Vector2i(100,20),
    var position: Vector2i = Vector2i(0,0),
    var visible: Boolean = true,
    var text: String = "Some text",
    var fontSize: Int = 24,
    var color: Color = Color.BLACK,
    var font: File? = null,
    var anchor: TextAnchor = TextAnchor.Left
)

fun DslClipContainer.text(conf: DslTextVideoClipConfiguration.() -> Unit){
    val c = DslTextVideoClipConfiguration()
    c.conf()
    val clip = TextVideoClip(
        UUID.randomUUID().toString(),
        c.size,
        c.position,
        c.visible,
        StaticTextVideoClipConfiguration(
            c.text,
            c.fontSize,
            c.color,
            c.font,
            c.anchor
        )
    )
    addClip(clip)
}


class DslCenterLayoutConfiguration: DslClipContainer()

fun DslClipContainer.centered(conf: DslCenterLayoutConfiguration.() -> Unit){
    val c = DslCenterLayoutConfiguration()
    c.conf()
    val centerLayout = CenterLayout()
    c.getClips().forEach(centerLayout::addChild)
    addClip(centerLayout)
}

class DslVerticalLayoutConfiguration: DslClipContainer()

fun DslClipContainer.vertical(conf: DslVerticalLayoutConfiguration.() -> Unit){
    val c = DslVerticalLayoutConfiguration()
    c.conf()
    val verticalLayout = VerticalLayout()
    c.getClips().forEach(verticalLayout::addChild)
    addClip(verticalLayout)
}
