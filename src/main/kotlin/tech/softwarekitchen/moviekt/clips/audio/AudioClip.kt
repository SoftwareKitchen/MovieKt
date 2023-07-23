package tech.softwarekitchen.moviekt.clips.audio

abstract class AudioClip(val numChannels: Int) {

    abstract fun getLength(): Double
    abstract fun getAt(t: Double): List<Double>
}
