package com.example.appunistaller

import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class PackageInfoContainer(
    val name: String?,
    val icon: Drawable?,
    val packageInfo: PackageInfo,
    val appVersion: String,
    val packageSize: String
)

interface MainActivityController {
    fun handleActionButton(packageInfo: PackageInfo)
}

@Parcelize
data class AppActionsContainer(
    val name: ActionType
) : Parcelable {

    fun getActionType(): String = when (name) {
        ActionType.ADD_SHORTCUT -> "Add Shortcut on HomeScreen"
        ActionType.DETAILS -> "Details"
        ActionType.UNINSTALL -> "Uninstall"
        ActionType.GO_TO_PLAY_STORE -> "Search on Google Play"
        ActionType.LAUNCH -> "Launch"
        ActionType.UPDATE -> "Update"
    }

    @Parcelize
    enum class ActionType : Parcelable {
        DETAILS, UNINSTALL, GO_TO_PLAY_STORE, LAUNCH, ADD_SHORTCUT, UPDATE
    }
}

interface AppController {
    fun uninstallApp()
    fun getAppDetails()
    fun createShortcutOnHomeScreen()
    fun searchOnGooglePlay()
    fun launchApp()
}