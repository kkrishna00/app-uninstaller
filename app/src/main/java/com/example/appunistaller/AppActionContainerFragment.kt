package com.example.appunistaller

import android.content.pm.PackageInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appunistaller.databinding.FragmentAppActionContainerBinding

class AppActionContainerFragment : Fragment(), AppController {
    private var screenData: ScreenData? = null


    companion object {
        private const val SCREEN_DATA = "SCREEN_DATA"

        @JvmStatic
        fun newInstance(screenData: ScreenData) =
            AppActionContainerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(SCREEN_DATA, screenData)
                }
            }
    }

    private lateinit var _binding: FragmentAppActionContainerBinding

    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            screenData = it.getParcelable(SCREEN_DATA)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppActionContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        initData()
    }

    private fun initData() {
        screenData?.packageInfo?.let {
            binding.ImageView.setImageDrawable(it.applicationInfo?.loadUnbadgedIcon(activity?.packageManager))
            binding.appName.text = activity?.packageManager?.let { it1 ->
                it.applicationInfo.loadLabel(it1).toString()
            }
            binding.appVersion.text = it.versionName
        }
    }

    private fun initRecyclerView() {
        binding.actionListRv.apply {
            if (adapter == null) {
                adapter = ActionContainerAdapter(
                    dataSet = listOf(
                        AppActionsContainer(AppActionsContainer.ActionType.LAUNCH),
                        AppActionsContainer(AppActionsContainer.ActionType.DETAILS),
                        AppActionsContainer(AppActionsContainer.ActionType.UNINSTALL),
                        AppActionsContainer(AppActionsContainer.ActionType.GO_TO_PLAY_STORE),
                        AppActionsContainer(AppActionsContainer.ActionType.ADD_SHORTCUT),
                    ), this@AppActionContainerFragment
                )
                layoutManager = LinearLayoutManager(
                    this@AppActionContainerFragment.context,
                    RecyclerView.VERTICAL,
                    false
                )
            }
        }
    }

    override fun handleActionButton(packageInfo: PackageInfo) {
        TODO("Not yet implemented")
    }

    override fun uninstallApp() {
        TODO("Not yet implemented")
    }

    override fun getAppDetails() {
        TODO("Not yet implemented")
    }

}