package com.example.appunistaller.Utils

import android.os.Environment
import android.os.StatFs
import java.text.DecimalFormat

object MemoryStatus {
    private const val ERROR = -1
    private fun externalMemoryAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    val availableInternalMemorySize: Long
        get() {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            return availableBlocks * blockSize
        }

    val totalInternalMemorySize: Long
        get() {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            return totalBlocks * blockSize
        }

    val availableExternalMemorySize: Long
        get() = if (externalMemoryAvailable()) {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            availableBlocks * blockSize
        } else {
            ERROR.toLong()
        }

    val totalExternalMemorySize: Long
        get() {
            return if (externalMemoryAvailable()) {
                val path = Environment.getExternalStorageDirectory()
                val stat = StatFs(path.path)
                val blockSize = stat.blockSizeLong
                val totalBlocks = stat.blockCountLong
                totalBlocks * blockSize
            } else {
                ERROR.toLong()
            }
        }

    private fun floatForm(d: Double): String {
        return DecimalFormat("#.##").format(d)
    }

    fun Long.bytesToHuman(): String {
        val size = this
        val kb = (1 * 1024).toLong()
        val mb = kb * 1024
        val gb = mb * 1024
        val tb = gb * 1024
        val pb = tb * 1024
        val eb = pb * 1024
        if (size < kb) return floatForm(size.toDouble()) + " byte"
        if (size in kb until mb) return floatForm(size.toDouble() / kb) + " KB"
        if (size in mb until gb) return floatForm(size.toDouble() / mb) + " MB"
        if (size in gb until tb) return floatForm(size.toDouble() / gb) + " GB"
        if (size in tb until pb) return floatForm(size.toDouble() / tb) + " TB"
        if (size in pb until eb) return floatForm(size.toDouble() / pb) + " PB"
        return floatForm(size.toDouble() / eb) + " EB"
    }
}