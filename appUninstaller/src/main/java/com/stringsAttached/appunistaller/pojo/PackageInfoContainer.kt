package com.stringsAttached.appunistaller.pojo

import android.content.pm.PackageInfo
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PackageInfoContainer(
    val name: String?,
    val packageInfo: PackageInfo,
    val appVersion: String,
    val packageSize: String,
    val isSelected: Boolean = false,
) : Parcelable, java.io.Serializable

interface AppActivityController {
    fun handleActionButton(packageInfo: PackageInfo) {

    }

    fun handleCheckBoxClicked(isSelected: Boolean, packageInfo: PackageInfoContainer)
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

@Parcelize
enum class FilterType : Parcelable {
    SORT_BY_NAME, SORT_BY_DATE, SORT_BY_SIZE, DEFAULT, SEARCH, SHOW_SELECTED_APPS
}