package tech.softwarekitchen.moviekt.clips.audio.basic

import tech.softwarekitchen.moviekt.clips.audio.AudioClip
import java.lang.Double.max
import java.lang.Double.min

class AudioContainerClip(private val length: Double, numChannels: Int): AudioClip(numChannels) {
    private data class AudioClipEntry(val clip: AudioClip, val offset: Double, val volume: Double)
    private val clips = ArrayList<AudioClipEntry>()

    fun addClip(clip: AudioClip, offset: Double = 0.0, volume: Double = 1.0){
        clips.add(AudioClipEntry(clip, offset, volume))
    }

    override fun getAt(t: Double): List<Double> {
        val raw = clips
            .filter { t >= it.offset && t < it.offset + it.clip.getLength() }
            .map{ entry -> entry.clip.getAt(t - entry.offset).map{it * entry.volume} }

        val combined = (0 until numChannels).map{
            cid ->
            raw.sumOf{
                when{
                    cid < it.size -> it[cid]
                    else -> 0.0
                }
            }
        }.map{max(-1.0, min(1.0, it))}

        return combined
    }

    override fun getLength(): Double {
        return length
    }
}
