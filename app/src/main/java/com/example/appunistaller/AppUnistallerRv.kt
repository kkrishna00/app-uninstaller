package com.example.appunistaller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appunistaller.databinding.AppActionContainerBinding
import com.example.appunistaller.databinding.AppInfoContainerBinding

class CustomAdapter(private val dataSet: List<PackageInfoContainer>, private val appController: AppController) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = AppInfoContainerBinding.bind(view)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.app_info_container, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.binding.appName.text = dataSet[position].name
        viewHolder.binding.appVersion.text = dataSet[position].appVersion
        viewHolder.binding.actionButton.setOnClickListener {
            appController.handleActionButton(dataSet[position].packageInfo)
        }

        dataSet[position].icon?.let { viewHolder.binding.ImageView.setImageDrawable(it) }
    }

    override fun getItemCount() = dataSet.size
}

class ActionContainerAdapter(private val dataSet: List<AppActionsContainer>, private val appController: AppController) :
    RecyclerView.Adapter<ActionContainerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = AppActionContainerBinding.bind(view)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.app_action_container, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val imageResource = when(dataSet[position].name) {
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
        }
        viewHolder.binding.actionImage.setImageResource(imageResource)
        viewHolder.binding.actionText.text = dataSet[position].getActionType()
    }

    override fun getItemCount() = dataSet.size
}