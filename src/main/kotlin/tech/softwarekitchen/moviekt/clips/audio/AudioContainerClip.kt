package tech.softwarekitchen.moviekt.clips.audio

import java.lang.Double.max
import java.lang.Double.min

class AudioContainerClip(private val length: Double): AudioClip() {
    private data class AudioClipEntry(val clip: AudioClip, val offset: Double)
    private val clips = ArrayList<AudioClipEntry>()

    fun addClip(clip: AudioClip, offset: Double = 0.0){
        clips.add(AudioClipEntry(clip, offset))
    }

    override fun getAt(t: Double): Double {
        val raw = clips
            .filter { t >= it.offset && t < it.offset + it.clip.getLength() }
            .sumOf { it.clip.getAt(t - it.offset) }

        return max(-1.0, min(1.0, raw))
    }

    override fun getLength(): Double {
        return length
    }
}
