package tech.softwarekitchen.moviekt.clips.video.file

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.core.util.VideoTimestamp
import java.awt.image.BufferedImage
import java.io.File

class MappedVideoTimestamp( val outerTs: Double, val videoTs: Double?)

class TimeMappedVideoClipConfiguration(
    val file: File,
    val tsMap: List<MappedVideoTimestamp>
)

class TimeMappedFileVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    videoSize: Vector2i,
    configuration: TimeMappedVideoClipConfiguration
): FileVideoClip(id, size, position, configuration.file, videoSize) {
    private class TimeMapInterval(val outerStart: Double, val outerEnd: Double, val videoStart: Double, val videoEnd: Double){
        operator fun invoke(at: Double): Double{
            val q = (at - outerStart) / (outerEnd - outerStart)
            return videoStart + (videoEnd - videoStart) * q
        }
    }
    private val intervals: List<TimeMapInterval>

    init {
        intervals = (0 until configuration.tsMap.size - 1).map{
            TimeMapInterval(
                configuration.tsMap[it].outerTs,
                configuration.tsMap[it+1].outerTs,
                configuration.tsMap[it].videoTs ?: duration,
                configuration.tsMap[it+1].videoTs ?: duration
            )
        }

        val timeMapper: (Double) -> Double = {
            outTime ->
            val interval = intervals.firstOrNull{
                outTime >= it.outerStart && outTime < it.outerEnd
            }
            if(interval == null){
                0.0
            }else{
                interval(outTime)
            }
        }

        set(PropertyKey_VideoTimeMapping, timeMapper )
    }

    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val interval = intervals.firstOrNull{ t.t >= it.outerStart && t.t < it.outerEnd } ?: return
        super.renderContent(img, t)
    }
}
