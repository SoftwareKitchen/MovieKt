package tech.softwarekitchen.moviekt.clips.video.tsr

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.tsr.camera.Camera
import tech.softwarekitchen.tsr.scene.Scene
import java.awt.image.BufferedImage

data class TSRSceneDescriptor(val scene: Scene, val camera: Camera)

class TSRVideoClip(
    id: String, size: Vector2i, position: Vector2i, visible: Boolean, private val scene: TSRSceneDescriptor
): VideoClip(id, size, position, visible) {
    override fun renderContent(img: BufferedImage) {
        val rendered = scene.camera.render(scene.scene)
        val g = img.createGraphics()
        g.drawImage(rendered,0,0,null)
    }
}
