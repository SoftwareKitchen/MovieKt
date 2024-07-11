package tech.softwarekitchen.moviekt.core.context

import tech.softwarekitchen.moviekt.core.Movie

class MovieKtContext {
    companion object {
        var activeRenderProcess: Movie? = null
        var currentTime: Float = 0f

        inline fun <reified T: Any>getExtension(): T?{
            return activeRenderProcess?.extensions?.firstOrNull{it is T} as T?
        }
    }
}
