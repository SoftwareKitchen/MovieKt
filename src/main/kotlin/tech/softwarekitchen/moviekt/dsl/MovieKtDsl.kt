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
import tech.softwarekitchen.moviekt.layout.impl.OverlayLayout
import tech.softwarekitchen.moviekt.layout.impl.VerticalLayout
import tech.softwarekitchen.moviekt.layout.impl.VerticalLayoutConfiguration
import tech.softwarekitchen.moviekt.theme.VideoTheme
import tech.softwarekitchen.moviekt.theme.VideoTheme.Companion.VTPropertyKey_FontColor
import tech.softwarekitchen.moviekt.theme.VideoTheme.Companion.VTPropertyKey_FontSize
import java.awt.Color
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

interface DslChapterContainer{
    val chapters: MutableList<DslChapterConfiguration>
    val logger: Logger
}

class DslVideoConfiguration(
    override val logger: Logger,
    var name: String = "Unnamed video",
    var fps: Int = 25,
    var size: Vector2i = FULLHD,
    override val chapters: MutableList<DslChapterConfiguration> = ArrayList<DslChapterConfiguration>(),
    var theme: VideoTheme? = null
): DslChapterContainer

fun movie(conf: DslVideoConfiguration.() -> Unit){
    val logger = LoggerFactory.getLogger("MovieKt-DSL-Movie")
    val c = DslVideoConfiguration(logger)
    c.conf()

    logger.info("Creating video ${c.name}")

    val rootClip = ContainerVideoClip("_", c.size, Vector2i(0,0),true)

    c.prepareChapters(rootClip)

    val videoLength = c.chapters.sumOf{ it.scenes.sumOf{ it.length }}

    val movie = Movie(
        c.name,
        videoLength,
        c.fps,
        rootClip
    )

    c.theme?.let{movie.setTheme(it)}


    movie.write()
}

fun DslChapterContainer.prepareChapters(target: VideoClip){
    var currentOffset = 0
    logger.info("Preparing Video with ${chapters.size} chapters")
    chapters.forEach{
        logger.info("Preparing Chapter '${it.name}' with ${it.scenes.size} scenes")
        val chapterLength = it.scenes.sumOf{it.length}
        val id = UUID.randomUUID().toString()
        val chapterRoot = ContainerVideoClip(id, target.getSize(), Vector2i(0,0), false, currentOffset.toFloat())
        chapterRoot.addRawAnimation(SetOnceAnimation(id, VideoClip.PropertyKey_Visible, 0f, true))
        chapterRoot.addRawAnimation(SetOnceAnimation(id, VideoClip.PropertyKey_Visible, chapterLength.toFloat(), false))
        target.addChild(chapterRoot)


        val overlay = OverlayLayout()
        chapterRoot.addChild(overlay)
        var currentSceneOffset = 0
        it.scenes.forEach{
            logger.info("Preparing Scene '${it.name}' with ${it.getClips().size} clips")
            val sid = UUID.randomUUID().toString()
            val sceneRoot = ContainerVideoClip(sid, target.getSize(), Vector2i(0,0), false, currentSceneOffset.toFloat())
            sceneRoot.addRawAnimation(SetOnceAnimation(sid, VideoClip.PropertyKey_Visible, 0f, true))
            sceneRoot.addRawAnimation(SetOnceAnimation(sid, VideoClip.PropertyKey_Visible, it.length.toFloat(), false))
            overlay.addChild(sceneRoot)

            it.getClips().forEach(sceneRoot::addChild)

            currentSceneOffset += it.length
        }
        currentOffset += chapterLength
    }
}

data class DslChapterConfiguration(
    var name: String = "Unnamed chapter",
    val scenes: MutableList<DslSceneConfiguration> = ArrayList<DslSceneConfiguration>()
)

fun DslChapterContainer.chapter(conf: DslChapterConfiguration.() -> Unit){
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
