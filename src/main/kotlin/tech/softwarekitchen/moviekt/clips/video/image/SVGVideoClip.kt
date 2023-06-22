package tech.softwarekitchen.moviekt.clips.video.image

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.ksvg.svg.SVGImage
import tech.softwarekitchen.ksvg.svg.draw.draw
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.image.BufferedImage
import java.io.File

data class SVGVideoClipConfiguration(
    val file: File
)


class SVGVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val configuration: SVGVideoClipConfiguration,
): VideoClip(
    id, size, position, visible
) {
    companion object{
        val PropertyKey_File = "file"
    }

    private var content: SVGImage = SVGImage(configuration.file)
    private val fileProperty = VideoClipProperty(PropertyKey_File,configuration.file,this::loadImage,{File(it as String)})

    init{
        registerProperty(fileProperty)
    }

    private fun loadImage(){
        content = SVGImage(fileProperty.v)
        markDirty()
    }

    override fun renderContent(img: BufferedImage) {
        content.draw(img)
    }
}

