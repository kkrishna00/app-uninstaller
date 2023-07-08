package com.stringsAttached.appunistaller.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.stringsAttached.appunistaller.*
import com.stringsAttached.appunistaller.databinding.AppActionContainerBinding
import com.stringsAttached.appunistaller.databinding.AppInfoContainerBinding
import com.stringsAttached.appunistaller.pojo.AppActionsContainer
import com.stringsAttached.appunistaller.pojo.AppController
import com.stringsAttached.appunistaller.pojo.AppActivityController
import com.stringsAttached.appunistaller.pojo.PackageInfoContainer

class CustomAdapter(
    private val dataSet: List<PackageInfoContainer>,
    private val appActivityController: AppActivityController,
    private val userApp: Boolean,
    private val showActionButton: Boolean = false
) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = AppInfoContainerBinding.bind(view)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.app_info_container, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val pm = viewHolder.binding.root.context.packageManager
        viewHolder.binding.appName.text = dataSet[position].name

        viewHolder.binding.appVersion.text = buildString {
            append(dataSet[position].packageSize)
            append(" \u2022 ")
            append(dataSet[position].appVersion)
        }
        if (showActionButton) {
            viewHolder.binding.actionButton.setOnClickListener {
                appActivityController.handleActionButton(dataSet[position].packageInfo)
            }
        } else {
            viewHolder.binding.actionButton.visibility = View.GONE
        }
        dataSet[position].packageInfo.applicationInfo.loadIcon(pm)
            ?.let { viewHolder.binding.ImageView.setImageDrawable(it) }

        if (userApp) {
            viewHolder.binding.radioButtonApp.isSelected = dataSet[position].isSelected
            viewHolder.binding.radioButtonApp.isChecked = dataSet[position].isSelected
            viewHolder.binding.root.setOnClickListener {
                val isSelected = viewHolder.binding.radioButtonApp.isSelected
                viewHolder.binding.radioButtonApp.isSelected = !isSelected
                viewHolder.binding.radioButtonApp.isChecked = !isSelected
                appActivityController.handleCheckBoxClicked(viewHolder.binding.radioButtonApp.isChecked, dataSet[position])
            }
        } else {
            viewHolder.binding.radioButtonApp.visibility = View.GONE
        }
    }

    override fun getItemCount() = dataSet.size
}

class ActionContainerAdapter(
    private val dataSet: List<AppActionsContainer>,
    private val appController: AppController
) :
    RecyclerView.Adapter<ActionContainerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = AppActionContainerBinding.bind(view)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.app_action_container, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val imageResource = when (dataSet[position].name) {
            AppActionsContainer.ActionType.UNINSTALL -> {
                R.mipmap.delete_icon
            }

            AppActionsContainer.ActionType.DETAILS -> {
                R.mipmap.info_icon
            }

            AppActionsContainer.ActionType.ADD_SHORTCUT -> {
                R.mipmap.shortcut_icon
            }

            AppActionsContainer.ActionType.GO_TO_PLAY_STORE -> {
                R.mipmap.search_icon
            }

            AppActionsContainer.ActionType.LAUNCH -> {
                R.mipmap.launch_icon
            }

            AppActionsContainer.ActionType.UPDATE -> {
                R.mipmap.update_icon
            }
        }

        viewHolder.binding.actionImage.setImageResource(imageResource)
        viewHolder.binding.actionText.text = dataSet[position].getActionType()

        viewHolder.binding.root.setOnClickListener {
            when (dataSet[position].name) {
                AppActionsContainer.ActionType.UNINSTALL -> {
                    appController.uninstallApp()
                }

                AppActionsContainer.ActionType.DETAILS -> {
                    appController.getAppDetails()
                }

                AppActionsContainer.ActionType.ADD_SHORTCUT -> {
                    appController.createShortcutOnHomeScreen()
                }

                AppActionsContainer.ActionType.GO_TO_PLAY_STORE -> {
                    appController.searchOnGooglePlay()
                }

                AppActionsContainer.ActionType.LAUNCH -> {
                    appController.launchApp()
                }

                AppActionsContainer.ActionType.UPDATE -> {
                    appController.searchOnGooglePlay()
                }
            }
        }
    }

    override fun getItemCount() = dataSet.size
}