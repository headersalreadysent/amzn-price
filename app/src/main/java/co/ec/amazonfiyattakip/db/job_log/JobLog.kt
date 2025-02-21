package co.ec.amazonfiyattakip.db.job_log

import androidx.room.Entity
import androidx.room.PrimaryKey
import co.ec.helper.utils.unix

@Entity
data class JobLog(
    @PrimaryKey(autoGenerate = true) var id: Long=0,
    var asin: String = "",
    var date: Long = unix(),
    var detail: String = "",
)