package tech.softwarekitchen.moviekt.animation

interface MovieKtAnimation<T: Any> {
    val nodeId: String
    val property: String
    fun isApplicable(t: Float): Boolean
    fun isFinished(t: Float): Boolean
    fun get(t: Float): T
}

