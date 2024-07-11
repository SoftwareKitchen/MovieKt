package tech.softwarekitchen.moviekt.clips.video.basic

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.text.StaticTextVideoClipConfiguration
import tech.softwarekitchen.moviekt.clips.video.text.TextVideoClip
import tech.softwarekitchen.moviekt.theme.VideoTheme
import tech.softwarekitchen.moviekt.theme.themeContainer
import java.awt.Color

class GroupVideoClipConfiguration(color: Color, val caption: String) : ColorVideoClipConfiguration(color)

class GroupVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val configuration: GroupVideoClipConfiguration
): ColorVideoClip(id, size, position, visible, configuration) {
    val caption: TextVideoClip
    init{
        caption = TextVideoClip("_", Vector2i(getSize().x - 20, 35), Vector2i(10,0),true, StaticTextVideoClipConfiguration(configuration.caption))
        caption.themeContainer().setVariant("group")
        addChild(caption)
        themeContainer().setVariant("group")
    }
}
