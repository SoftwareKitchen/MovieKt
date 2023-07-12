package tech.softwarekitchen.moviekt.clips.audio

import java.io.File

class FileAudioClip(f: File, private val offset: Double = 0.0): AudioClip() {
    constructor(path: String): this(File(path))

    private val data: ByteArray

    init{
        val process = ProcessBuilder(
            "ffmpeg",
            "-i",
            f.absolutePath,
            "-f",
            "u16be",
            "-ar",
            "44100",
            "-"
        ).start()

        val stream = process.inputStream
        data = stream.readAllBytes()
    }

    override fun getAt(t: Double): Double {
        val index = 4* ((t + offset) * 44100).toInt()
        if(index < 0 || index > data.size -1){
            throw Exception()
        }
        val s = (data[index].toUByte() * 256u + data[index+1].toUByte()).toInt() - 32768
        return s.toDouble() / 32768.0
    }

    override fun getLength(): Double {
        return data.size.toDouble() / 88200.0
    }
}
