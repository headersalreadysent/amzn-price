package co.ec.amazonfiyattakip.db.job_log

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import co.ec.amazonfiyattakip.ui.part.graph.MinuteSpanData

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

    @Query("SELECT (j.date - s.date) / 60 AS minuteSpan, " +
            "COUNT(*) AS count " +
            "FROM joblog j LEFT JOIN joblog s ON s.id = j.id-1 WHERE s.date IS NOT NULL " +
            "GROUP BY minuteSpan ORDER BY minuteSpan")
    fun getStat() : List<MinuteSpanData>


}