package tech.softwarekitchen.moviekt.core.audio

abstract class AudioClip(val numChannels: Int) {

    abstract fun getAt(t: Double): List<Double>
}
