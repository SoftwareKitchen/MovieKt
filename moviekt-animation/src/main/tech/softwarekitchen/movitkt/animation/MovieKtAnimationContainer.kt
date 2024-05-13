package tech.softwarekitchen.movitkt.animation

import tech.softwarekitchen.moviekt.core.extension.MovieKtVideoExtensionContainer
import tech.softwarekitchen.moviekt.core.mutation.MovieKtMutation
import tech.softwarekitchen.moviekt.core.video.VideoClip
import java.util.ArrayList

class MovieKtAnimationContainer(
    override val videoClip: VideoClip
): MovieKtVideoExtensionContainer{
    private val rawAnimations = ArrayList<MovieKtAnimation<*>>()
    private val rawMutations = ArrayList<MovieKtMutation>()

    fun addRawAnimation(anim: MovieKtAnimation<*>){
        rawAnimations.add(anim)
    }

    fun addRawMutation(mut: MovieKtMutation){
        rawMutations.add(mut)
    }

    fun getAnimations(): List<MovieKtAnimation<*>>{
        return (videoClip.readChildren{it.animationContainer().getAnimations()}.flatten() + rawAnimations).map{it.shift(videoClip.getTimeShift())}
    }
}

fun VideoClip.animationContainer(): MovieKtAnimationContainer{
    return getExtensionContainer<MovieKtAnimationContainer>(::MovieKtAnimationContainer)
}