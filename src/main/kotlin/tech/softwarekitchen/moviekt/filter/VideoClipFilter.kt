package tech.softwarekitchen.moviekt.filter

import tech.softwarekitchen.moviekt.util.Pixel

interface VideoClipFilter {
    fun filter(x: Int, y: Int, xSize: Int, ySize: Int, pixel: Pixel): Pixel
}

class VideoClipFilterChain: VideoClipFilter {
    private val filters = ArrayList<VideoClipFilter>()

    fun addFilter(filter: VideoClipFilter){
        filters.add(filter)
    }
    override fun filter(x: Int, y: Int, xSize: Int, ySize: Int, pixel: Pixel): Pixel{
        var _pixel = pixel
        filters.forEach{
            _pixel = it.filter(x, y, xSize, ySize, _pixel)
        }
        return _pixel
    }
}
