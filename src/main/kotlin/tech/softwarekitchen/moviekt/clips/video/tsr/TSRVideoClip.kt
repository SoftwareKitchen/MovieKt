package tech.softwarekitchen.moviekt.clips.video.tsr

import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.tsr.camera.Camera
import tech.softwarekitchen.tsr.scene.Scene
import java.awt.image.BufferedImage

data class TSRSceneDescriptor(val scene: Scene, val camera: Camera)

class TSRVideoClip(size: SizeProvider, private val sceneProvider: (Int, Int, Float) -> TSRSceneDescriptor, tOffset: Float = 0f, visibilityDuration: Float? = null): VideoClip(size, tOffset, visibilityDuration) {
    override fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float): BufferedImage {
        val toRender = sceneProvider(frameNo, nFrames, tTotal)
        return toRender.camera.render(toRender.scene)
    }
}
