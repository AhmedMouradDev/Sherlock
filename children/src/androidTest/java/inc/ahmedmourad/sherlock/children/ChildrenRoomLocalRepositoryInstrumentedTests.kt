package inc.ahmedmourad.sherlock.children

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Lazy
import inc.ahmedmourad.sherlock.children.local.database.SherlockDatabase
import inc.ahmedmourad.sherlock.children.local.entities.RoomChildEntity
import inc.ahmedmourad.sherlock.children.local.repository.ChildrenRoomLocalRepository
import inc.ahmedmourad.sherlock.children.repository.dependencies.ChildrenLocalRepository
import inc.ahmedmourad.sherlock.domain.constants.Gender
import inc.ahmedmourad.sherlock.domain.constants.Hair
import inc.ahmedmourad.sherlock.domain.constants.Skin
import inc.ahmedmourad.sherlock.domain.model.children.*
import inc.ahmedmourad.sherlock.domain.model.children.Range
import inc.ahmedmourad.sherlock.domain.model.children.submodel.ApproximateAppearance
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Coordinates
import inc.ahmedmourad.sherlock.domain.model.children.submodel.FullName
import inc.ahmedmourad.sherlock.domain.model.children.submodel.Location
import io.reactivex.Flowable
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import splitties.init.appCtx
import timber.log.Timber
import java.io.IOException
import java.util.*

@RunWith(AndroidJUnit4::class)
class ChildrenRoomLocalRepositoryInstrumentedTests {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: SherlockDatabase
    private lateinit var repository: ChildrenLocalRepository

    private val child0 = RetrievedChild(
            UUID.randomUUID().toString(),
            System.currentTimeMillis(),
            FullName("Ahmed", "Mourad"),
            "",
            Location("1", "a", "a", Coordinates(90.0, 140.0)),
            ApproximateAppearance(
                    Gender.MALE,
                    Skin.WHEAT,
                    Hair.DARK,
                    Range(18, 22),
                    Range(170, 190)
            ), ""
    ) to 100

    private val child1 = RetrievedChild(
            UUID.randomUUID().toString(),
            System.currentTimeMillis(),
            FullName("Yasmeen", "Mourad"),
            "",
            Location("11", "sh", "hs", Coordinates(70.0, 120.0)),
            ApproximateAppearance(
                    Gender.FEMALE,
                    Skin.WHITE,
                    Hair.DARK,
                    Range(16, 21),
                    Range(160, 180)
            ), ""
    ) to 200

    private val child2 = RetrievedChild(
            UUID.randomUUID().toString(),
            System.currentTimeMillis(),
            FullName("Ahmed", "Shamakh"),
            "",
            Location("111", "b", "bb", Coordinates(55.0, 99.0)),
            ApproximateAppearance(
                    Gender.MALE,
                    Skin.DARK,
                    Hair.BROWN,
                    Range(11, 23),
                    Range(150, 180)
            ), ""
    ) to 300

    @Before
    fun setup() {
        Timber.plant(Timber.DebugTree())
        setupRepositoryAndDatabase()
    }

    private fun setupRepositoryAndDatabase() {
        db = Room.inMemoryDatabaseBuilder(appCtx, SherlockDatabase::class.java).allowMainThreadQueries().build()
        repository = ChildrenRoomLocalRepository(Lazy { db })
    }

    @Test
    fun updateIfExists_shouldReturnTheScoreOfTheGivenChild() {

        repository.updateIfExists(child0.first)
                .test()
                .await()
                .assertNoErrors()
                .assertNoValues()
                .assertComplete()

        val resultsTestObserver = db.resultsDao()
                .findAllWithWeight()
                .distinctUntilChanged()
                .flatMap {
                    Flowable.fromIterable(it)
                            .map(RoomChildEntity::toRetrievedChild)
                            .toList()
                            .toFlowable()
                }.test()

        resultsTestObserver.awaitCount(1).assertValuesOnly(listOf())

        repository.replaceAll(listOf(child0))
                .test()
                .assertNoErrors()
                .assertComplete()

        resultsTestObserver.awaitCount(2).assertValuesOnly(
                listOf(),
                listOf(child0)
        )

        val newChild0 = child0.first.copy(fullName = FullName("Yasmeen", "Shamakh")) to child0.second

        repository.updateIfExists(newChild0.first)
                .test()
                .await()
                .assertNoErrors()
                .assertValue(newChild0)
                .assertComplete()

        resultsTestObserver.awaitCount(3).assertValuesOnly(
                listOf(),
                listOf(child0),
                listOf(newChild0)
        )
    }

