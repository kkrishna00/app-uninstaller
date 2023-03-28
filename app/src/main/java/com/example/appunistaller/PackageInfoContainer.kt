package com.example.appunistaller

import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable

data class PackageInfoContainer(val name: String?, val icon: Drawable?, val packageInfo: PackageInfo, val appVersion: String)

interface AppController {
    fun handleActionButton(packageInfo: PackageInfo)
    fun uninstallApp()
    fun getAppDetails()
}