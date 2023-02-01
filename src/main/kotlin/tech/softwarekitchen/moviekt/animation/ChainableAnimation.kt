package tech.softwarekitchen.moviekt.animation

open class ChainableAnimation<Tout>(private val partialAnimation: (Float,Float?,Float?) -> Tout?, private val defaultValue: Tout) {
    private var next: ChainableAnimation<Tout>? = null
    fun and(next: ChainableAnimation<Tout>):  ChainableAnimation<Tout>{
        when(val n = this.next){
            null -> this.next = next
            else -> n.and(next)
        }
        return this
    }

    fun build(): (Float, Float?,Float?) -> Tout{
        val nextInChain: ((Float, Float?, Float?) -> Tout)? = when(val n = next){
            null -> null
            else -> n.build()
        }
        return {
                tAbs, tTot, tRel ->
            when(val internalResult = partialAnimation(tAbs, tTot, tRel)){
                null -> {
                    when(nextInChain){
                        null -> defaultValue
                        else -> nextInChain(tAbs, tTot, tRel)
                    }
                }
                else -> internalResult
            }
        }
    }
}
