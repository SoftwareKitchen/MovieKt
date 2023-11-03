package tech.softwarekitchen.moviekt.clips.audio

abstract class AudioClip(val numChannels: Int) {

    abstract fun getAt(t: Double): List<Double>
}
