package tech.softwarekitchen.moviekt.clips.video.basic

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.clips.video.VideoTimestamp
import java.awt.Color
import java.awt.image.BufferedImage

data class ColorVideoClipConfiguration(val background: Color, val border: Color? = null, val borderWidth: Int = 0, val borderRadius: Int = 0)

open class ColorVideoClip(id: String, size: Vector2i, position: Vector2i, visible: Boolean, configuration: ColorVideoClipConfiguration): VideoClip(id, size,position, visible) {
    companion object{
        val PropertyKey_Background = "Background"
        val PropertyKey_BorderColor = "BorderColor"
        val PropertyKey_BorderWidth = "BorderWidth"
        val PropertyKey_BorderRadius = "BorderRadius"
    }
    override val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val propertyBackground = VideoClipProperty(PropertyKey_Background, configuration.background,this::markDirty)
    private val propertyBorderColor = VideoClipProperty(PropertyKey_BorderColor, configuration.border,this::markDirty)
    private val propertyBorderWidth = VideoClipProperty(PropertyKey_BorderWidth, configuration.borderWidth,this::markDirty)
    private val propertyBorderRadius = VideoClipProperty(PropertyKey_BorderRadius, configuration.borderRadius,this::markDirty)

    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val bc = propertyBorderColor.v
        val bw = propertyBorderWidth.v
        val br = propertyBorderRadius.v

        val graphics = img.createGraphics()

        if(bc == null || bw <= 0){
            graphics.color = propertyBackground.v
            graphics.fillOval(0,0,br * 2, br * 2)
            graphics.fillOval(img.width - 2 * br, 0, 2 * br, 2 * br)
            graphics.fillOval(0,img.height - 2 * br, 2 * br, 2 * br)
            graphics.fillOval(img.width - 2 * br, img.height - 2 * br, 2 * br, 2 * br)

            graphics.fillRect(br, 0, img.width - 2 * br, img.height)
            graphics.fillRect(0, br, img.width, img.height - 2 * br)
        }

        //Outer: BorderRadius + Width / 2
        val effectiveRadius = br - bw / 2
        graphics.color = bc
        graphics.fillOval(0,0,effectiveRadius * 2, effectiveRadius * 2)
        graphics.fillOval(img.width - 2 * effectiveRadius, 0, 2 * effectiveRadius, 2 * effectiveRadius)
        graphics.fillOval(0,img.height - 2 * effectiveRadius, 2 * effectiveRadius, 2 * effectiveRadius)
        graphics.fillOval(img.width - 2 * effectiveRadius, img.height - 2 * effectiveRadius, 2 * effectiveRadius, 2 * effectiveRadius)

        graphics.fillRect(effectiveRadius, 0, img.width - 2 * effectiveRadius, img.height)
        graphics.fillRect(0, effectiveRadius, img.width, img.height - 2 * effectiveRadius)

        graphics.color = propertyBackground.v
        graphics.fillOval(bw,bw,effectiveRadius * 2, effectiveRadius * 2)
        graphics.fillOval(img.width - 2 * effectiveRadius - bw, bw, 2 * effectiveRadius, 2 * effectiveRadius)
        graphics.fillOval(bw,img.height - 2 * effectiveRadius - bw, 2 * effectiveRadius, 2 * effectiveRadius)
        graphics.fillOval(img.width - 2 * effectiveRadius - bw, img.height - 2 * effectiveRadius - bw, 2 * effectiveRadius, 2 * effectiveRadius)

        graphics.fillRect(effectiveRadius+bw, bw, img.width - 2 * effectiveRadius - 2 * bw, img.height - 2 * bw)
        graphics.fillRect(bw, effectiveRadius + bw, img.width - 2 * bw, img.height - 2 * effectiveRadius - 2 * bw)

    }

}
