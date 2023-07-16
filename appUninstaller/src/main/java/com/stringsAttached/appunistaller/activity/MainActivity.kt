package com.stringsAttached.appunistaller.activity

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.stringsAttached.appunistaller.R
import com.stringsAttached.appunistaller.Utils.MemoryStatus
import com.stringsAttached.appunistaller.Utils.MemoryStatus.bytesToHuman
import com.stringsAttached.appunistaller.databinding.ActivityMainBinding
import com.stringsAttached.appunistaller.fragment.ViewPagerAdapterScreenData
import com.stringsAttached.appunistaller.pojo.AppActivityController
import com.stringsAttached.appunistaller.pojo.PackageInfoContainer
import com.stringsAttached.appunistaller.viewPager.DemoCollectionPagerAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.DateFormat
import java.util.Date


class MainActivity : AppCompatActivity(), AppActivityController {

    private lateinit var binding: ActivityMainBinding

    private var homeAdapter: DemoCollectionPagerAdapter? = null

    private var listMap = mutableMapOf<String, PackageInfoContainer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRv()
        showInfo()
        supportActionBar?.hide()
        setSupportActionBar(binding.toolbar)
        setOverflowIcon()
        setupMemoryStatus()
        setupStatusBar()
        setupFabButtonListener()
    }

    private fun setOverflowIcon() {
        val drawable = ContextCompat.getDrawable(
            applicationContext,
            R.drawable.baseline_sort_24
        )
        binding.toolbar.overflowIcon = drawable
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val list = getInstalledApps()
        when (item.itemId) {
            R.id.sortByName -> {

                val filteredList = list.copy(
                    userApps = list.userApps.sortedBy { it.name },
                    systemApps = list.systemApps.sortedBy { it.name },
                    showActionButton = listMap.isEmpty()
                )
                homeAdapter?.updateData(filteredList)
            }

            R.id.sortByDate -> {
                val filteredList = list.copy(
                    userApps = list.userApps.sortedBy {
                        val firstInstallTime = it.packageInfo.firstInstallTime
                        val result = Date(firstInstallTime)
                        result
                    },
                    systemApps = list.systemApps.sortedBy {
                        val firstInstallTime = it.packageInfo.firstInstallTime
                        val result = Date(firstInstallTime)
                        result
                    },
                    showActionButton = listMap.isEmpty()
                )
                homeAdapter?.updateData(filteredList)
            }

            R.id.sortBySize -> {
                val filteredList = list.copy(
                    userApps = list.userApps.sortedBy { File(it.packageInfo.applicationInfo.sourceDir).length() },
                    systemApps = list.systemApps.sortedBy { File(it.packageInfo.applicationInfo.sourceDir).length() },
                    showActionButton = listMap.isEmpty()
                )
                homeAdapter?.updateData(filteredList)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.sorting_menu, menu)
        val searchItem = menu.findItem(R.id.search)
        if (searchItem != null) {
            val searchView = MenuItemCompat.getActionView(searchItem) as SearchView
            setupSearch(searchView)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    private fun setupFabButtonListener() {
        binding.fabDeleteButton.visibility = View.GONE
        binding.fabDeleteButton.setOnClickListener {
            listMap.forEach { app ->
                uninstallApp(app.toPair().second.packageInfo)
            }
        }
    }

    private fun showInfo() {
        Snackbar.make(binding.root, "CANNOT UNINSTALL SYSTEM APPS", Snackbar.LENGTH_SHORT)
            .show()
    }

    private fun setupStatusBar() {
        window?.statusBarColor = Color.parseColor("#FF018786")
    }

    private fun setupSearch(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
                    } else if (newText.isEmpty()) {
                        setupRv()
                        hideSoftKeyboard(searchView)
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
            },
            showActionButton = listMap.isEmpty()
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
                    homeAdapter =
                        DemoCollectionPagerAdapter(supportFragmentManager, getInstalledApps())
                    adapter = homeAdapter
                } else {
                    (homeAdapter as DemoCollectionPagerAdapter).updateData(
                        screenData = getInstalledApps().copy(showActionButton = listMap.isEmpty())
                    )
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
                packageManager.getInstalledPackages(
                    PackageManager.PackageInfoFlags.of(
                        PackageManager.GET_META_DATA.toLong()
                    )
                )
            } else {
                packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            }

        val userPack = mutableListOf<PackageInfoContainer>()
        val systemPack = mutableListOf<PackageInfoContainer>()
        for (packageInfo in applicationsPack) {
            if (packageInfo.packageName == this@MainActivity.packageName) {
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
                                .bytesToHuman(),
                            isSelected = listMap.contains(packageInfo.packageName)
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
        if (listMap.isEmpty()) {
            binding.title.text = buildString {
                append(systemPack.size + userPack.size)
                append(" INSTALLED APPS")
            }
        } else {
            binding.title.text = buildString {
                append(listMap.size)
                append(" SELECTED APPS")
            }
        }
        return ViewPagerAdapterScreenData(
            systemApps = systemPack,
            userApps = userPack
        )
    }

    override fun handleCheckBoxClicked(isSelected: Boolean, packageInfo: PackageInfoContainer) {
        if (isSelected) {
            listMap[packageInfo.packageInfo.packageName] = packageInfo
            if (listMap.size == 1) {
                binding.fabDeleteButton.visibility = View.VISIBLE
                homeAdapter?.updateData(
                    screenData = getInstalledApps().copy(showActionButton = false)
                )
            }
            binding.title.text = buildString {
                append(listMap.size)
                append(" SELECTED APPS")
            }
        } else {
            listMap.remove(packageInfo.packageInfo.packageName)
            if (listMap.isEmpty()) {
                binding.fabDeleteButton.visibility = View.GONE
                homeAdapter?.updateData(
                    screenData = getInstalledApps().copy(showActionButton = true)
                )
                binding.title.text = buildString {
                    append(getInstalledApps().systemApps.size + getInstalledApps().userApps.size)
                    append(" INSTALLED APPS")
                }
            } else {
                binding.title.text = buildString {
                    append(listMap.size)
                    append(" SELECTED APPS")
                }
            }
        }
    }

    private fun uninstallApp(packageInfo: PackageInfo) {
        val packageName = packageInfo.packageName
        try {
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        } catch (exception: ActivityNotFoundException) {
            exception.printStackTrace()
            Toast.makeText(this, "NO APP FOUND", Toast.LENGTH_SHORT).show()
        }
    }
}

