package inc.ahmedmourad.sherlock.domain.filter

import arrow.core.Tuple2

//TODO: should be a function
interface Filter<T> {
    //TODO: return a double as a ratio that this is the right child
    fun filter(items: List<T>): List<Tuple2<T, Int>>
}
