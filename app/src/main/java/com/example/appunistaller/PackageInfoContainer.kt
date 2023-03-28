package com.example.appunistaller

import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable
import android.os.Parcelable
import kotlinx.coroutines.internal.PrepareOp
import kotlinx.parcelize.Parcelize

data class PackageInfoContainer(val name: String?, val icon: Drawable?, val packageInfo: PackageInfo, val appVersion: String)

interface AppController {
    fun handleActionButton(packageInfo: PackageInfo)
    fun uninstallApp()
    fun getAppDetails()
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
    }

    @Parcelize
    enum class ActionType : Parcelable {
        DETAILS, UNINSTALL, GO_TO_PLAY_STORE, LAUNCH, ADD_SHORTCUT
    }
}