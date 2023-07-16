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
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import com.stringsAttached.appunistaller.R
import com.stringsAttached.appunistaller.Utils.MemoryStatus
import com.stringsAttached.appunistaller.Utils.MemoryStatus.bytesToHuman
import com.stringsAttached.appunistaller.databinding.ActivityMainBinding
import com.stringsAttached.appunistaller.fragment.ViewPagerAdapterScreenData
import com.stringsAttached.appunistaller.pojo.AppActivityController
import com.stringsAttached.appunistaller.pojo.FilterType
import com.stringsAttached.appunistaller.pojo.PackageInfoContainer
import com.stringsAttached.appunistaller.viewPager.DemoCollectionPagerAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date


class MainActivity : AppCompatActivity(), AppActivityController {

    private lateinit var binding: ActivityMainBinding

    private var homeAdapter: DemoCollectionPagerAdapter? = null

    private var currentSortingType: FilterType = FilterType.DEFAULT

    private var currentQuery: String = ""

    private var listMap = mutableMapOf<String, PackageInfoContainer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRv()
        showInfo()
        supportActionBar?.hide()
        setupSortingViews()
        setSupportActionBar(binding.toolbar)
        setOverflowIcon()
        setupMemoryStatus()
        setupStatusBar()
        setupFabButtonListener()
        setupAd()
    }

    private fun setupAd() {
        MobileAds.initialize(this) {
            // no -op
        }

        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
        binding.adView.adListener = object : AdListener() {
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Code to be executed when an ad request fails.
            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        }
    }

    private fun setupSortingViews() {
        binding.sortingViewContainer.visibility = View.GONE
    }

    private fun setOverflowIcon() {
        val drawable = ContextCompat.getDrawable(
            applicationContext,
            R.drawable.baseline_sort_24
        )
        binding.toolbar.overflowIcon = drawable
    }

    private fun sortList(screenData: ViewPagerAdapterScreenData): ViewPagerAdapterScreenData {
        return when (currentSortingType) {
            FilterType.SORT_BY_NAME -> {
                sortByName(screenData)
            }

            FilterType.SORT_BY_DATE -> {
                sortByDate(screenData)
            }

            FilterType.SORT_BY_SIZE -> {
                sortBySize(screenData)
            }

            FilterType.DEFAULT -> {
                screenData
            }

            FilterType.SEARCH -> {
                filtererSearchList(screenData)
            }

            FilterType.SHOW_SELECTED_APPS -> {
                selectedApps(screenData)
            }
        }
    }

    private fun selectedApps(list: ViewPagerAdapterScreenData): ViewPagerAdapterScreenData {
        return list.copy(
            userApps = list.userApps.filter { packageInfoContainer ->
                listMap.contains(packageInfoContainer.packageInfo.packageName)
            },
            showActionButton = listMap.isEmpty()
        )
    }

     private fun filtererSearchList(list: ViewPagerAdapterScreenData): ViewPagerAdapterScreenData {
        return list.copy(
            userApps = list.userApps.filter { packageInfoContainer ->
                packageInfoContainer.name?.lowercase()?.contains(currentQuery) == true
            },
            systemApps = list.systemApps.filter { packageInfoContainer ->
                packageInfoContainer.name?.lowercase()?.contains(currentQuery) == true
            },
            showActionButton = listMap.isEmpty()
        )
    }

    private fun sortByName(data: ViewPagerAdapterScreenData): ViewPagerAdapterScreenData {
        return data.copy(
            userApps = data.userApps.sortedBy { it.name },
            systemApps = data.systemApps.sortedBy { it.name },
            showActionButton = listMap.isEmpty()
        )
    }

    private fun sortByDate(list: ViewPagerAdapterScreenData): ViewPagerAdapterScreenData {
        return list.copy(
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
    }

    private fun sortBySize(list: ViewPagerAdapterScreenData): ViewPagerAdapterScreenData {
        return list.copy(
            userApps = list.userApps.sortedBy { File(it.packageInfo.applicationInfo.sourceDir).length() },
            systemApps = list.systemApps.sortedBy { File(it.packageInfo.applicationInfo.sourceDir).length() },
            showActionButton = listMap.isEmpty()
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sortByName -> {

                currentSortingType = FilterType.SORT_BY_NAME
                homeAdapter?.updateData(getInstalledApps())
            }

            R.id.sortByDate -> {
                currentSortingType = FilterType.SORT_BY_DATE
                homeAdapter?.updateData(getInstalledApps())
            }

            R.id.sortBySize -> {
                currentSortingType = FilterType.SORT_BY_SIZE
                homeAdapter?.updateData(getInstalledApps())
            }

            R.id.selectedApps -> {
                if(listMap.isEmpty()) {
                    Toast.makeText(this, "No apps selected", Toast.LENGTH_SHORT).show()
                } else {
                    currentSortingType = FilterType.SHOW_SELECTED_APPS
                    homeAdapter?.updateData(getInstalledApps())
                }
            }

            R.id.showAll -> {
                currentSortingType = FilterType.DEFAULT
                homeAdapter?.updateData(getInstalledApps())
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
        var start = true
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                lifecycleScope.launch {
                    delay(300L)
                    currentQuery = query
                    if (query.length >= 3) {
                        start = false
                        currentSortingType = FilterType.SEARCH
                        filterList(query.lowercase())
                    } else if (query.isEmpty() && !start) {
                        start = false
                        currentSortingType = FilterType.DEFAULT
                        setupRv()
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                lifecycleScope.launch {
                    delay(300L)
                    currentQuery = newText
                    if (newText.length >= 3) {
                        start = false
                        currentSortingType = FilterType.SEARCH
                        filterList(newText.lowercase())
                    } else if (newText.isEmpty() && !start) {
                        start = false
                        setupRv()
                        currentSortingType = FilterType.DEFAULT
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
        return sortList(
            ViewPagerAdapterScreenData(
                systemApps = systemPack,
                userApps = userPack
            )
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
                currentSortingType = if(currentQuery.isEmpty()) {
                    FilterType.DEFAULT
                } else {
                    FilterType.SEARCH
                }
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

