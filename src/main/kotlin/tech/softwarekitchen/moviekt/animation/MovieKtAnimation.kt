package tech.softwarekitchen.moviekt.animation

interface MovieKtAnimation<T: Any> {
    val nodeId: String
    val property: String
    fun isApplicable(t: Float): Boolean
    fun isFinished(t: Float): Boolean
    fun get(t: Float): T
    fun shift(t: Float): MovieKtAnimation<T>
}

abstract class MKTOnceAnimation<T: Any>(
    override val nodeId: String,
    override val property: String,
    val at: Float
): MovieKtAnimation<T>{
    override fun isFinished(t: Float): Boolean {
        return t >= at
    }

    override fun isApplicable(t: Float): Boolean {
        return t >= at
    }
}

abstract class MKTTimerangeAnimation<T: Any>(
    override val nodeId: String,
    override val property: String,
    val start: Float,
    val duration: Float
): MovieKtAnimation<T>{
    override fun isFinished(t: Float): Boolean {
        return t > start + duration
    }

    override fun isApplicable(t: Float): Boolean {
        return t >= start && !isFinished(t)
    }
}

abstract class MKTPerpetualAnimation<T: Any>(
    override val nodeId: String,
    override val property: String
): MovieKtAnimation<T>{
    override fun isApplicable(t: Float): Boolean {
        return true
    }

    override fun isFinished(t: Float): Boolean {
        return false
    }
}