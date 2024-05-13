package tech.softwarekitchen.moviekt.layout

import org.slf4j.LoggerFactory
import tech.softwarekitchen.moviekt.core.Movie
import tech.softwarekitchen.moviekt.core.extension.MovieKtExtension

class MovieKtLayoutExtension: MovieKtExtension {
    private val logger = LoggerFactory.getLogger(javaClass.name)
    override fun prepare(movie: Movie) {
        logger.info("Initializing layouts")
        movie.onRoot { it.initializeLayouts() }
        logger.info("Video preparations completed")
    }
}
