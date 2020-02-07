package inc.ahmedmourad.sherlock.children.repository.dependencies

import arrow.core.Either
import arrow.core.Tuple2
import inc.ahmedmourad.sherlock.domain.filter.Filter
import inc.ahmedmourad.sherlock.domain.model.children.*
import inc.ahmedmourad.sherlock.domain.model.children.submodel.ChildId
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Url
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Weight
import io.reactivex.Flowable
import io.reactivex.Single

internal interface ChildrenRemoteRepository {

    fun publish(
            childId: ChildId,
            child: PublishedChild,
            pictureUrl: Url?
    ): Single<Either<Throwable, RetrievedChild>>

    fun find(
            childId: ChildId
    ): Flowable<Either<Throwable, RetrievedChild?>>

    fun findAll(
            query: ChildQuery,
            filter: Filter<RetrievedChild>
    ): Flowable<Either<Throwable, List<Tuple2<RetrievedChild, Weight>>>>

    fun clear(): Single<Either<Throwable, Unit>>
}
