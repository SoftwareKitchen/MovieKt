package tech.softwarekitchen.moviekt.clips.audio

abstract class AudioClip {

    abstract fun getLength(): Double
    abstract fun getAt(t: Double): Double
}
