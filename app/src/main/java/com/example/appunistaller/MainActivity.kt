package com.example.appunistaller

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appunistaller.MemoryStatus.bytesToHuman
import com.example.appunistaller.databinding.ActivityMainBinding
import java.text.DecimalFormat


class MainActivity : AppCompatActivity(), MainActivityController {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRv()
        setupMemoryStatus()
    }

    private fun setupMemoryStatus() {
        binding.memoryStatus.text =
            buildString {
                append("Free Memory: ")
                append(MemoryStatus.availableInternalMemorySize.bytesToHuman())
                append(" / ")
                append(MemoryStatus.totalInternalMemorySize.bytesToHuman())
            }
    }

    private fun setupRv() {
        binding.recyclerView.apply {
            if(adapter == null) {
                adapter = CustomAdapter(getInstalledApps(), this@MainActivity)
                layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
            }
        }
    }

    private fun getInstalledApps(): List<PackageInfoContainer> {

        val applicationsPack =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
            } else {
                packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            }

        val pack = mutableListOf<PackageInfoContainer>()
        for (packageInfo in applicationsPack) {
            if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 1) {
                pack.add(
                    PackageInfoContainer(
                        name = packageInfo.applicationInfo.loadLabel(packageManager).toString(),
                        icon = packageInfo.applicationInfo.loadUnbadgedIcon(packageManager),
                        packageInfo = packageInfo,
                        appVersion = packageInfo.versionName
                    )
                )
            }
        }
        return pack
    }

    override fun handleActionButton(packageInfo: PackageInfo) {
        AppActionContainerActivity.startActivity(this, screenData = ScreenData(packageInfo))
    }
}


object MemoryStatus {
    private const val ERROR = -1
    private fun externalMemoryAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    val availableInternalMemorySize: Long
        get() {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSize.toLong()
            val availableBlocks = stat.availableBlocks.toLong()
            return availableBlocks * blockSize
        }

    val totalInternalMemorySize: Long
        get() {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSize.toLong()
            val totalBlocks = stat.blockCount.toLong()
            return totalBlocks * blockSize
        }

    val availableExternalMemorySize: Long
        get() = if (externalMemoryAvailable()) {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSize.toLong()
            val availableBlocks = stat.availableBlocks.toLong()
            availableBlocks * blockSize
        } else {
            ERROR.toLong()
        }

    val totalExternalMemorySize: Long
        get() {
            return if (externalMemoryAvailable()) {
                val path = Environment.getExternalStorageDirectory()
                val stat = StatFs(path.path)
                val blockSize = stat.blockSize.toLong()
                val totalBlocks = stat.blockCount.toLong()
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
