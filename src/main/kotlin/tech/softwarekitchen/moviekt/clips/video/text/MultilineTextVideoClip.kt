package tech.softwarekitchen.moviekt.clips.video.text

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.clips.video.VideoTimestamp
import java.awt.Color
import java.awt.Font
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.io.File

enum class MultilineMode{
    Linebreak, Auto
}

data class MultilineTextVideoClipConfiguration(
    val text: String,
    val fontSize: Int = 24,
    val color: Color = Color.BLACK,
    val ttFont: File? = null,
    val anchor: TextAnchor = tech.softwarekitchen.moviekt.clips.video.text.TextAnchor.Left,
    val lineDistance: Int,
    val mode: MultilineMode = MultilineMode.Auto
)

private data class Line(val text: String, val bounds: Rectangle2D)

class MultilineTextVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val configuration: MultilineTextVideoClipConfiguration
): VideoClip(id, size, position, visible){

    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val graphics = img.createGraphics()
        val _font = configuration.ttFont?.let{
            Font.createFont(Font.TRUETYPE_FONT, it)
        }
        val font = (_font ?: graphics.font).deriveFont(configuration.fontSize.toFloat())
        graphics.font = font

        val lines = ArrayList<Line>()

        when(configuration.mode){
            MultilineMode.Auto -> {
                var parts = configuration.text.split(" ").toMutableList()
                var index = 1
                while(parts.isNotEmpty()){
                    val text = parts.subList(0,index).joinToString(" ")
                    val bounds = font.getStringBounds(text, graphics.fontRenderContext)
                    if(bounds.width > img.width || index == parts.size){
                        if(bounds.width <= img.width){
                            index++
                        }
                        if(index == 1){
                            throw Exception()
                        }
                        val l = parts.subList(0,index-1).joinToString(" ")
                        parts = parts.subList(index - 1, parts.size)
                        val r2d = font.getStringBounds(l, graphics.fontRenderContext)
                        lines.add(Line(l, r2d))
                        index = 0
                    }
                    index++
                }
            }
            MultilineMode.Linebreak -> {
                val split = configuration.text.split("\n")
                lines.addAll(split.map{
                    Line(it, font.getStringBounds(it, graphics.fontRenderContext))
                })
            }
        }

        val lineHeight = lines.maxOf{it.bounds.height}.toInt() + configuration.lineDistance

        lines.forEachIndexed{
            i, it ->
            val top = lineHeight * i - it.bounds.y.toInt()
            val left = when(configuration.anchor){
                TextAnchor.Center -> (img.width - it.bounds.width.toInt()) / 2
                TextAnchor.Left -> 0
            } - it.bounds.x.toInt()

            graphics.drawString(it.text, left, top)
        }
    }
}
