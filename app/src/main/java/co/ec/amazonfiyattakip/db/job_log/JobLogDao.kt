package co.ec.amazonfiyattakip.db.job_log

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface JobLogDao {

    /**
     * add log to database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(log: JobLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(logs: List<JobLog>): List<Long>


    @Update()
    fun update(log: JobLog): Int

    @Query("SELECT * FROM joblog ORDER BY id DESC")
    fun getAll() : List<JobLog>


}