package inc.ahmedmourad.sherlock.children.local.repository

import arrow.core.Tuple2
import dagger.Lazy
import inc.ahmedmourad.sherlock.children.local.database.SherlockDatabase
import inc.ahmedmourad.sherlock.children.local.mapper.toDomainSimpleChild
import inc.ahmedmourad.sherlock.children.local.mapper.toRoomChildEntity
import inc.ahmedmourad.sherlock.children.local.model.entities.RoomChildEntity
import inc.ahmedmourad.sherlock.children.repository.dependencies.ChildrenLocalRepository
import inc.ahmedmourad.sherlock.domain.model.children.DomainRetrievedChild
import inc.ahmedmourad.sherlock.domain.model.children.DomainSimpleRetrievedChild
import io.reactivex.*
import io.reactivex.schedulers.Schedulers

internal class ChildrenRoomLocalRepository(private val db: Lazy<SherlockDatabase>) : ChildrenLocalRepository {

    override fun updateIfExists(
            child: DomainRetrievedChild
    ): Maybe<Tuple2<DomainRetrievedChild, Int>> {
        return db.get()
                .resultsDao()
                .updateIfExists(child.toRoomChildEntity(-1))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(RoomChildEntity::toDomainRetrievedChild)
    }

    override fun findAll(): Flowable<List<Tuple2<DomainSimpleRetrievedChild, Int>>> {
        return db.get()
                .resultsDao()
                .findAll()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .distinctUntilChanged()
                .flatMap { list ->
                    Flowable.fromIterable(list)
                            .map { it.simplify().toDomainSimpleChild() }
                            .toList()
                            .toFlowable()
                }
    }

    override fun replaceAll(
            results: List<Tuple2<DomainRetrievedChild, Int>>
    ): Single<List<Tuple2<DomainSimpleRetrievedChild, Int>>> {
        return db.get()
                .resultsDao()
                .replaceAll(results.map(Tuple2<DomainRetrievedChild, Int>::toRoomChildEntity))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .toSingleDefault(results)
                .flatMap {
                    Observable.fromIterable(it)
                            .map { (child, score) ->
                                child.toRoomChildEntity(score).simplify().toDomainSimpleChild()
                            }.toList()
                }
    }

    override fun clear(): Completable {
        return Completable.fromAction { db.get().resultsDao().deleteAll() }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
    }
}
