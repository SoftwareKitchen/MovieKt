package tech.softwarekitchen.moviekt.layout.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.core.util.Padding
import tech.softwarekitchen.moviekt.core.video.VideoClip
import tech.softwarekitchen.moviekt.layout.Layout

data class VerticalLayoutConfiguration(val padding: Padding = Padding(0,0,0,0), val spaceBetween: Int = 0)

class VerticalLayout(private val configuration: VerticalLayoutConfiguration = VerticalLayoutConfiguration()): Layout() {
    companion object{
        fun of(padding: Padding, spaceBetween: Int, vararg clips: VideoClip): VerticalLayout {
            val layout = VerticalLayout(VerticalLayoutConfiguration(padding, spaceBetween))
            layout.addChild(*clips)
            return layout
        }
    }

    override fun recalculateChildren(){
        val children = readChildren{it}

        if(children.isEmpty()){
            return
        }

        val totalSpace = Vector2i(getSize().x - configuration.padding.left - configuration.padding.right, getSize().y - configuration.padding.top - configuration.padding.bottom - (children.size * configuration.spaceBetween))
        val spacePerChild = Vector2i(totalSpace.x, totalSpace.y / children.size)
        children.forEachIndexed {
            index, videoClip ->
            videoClip.set(PropertyKey_Position, Vector2i(configuration.padding.left, configuration.padding.top + index * (spacePerChild.y + configuration.spaceBetween)))
            videoClip.set(PropertyKey_Size, spacePerChild)
        }
    }
}
