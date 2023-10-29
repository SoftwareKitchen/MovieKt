package tech.softwarekitchen.moviekt.dsl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.once.SetOnceAnimation
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.clips.video.basic.*
import tech.softwarekitchen.moviekt.clips.video.image.*
import tech.softwarekitchen.moviekt.clips.video.shape.ArrowVideoClip
import tech.softwarekitchen.moviekt.clips.video.shape.ArrowVideoClipConfiguration
import tech.softwarekitchen.moviekt.clips.video.text.*
import tech.softwarekitchen.moviekt.clips.video.util.FULLHD
import tech.softwarekitchen.moviekt.core.Movie
import tech.softwarekitchen.moviekt.layout.impl.*
import tech.softwarekitchen.moviekt.theme.VideoTheme
import tech.softwarekitchen.moviekt.theme.VideoTheme.Companion.VTPropertyKey_BackgroundColor
import tech.softwarekitchen.moviekt.theme.VideoTheme.Companion.VTPropertyKey_BorderColor
import tech.softwarekitchen.moviekt.theme.VideoTheme.Companion.VTPropertyKey_Font
import tech.softwarekitchen.moviekt.theme.VideoTheme.Companion.VTPropertyKey_FontColor
import tech.softwarekitchen.moviekt.theme.VideoTheme.Companion.VTPropertyKey_FontSize
import tech.softwarekitchen.moviekt.util.Padding
import java.awt.Color
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.atan2

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
    var theme: VideoTheme? = null,
    var chapterPreparator: DslVideoConfiguration.(VideoClip) -> Unit = DslVideoConfiguration::prepareChapters
): DslChapterContainer

