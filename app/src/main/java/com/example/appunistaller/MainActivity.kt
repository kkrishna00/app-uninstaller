package com.example.appunistaller

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appunistaller.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

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
                adapter = CustomAdapter(getInstalledApps())
                layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
            }
        }
    }

    private fun getInstalledApps(): List<PackageInfoContainer> {

        val applicationsPack =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
            } else {
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            }

        val pack = mutableListOf<PackageInfoContainer>()
        for (applications in applicationsPack) {
            if (applications.flags and ApplicationInfo.FLAG_SYSTEM != 1) {
                pack.add(
                    PackageInfoContainer(
                        name = applications.loadLabel(packageManager).toString(),
                        icon = applications.loadIcon(packageManager),
                    )
                )
            }
        }
        return pack
    }
}