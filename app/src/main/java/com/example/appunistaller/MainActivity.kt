package com.example.appunistaller

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appunistaller.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), MainActivityController {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRv()
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