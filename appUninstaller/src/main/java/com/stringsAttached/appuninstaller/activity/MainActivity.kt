package com.stringsAttached.appuninstaller.activity

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.stringsAttached.appuninstaller.Utils.MemoryStatus
import com.stringsAttached.appuninstaller.Utils.MemoryStatus.bytesToHuman
import com.stringsAttached.appuninstaller.databinding.ActivityMainBinding
import com.stringsAttached.appuninstaller.fragment.ViewPagerAdapterScreenData
import com.stringsAttached.appuninstaller.pojo.PackageInfoContainer
import com.stringsAttached.appuninstaller.viewPager.DemoCollectionPagerAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    private var homeAdapter: DemoCollectionPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRv()
        showInfo()
        supportActionBar?.hide()
        setSupportActionBar(binding.toolbar)
        setupMemoryStatus()
        setupSearch()
        setupStatusBar()
    }

    private fun showInfo() {
        Snackbar.make(binding.root, "CANNOT UNINSTALL SYSTEM APPS", Snackbar.LENGTH_SHORT)
            .show()
    }

    private fun setupStatusBar() {
       window?.statusBarColor = Color.parseColor("#FF018786")
    }

    private fun setupSearch() {
        binding.searchIcon.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                lifecycleScope.launch {
                    delay(300L)
                    if (query.length >= 3) {
                        filterList(query.lowercase())
                    } else if (query.isEmpty()) {
                        setupRv()
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                lifecycleScope.launch {
                    delay(300L)
                    if (newText.length >= 3) {
                        filterList(newText.lowercase())
                    } else if(newText.isEmpty()) {
                        setupRv()
                        hideSoftKeyboard(binding.searchIcon)
                    }
                }
                return false
            }
        })
    }

    private fun hideSoftKeyboard(view: View) {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun filterList(query: String) {
        val list = getInstalledApps()
        val filteredList = list.copy(
            userApps = list.userApps.filter { packageInfoContainer ->
                packageInfoContainer.name?.lowercase()?.contains(query) == true
            },
            systemApps = list.systemApps.filter { packageInfoContainer ->
                packageInfoContainer.name?.lowercase()?.contains(query) == true
            }
        )
        (homeAdapter as DemoCollectionPagerAdapter).updateData(filteredList)
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

    override fun onResume() {
        super.onResume()
        setupRv()
    }

    private fun setupRv() {
        try {
            binding.recyclerView.apply {
                if (homeAdapter == null) {
                    homeAdapter = DemoCollectionPagerAdapter(supportFragmentManager, getInstalledApps())
                    adapter = homeAdapter
                } else {
                    (homeAdapter as DemoCollectionPagerAdapter).updateData(getInstalledApps())
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
            if(packageInfo.packageName == this@MainActivity.packageName) {
                continue
            }

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

