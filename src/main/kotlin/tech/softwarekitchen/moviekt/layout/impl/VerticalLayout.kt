package tech.softwarekitchen.moviekt.layout.impl

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.layout.Layout
import tech.softwarekitchen.moviekt.util.Padding

data class VerticalLayoutConfiguration(val padding: Padding, val spaceBetween: Int)

class VerticalLayout(private val configuration: VerticalLayoutConfiguration): Layout() {
    companion object{
        fun of(padding: Padding, spaceBetween: Int, vararg clips: VideoClip): VerticalLayout {
            val layout = VerticalLayout(VerticalLayoutConfiguration(padding, spaceBetween))
            clips.forEach(layout::addChild)
            return layout
        }
    }
    override fun recalculateChildren(){
        val children = getChildren()
        val totalSpace = Vector2i(getSize().x - configuration.padding.left - configuration.padding.right, getSize().y - configuration.padding.top - configuration.padding.bottom - (children.size * configuration.spaceBetween))
        val spacePerChild = Vector2i(totalSpace.x, totalSpace.y / children.size)
        children.forEachIndexed {
            index, videoClip ->
            videoClip.set(PropertyKey_Position, Vector2i(configuration.padding.left, configuration.padding.top + index * (spacePerChild.y + configuration.spaceBetween)))
            videoClip.set(PropertyKey_Size, spacePerChild)
        }
    }
}
