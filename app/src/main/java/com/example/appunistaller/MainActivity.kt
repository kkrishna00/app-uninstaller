package com.example.appunistaller

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
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
        binding.recyclerView.apply {
            if(adapter == null) {
                adapter = DemoCollectionPagerAdapter(supportFragmentManager, getInstalledApps())
            }
        }
        binding.tabLayout.setupWithViewPager(binding.recyclerView)
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
                userPack.add(
                    PackageInfoContainer(
                        name = packageInfo.applicationInfo.loadLabel(packageManager).toString(),
                        packageInfo = packageInfo,
                        appVersion = packageInfo.versionName,
                        packageSize = File(packageInfo.applicationInfo.sourceDir).length()
                            .bytesToHuman()
                    )
                )
            } else {
                systemPack.add(
                    PackageInfoContainer(
                        name = packageInfo.applicationInfo.loadLabel(packageManager).toString(),
                        packageInfo = packageInfo,
                        appVersion = packageInfo.versionName,
                        packageSize = File(packageInfo.applicationInfo.sourceDir).length()
                            .bytesToHuman(),
                    )
                )
            }
        }
        return ViewPagerAdapterScreenData(
            systemApps = systemPack,
            userApps = userPack
        )
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        var bitmap: Bitmap? = null
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }
        bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            ) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
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
