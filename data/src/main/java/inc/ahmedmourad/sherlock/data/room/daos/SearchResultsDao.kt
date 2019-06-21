package inc.ahmedmourad.sherlock.data.room.daos

import androidx.room.*
import inc.ahmedmourad.sherlock.data.room.contract.RoomContract.ChildrenEntry
import inc.ahmedmourad.sherlock.data.room.entities.RoomChildEntity
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
abstract class SearchResultsDao {

    @Query("""SELECT
            ${ChildrenEntry.COLUMN_ID},
            ${ChildrenEntry.COLUMN_PUBLICATION_DATE},
            ${ChildrenEntry.COLUMN_FIRST_NAME},
            ${ChildrenEntry.COLUMN_LAST_NAME},
            ${ChildrenEntry.COLUMN_PICTURE_URL},
            ${ChildrenEntry.COLUMN_LOCATION},
            ${ChildrenEntry.COLUMN_NOTES},
            ${ChildrenEntry.COLUMN_GENDER},
            ${ChildrenEntry.COLUMN_SKIN},
            ${ChildrenEntry.COLUMN_HAIR},
            ${ChildrenEntry.COLUMN_START_AGE},
            ${ChildrenEntry.COLUMN_END_AGE},
            ${ChildrenEntry.COLUMN_START_HEIGHT},
            ${ChildrenEntry.COLUMN_END_HEIGHT},
            ${ChildrenEntry.COLUMN_SCORE}
            FROM
            ${ChildrenEntry.TABLE_NAME}""")
    abstract fun getResults(): Flowable<List<RoomChildEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun bulkInsert(resultsEntities: List<RoomChildEntity>)

    @Query("DELETE FROM ${ChildrenEntry.TABLE_NAME}")
    protected abstract fun deleteAll()

    @Transaction
    protected open fun replaceResultsTransaction(resultsEntities: List<RoomChildEntity>) {
        deleteAll()
        bulkInsert(resultsEntities)
    }

    fun replaceResults(resultsEntities: List<RoomChildEntity>): Completable {
        return Completable.fromAction { replaceResultsTransaction(resultsEntities) }
    }
}
