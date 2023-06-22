package tech.softwarekitchen.moviekt.clips.video.tsr

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.common.vector.Vector3
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.tsr.camera.Camera
import tech.softwarekitchen.tsr.color.Color
import tech.softwarekitchen.tsr.scene.Scene
import java.awt.image.BufferedImage

data class TSRSceneDescriptor(val scene: Scene, val camera: Camera)

class TSRVideoClip(
    id: String, size: Vector2i, position: Vector2i, visible: Boolean, private val scene: TSRSceneDescriptor
): VideoClip(id, size, position, visible) {
    companion object{
        val PropertyKey_Scene = "Scene"
    }

    private val sceneProperty = VideoClipProperty(
        PropertyKey_Scene,
        TSRSceneDescriptor(
            Scene(listOf()),
            Camera(
                Vector3(0.0,0.0,0.0),
                Vector3(1.0,0.0,0.0),
                Vector3(0.0,1.0,0.0),
                45.0,
                size,
                Color(0,0,0,0),
                listOf()
            )
        ),
        this::markDirty
    )

    init{
        registerProperty(sceneProperty)
    }

    fun updateScene(scene: TSRSceneDescriptor){
        sceneProperty.set(scene)
    }

    override fun renderContent(img: BufferedImage) {
        val rendered = sceneProperty.v.camera.render(sceneProperty.v.scene)
        val g = img.createGraphics()
        g.drawImage(rendered,0,0,null)
    }
}