    @Test
    fun replaceAll_shouldReplaceTheResultsInTheDatabaseWithTheOnesProvided() {

        repository.replaceAll(listOf(child1, child2)).test().await()

        val resultsTestObserver = db.resultsDao()
                .findAllWithWeight()
                .distinctUntilChanged()
                .flatMap {
                    Flowable.fromIterable(it)
                            .map(RoomChildEntity::toRetrievedChild)
                            .toList()
                            .toFlowable()
                }.test()

        resultsTestObserver.awaitCount(1).assertValuesOnly(listOf(child1, child2))

        repository.apply {
            replaceAll(listOf(child0, child1, child2))
                    .flatMap { replaceAll(listOf(child0)) }
                    .flatMap { replaceAll(listOf(child1, child2)) }
                    .flatMap { replaceAll(listOf(child0, child1, child2)) }
                    .test()
                    .assertNoErrors()
                    .assertComplete()
        }

        resultsTestObserver.awaitCount(5).assertValuesOnly(
                listOf(child1, child2),
                listOf(child0, child1, child2),
                listOf(child0),
                listOf(child1, child2),
                listOf(child0, child1, child2)
        )
    }

    @Test
    fun findAll_shouldRetrieveAndObserveChangesToTheResultsInTheDatabase() {

        db.resultsDao().replaceAll(listOf(child1, child2).map(Pair<RetrievedChild, Int>::toRoomChildEntity)).test().await()

        val resultsTestObserver = repository.findAllWithWeight().test()

        resultsTestObserver.awaitCount(1).assertValuesOnly(
                listOf(child1, child2).map { (child, score) ->
                    child.simplify() to score
                }
        )

        db.resultsDao().apply {
            replaceAll(listOf(child0, child1, child2).map(Pair<RetrievedChild, Int>::toRoomChildEntity))
                    .andThen(replaceAll(listOf(child0).map(Pair<RetrievedChild, Int>::toRoomChildEntity)))
                    .andThen(replaceAll(listOf(child1, child2).map(Pair<RetrievedChild, Int>::toRoomChildEntity)))
                    .andThen(replaceAll(listOf(child0, child1, child2).map(Pair<RetrievedChild, Int>::toRoomChildEntity)))
                    .test()
                    .assertNoErrors()
                    .assertComplete()
        }

        resultsTestObserver.awaitCount(5).assertValuesOnly(
                *listOf(
                        listOf(child1, child2),
                        listOf(child0, child1, child2),
                        listOf(child0),
                        listOf(child1, child2),
                        listOf(child0, child1, child2)
                ).map {
                    it.map { (child, score) ->
                        child.simplify() to score
                    }
                }.toTypedArray()
        )
    }

    @Test
    fun clear_shouldDeleteAllTheResultsInTheDatabase() {

        repository.replaceAll(listOf(child1, child2)).test().await()

        val resultsTestObserver = db.resultsDao()
                .findAllWithWeight()
                .distinctUntilChanged()
                .flatMap {
                    Flowable.fromIterable(it)
                            .map(RoomChildEntity::toRetrievedChild)
                            .toList()
                            .toFlowable()
                }.test()

        resultsTestObserver.awaitCount(1).assertValuesOnly(listOf(child1, child2))

        repository.clear().test().await().assertNoErrors().assertComplete()

        resultsTestObserver.awaitCount(2).assertValuesOnly(
                listOf(child1, child2),
                listOf()
        )
    }


    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}
