package tech.softwarekitchen.moviekt.clips.audio.basic

import tech.softwarekitchen.moviekt.clips.audio.AudioClip
import java.io.File

class FileAudioClip(f: File, private val offset: Double = 0.0): AudioClip(1) {
    constructor(path: String): this(File(path))

    private val data: ByteArray

    init{
        if(!f.exists() || f.isDirectory){
            throw Exception()
        }

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

    override fun getAt(t: Double): List<Double> {
        val index = 4* ((t + offset) * 44100).toInt()
        if(index < 0 || index > data.size -1){
            return listOf(0.0)
        }
        val s = (data[index].toUByte() * 256u + data[index+1].toUByte()).toInt() - 32768
        return listOf(s.toDouble() / 32768.0)
    }

    override fun getLength(): Double {
        return data.size.toDouble() / 88200.0
    }
}
