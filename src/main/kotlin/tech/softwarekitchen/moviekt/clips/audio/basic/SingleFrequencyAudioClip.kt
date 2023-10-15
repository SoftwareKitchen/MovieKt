package tech.softwarekitchen.moviekt.clips.audio.basic

import tech.softwarekitchen.moviekt.clips.audio.AudioClip
import kotlin.math.PI
import kotlin.math.sin

class SingleFrequencyAudioClip(val frequencies: List<Double>, val clipLength: Double): AudioClip(frequencies.size){

    override fun getAt(t: Double): List<Double> {
        return frequencies.map{ sin(2 * PI * it * t ) }
    }

    override fun getLength(): Double {
        return clipLength
    }
}