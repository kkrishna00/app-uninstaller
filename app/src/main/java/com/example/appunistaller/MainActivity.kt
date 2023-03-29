package com.example.appunistaller

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import androidx.appcompat.app.AppCompatActivity
import com.example.appunistaller.MemoryStatus.bytesToHuman
import com.example.appunistaller.databinding.ActivityMainBinding
import java.io.File
import java.text.DecimalFormat


class MainActivity : AppCompatActivity() {

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
        try {
            binding.recyclerView.apply {
                if (adapter == null) {
                    adapter = DemoCollectionPagerAdapter(supportFragmentManager, getInstalledApps())
                }
            }
            binding.tabLayout.setupWithViewPager(binding.recyclerView)
        } catch (exception: java.lang.Exception) {
            exception.printStackTrace()
        }
    }

    private fun getInstalledApps(): ViewPagerAdapterScreenData {

        val applicationsPack =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
            } else {
                packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            }

        val userPack = mutableListOf<PackageInfoContainer>()
        val systemPack = mutableListOf<PackageInfoContainer>()
        for (packageInfo in applicationsPack) {
            if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 1) {
                try {
                    userPack.add(
                        PackageInfoContainer(
                            name = packageInfo.applicationInfo.loadLabel(packageManager).toString(),
                            packageInfo = packageInfo,
                            appVersion = packageInfo.versionName,
                            packageSize = File(packageInfo.applicationInfo.sourceDir).length()
                                .bytesToHuman()
                        )
                    )
                } catch (exception: java.lang.Exception) {
                    exception.printStackTrace()
                }

            } else {
                try {
                    systemPack.add(
                        PackageInfoContainer(
                            name = packageInfo.applicationInfo.loadLabel(packageManager).toString(),
                            packageInfo = packageInfo,
                            appVersion = packageInfo.versionName,
                            packageSize = File(packageInfo.applicationInfo.sourceDir).length()
                                .bytesToHuman(),
                        )
                    )
                } catch (exception: java.lang.Exception) {
                    exception.printStackTrace()
                }

            }
        }
        binding.title.text = buildString {
            append(systemPack.size + userPack.size)
            append(" INSTALLED APPS")
        }
        return ViewPagerAdapterScreenData(
            systemApps = systemPack,
            userApps = userPack
        )
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
