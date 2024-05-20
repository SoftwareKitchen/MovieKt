package tech.softwarekitchen.movitkt.animation

import tech.softwarekitchen.moviekt.core.Movie
import tech.softwarekitchen.moviekt.core.extension.MovieKtExtension
import tech.softwarekitchen.moviekt.core.video.VideoClip

class MovieKtAnimationExtension: MovieKtExtension {
    val animations = ArrayList<Pair<MovieKtAnimation<*>, List<VideoClip>>>()
    override fun prepare(movie: Movie) {
        animations.addAll(movie.readRoot{
            rootVC ->
            rootVC.animationContainer().getAnimations().map{Pair(it, rootVC.findById(it.nodeId))}
        })
    }

    override fun frame(t: Float) {
        animations.forEach { animData ->
            if (animData.first.isApplicable(t)) {
                animData.second.forEach { target ->
                    target.set(animData.first.property, animData.first.get(t))
                }
            }
        }

        animations.removeIf { it.first.isFinished(t) }
    }
}
