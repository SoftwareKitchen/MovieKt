package tech.softwarekitchen.moviekt.clips.video.ui

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.clips.video.VideoTimestamp
import tech.softwarekitchen.moviekt.clips.video.basic.ContainerVideoClip
import tech.softwarekitchen.moviekt.clips.video.text.StaticTextVideoClipConfiguration
import tech.softwarekitchen.moviekt.clips.video.text.TextAnchor
import tech.softwarekitchen.moviekt.clips.video.text.TextVideoClip
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.*

data class ChapterVideoClipConfiguration(val chapters: List<String>)

class ChapterVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    private val configuration: ChapterVideoClipConfiguration
    ): VideoClip(id, size, position,true) {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val child = ContainerVideoClip(
        UUID.randomUUID().toString(),
        Vector2i(size.x * configuration.chapters.size, size.y),
        Vector2i(0,0),
        true
    )

    companion object{
        val PropertyKey_ChapterPosition = "ChapterPosition"
    }

    private val chapterPositionProperty = VideoClipProperty(
        PropertyKey_ChapterPosition,
        0.0,
        {
            child.set(PropertyKey_Position, Vector2i((it * getSize().x).toInt(), 0))
            markDirty(null)
        }
    )

    init {
        registerProperty(chapterPositionProperty)

        configuration.chapters.forEachIndexed {
            index, chapter ->
            val item = TextVideoClip(
                "_", getSize(),
                Vector2i(index * getSize().x, 0),true,
                StaticTextVideoClipConfiguration(chapter,36, Color.WHITE, anchor = TextAnchor.Center)
            )
            child.addChild(item)
        }

        addChild(child)
    }

    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {

    }
}
