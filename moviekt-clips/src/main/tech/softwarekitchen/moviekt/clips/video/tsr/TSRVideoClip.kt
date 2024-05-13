package tech.softwarekitchen.moviekt.clips.video.tsr

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.core.util.VideoTimestamp
import tech.softwarekitchen.moviekt.core.video.VideoClip
import tech.softwarekitchen.tsr.camera.Camera
import tech.softwarekitchen.tsr.scene.Scene
import java.awt.image.BufferedImage

data class TSRSceneDescriptor(val scene: Scene, val camera: Camera)

class TSRVideoClip(
    id: String, size: Vector2i, position: Vector2i, visible: Boolean, private val scene: TSRSceneDescriptor
): VideoClip(id, size, position, visible, volatile = true) {
    companion object{
        val PropertyKey_Scene = "Scene"
    }

    override val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val sceneProperty = VideoClipProperty(
        PropertyKey_Scene,
        scene,
        this::markDirty
    )

    init{
        registerProperty(sceneProperty)
    }

    fun updateScene(scene: TSRSceneDescriptor){
        sceneProperty.set(scene)
    }

    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val rendered = sceneProperty.v.camera.render(sceneProperty.v.scene)
        val g = img.createGraphics()
        g.drawImage(rendered,0,0,null)
    }
}