fun movie(conf: DslVideoConfiguration.() -> Unit){
    val logger = LoggerFactory.getLogger("MovieKt-DSL-Movie")
    val c = DslVideoConfiguration(logger)
    c.conf()

    logger.info("Creating video ${c.name}")

    val rootClip = ContainerVideoClip("_", c.size, Vector2i(0,0),true)

    rootClip.addChild(ColorVideoClip("_", c.size, Vector2i(0,0), true, ColorVideoClipConfiguration(Color.BLACK)))

    val cp = c.chapterPreparator
    c.cp(rootClip)

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

open class DslChapterConfiguration(
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

open class DslSceneConfiguration(
    var name: String = "Unnamed scene",
    var length: Int = 3
): DslClipContainer()

fun DslChapterConfiguration.scene(conf: DslSceneConfiguration.() -> Unit){
    val c = DslSceneConfiguration()
    c.conf()

    scenes.add(c)
}

data class DslThemeEntry(val key: String, val value: Any, val variant: String?)

open class DslTheme{
    protected val items = ArrayList<DslThemeEntry>()

    fun getItems(): List<DslThemeEntry>{
        return items.toList()
    }

    fun addItem(item: DslThemeEntry){
        items.add(item)
    }

    var fontColor: Color
        get() = items.last{it.key == VTPropertyKey_FontColor}.value as Color
        set(v){
            items.add(DslThemeEntry(VTPropertyKey_FontColor, v, null))
        }
    var fontSize: Int
        get() = items.last{it.key == VTPropertyKey_FontSize}.value as Int
        set(v){
            items.add(DslThemeEntry(VTPropertyKey_FontSize, v, null))
        }

    var backgroundColor: Color
        get() = items.last{it.key == VTPropertyKey_BackgroundColor } as Color
        set(v){
            items.add(DslThemeEntry(VTPropertyKey_BackgroundColor, v, null))
        }

    var font: File
        get() = items.last{it.key == VTPropertyKey_Font } as File
        set(v){
            items.add(DslThemeEntry(VTPropertyKey_Font, v, null))
        }

    var borderColor: Color
        get() = items.last{it.key == VTPropertyKey_BorderColor } as Color
        set(v){
            items.add(DslThemeEntry(VTPropertyKey_BorderColor, v, null))
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

data class DslVariantTheme(var name: String = "foo"): DslTheme()

fun DslTheme.variant(conf: DslVariantTheme.() -> Unit){
    val c = DslVariantTheme()
    c.conf()

    c.getItems().forEach{
        addItem(DslThemeEntry(it.key, it.value, c.name))
    }
}

data class DslTextVideoClipConfiguration(
    var size: Vector2i = Vector2i(100,20),
    var position: Vector2i = Vector2i(0,0),
    var visible: Boolean = true,
    var text: String = "Some text",
    var fontSize: Int = 24,
    var color: Color = Color.BLACK,
    var font: File? = null,
    var anchor: TextAnchor = TextAnchor.Left,
    var variant: String? = null
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
    c.variant?.let(clip::setVariant)
    addClip(clip)
}

data class DslImageVideoClipConfiguration(
    var size: Vector2i = Vector2i(100,100),
    var position: Vector2i = Vector2i(0,0),
    var file: File = File("Foo.bar"),
    var mode: StaticImageMode = StaticImageMode.StretchWithAspect
)

fun DslClipContainer.image(conf: DslImageVideoClipConfiguration.() -> Unit){
    val c = DslImageVideoClipConfiguration()
    c.conf()
    val clip = StaticImageVideoClip(
        UUID.randomUUID().toString(),
        c.size,
        c.position,
        true,
        c.file,
        StaticImageVideoClipConfiguration(c.mode)
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

class DslVerticalLayoutConfiguration(
    var padding: Padding = Padding(0,0,0,0)
): DslClipContainer()

fun DslClipContainer.vertical(conf: DslVerticalLayoutConfiguration.() -> Unit){
    val c = DslVerticalLayoutConfiguration()
    c.conf()
    val verticalLayout = VerticalLayout(
        VerticalLayoutConfiguration(c.padding)
    )
    c.getClips().forEach(verticalLayout::addChild)
    addClip(verticalLayout)
}

class DslHorizontalLayoutConfiguration: DslClipContainer()

fun DslClipContainer.horizontal(conf: DslHorizontalLayoutConfiguration.() -> Unit){
    val c = DslHorizontalLayoutConfiguration()
    c.conf()
    val hLayout = HorizontalLayout()
    c.getClips().forEach(hLayout::addChild)
    addClip(hLayout)
}

data class DslArrowConfiguration(
    var topLeft: Vector2i = Vector2i(0,0),
    var vec: Vector2i = Vector2i(100,0),
    var width: Double = 30.0,
    var outlineWidth: Float = 3f,
    var outlineColor: Color = Color.RED,
    var fillColor: Color = Color.YELLOW
)

fun DslClipContainer.arrow(conf: DslArrowConfiguration.() -> Unit){
    val c = DslArrowConfiguration()
    c.conf()
    val clip = ArrowVideoClip(
        UUID.randomUUID().toString(),
        c.topLeft,
        true,
        ArrowVideoClipConfiguration(
            Vector2(c.vec.x.toDouble(), c.vec.y.toDouble()).length(),
            c.width,
            Math.PI / 2 - atan2(c.vec.y.toDouble(), c.vec.x.toDouble()),
            c.outlineWidth,
            c.outlineColor,
            c.fillColor
        )
    )

    addClip(clip)
}

class DslContainerBorderConfiguration(
    var width: Int = 0,
    var color: Color = Color.WHITE
)

class DslContainerConfiguration(
    var size: Vector2i = Vector2i(100,100),
    var position: Vector2i = Vector2i(0,0),
    var border: DslContainerBorderConfiguration = DslContainerBorderConfiguration()
): DslClipContainer()

fun DslClipContainer.container(conf: DslContainerConfiguration.() -> Unit){
    val c = DslContainerConfiguration()
    c.conf()

    val clip = ContainerVideoClip(
        UUID.randomUUID().toString(),
        c.size,
        c.position,
        true,
        0f,
        ContainerVideoClipConfiguration(
            ContainerBorderConfiguration(
                c.border.width,
                c.border.color
            )
        )
    )

    c.getClips().forEach(clip::addChild)

    addClip(clip)
}

fun DslContainerConfiguration.border(conf: DslContainerBorderConfiguration.() -> Unit){
    val c = DslContainerBorderConfiguration()
    c.conf()

    border = c
}

data class DslMultilineTextConfiguration(
    var text: String = "Foobar",
    var position: Vector2i = Vector2i(0,0),
    var size: Vector2i = Vector2i(100,100),
    var fontSize: Int = 24,
    var color: Color = Color.WHITE,
    var mode: MultilineMode = MultilineMode.Auto,
    var lineDistance: Int = 2
)

fun DslClipContainer.multiline(conf: DslMultilineTextConfiguration.() -> Unit){
    val c = DslMultilineTextConfiguration()
    c.conf()

    val clip = MultilineTextVideoClip(
        UUID.randomUUID().toString(),
        c.size,
        c.position,
        true,
        MultilineTextVideoClipConfiguration(
            c.text,
            c.fontSize,
            c.color,
            mode = c.mode,
            lineDistance = c.lineDistance
        )
    )

    addClip(clip)
}

data class DslSingleColorConfiguration(
    var position: Vector2i = Vector2i(0,0),
    var size: Vector2i = Vector2i(100,100),
    var color: Color = Color.BLACK
)

fun DslClipContainer.color(conf: DslSingleColorConfiguration.() -> Unit){
    val c = DslSingleColorConfiguration()
    c.conf()

    val clip = ColorVideoClip(
        UUID.randomUUID().toString(),
        c.size,
        c.position,
        true,
        ColorVideoClipConfiguration(c.color)
    )

    addClip(clip)
}

data class DslSVGConfiguration(
    var position: Vector2i = Vector2i(0,0),
    var size: Vector2i = Vector2i(100,100),
    var svg: File = File("foo")
)

fun DslClipContainer.svg(conf: DslSVGConfiguration.() -> Unit){
    val c = DslSVGConfiguration()
    c.conf()

    val clip = SVGVideoClip(
        UUID.randomUUID().toString(),
        c.size,
        c.position,
        true, SVGVideoClipConfiguration(
            c.svg
        )
    )

    addClip(clip)
}

data class DslFormulaConfiguration(
    var size: Vector2i = Vector2i(100,100),
    var position: Vector2i = Vector2i(0,0),
    var initialFontSize: Int = 32,
    var fontSizeDecrease: Int = 3,
    var formula: FormulaElement = FormulaTextElement("Blubb")
)

fun DslClipContainer.formula(conf: DslFormulaConfiguration.() -> Unit){
    val c = DslFormulaConfiguration()
    c.conf()

    val clip = FormulaVideoClip(
        UUID.randomUUID().toString(),
        c.size,
        c.position,
        true,
        c.formula,
        StaticFormulaVideoClipConfiguration(
            c.initialFontSize,
            c.fontSizeDecrease
        )
    )

    addClip(clip)
}

data class DslVideoClipConfiguration(
    var size: Vector2i = Vector2i(100,100),
    var position: Vector2i = Vector2i(0,0),
    var file: File = File("foo"),
    var shift: Vector2i = Vector2i(0,0),
    var offset: Double = 0.0,
    var videoSize: Vector2i = Vector2i(100,100)
)

fun DslClipContainer.video(conf: DslVideoClipConfiguration.() -> Unit){
    val c = DslVideoClipConfiguration()
    c.conf()

    val clip = FileVideoClip(
        UUID.randomUUID().toString(),
        c.size,
        c.position,
        c.file,
        c.videoSize,
        c.offset,
        c.shift
    )

    addClip(clip)
}