package com.stringsAttached.appuninstaller.fragment

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stringsAttached.appuninstaller.activity.AppActionContainerActivity
import com.stringsAttached.appuninstaller.activity.MainActivity
import com.stringsAttached.appuninstaller.activity.ScreenData
import com.stringsAttached.appuninstaller.adapter.CustomAdapter
import com.stringsAttached.appuninstaller.databinding.FragmentAppContainerBinding
import com.stringsAttached.appuninstaller.pojo.AppActivityController
import com.stringsAttached.appuninstaller.pojo.PackageInfoContainer
import kotlinx.parcelize.Parcelize

private const val SCREEN_DATA = "SCREEN_DATA"

class AppContainerFragment : Fragment(), AppActivityController {

    companion object {
        @JvmStatic
        fun newInstance(screenData: FragmentScreenData) =
            AppContainerFragment().apply {
                val bundle = Bundle().apply {
                    putParcelable(SCREEN_DATA, screenData)
                }
                arguments = bundle
            }
    }

    private lateinit var appActivityController: AppActivityController

    private var screenData: FragmentScreenData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            screenData = it.getParcelable(SCREEN_DATA)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            this.appActivityController = context
        }
    }

    private lateinit var _binding: FragmentAppContainerBinding

    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initRv()
    }

    private fun initRv() {
        binding.appContainerRv.apply {
            if (adapter == null) {
                adapter =
                    screenData?.installedApps?.let {
                        CustomAdapter(
                            dataSet = it,
                            appActivityController = this@AppContainerFragment,
                            userApp = screenData?.userApp ?: false
                        )
                    }
                layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            }
        }
    }

    override fun handleActionButton(packageInfo: PackageInfo) {
        AppActionContainerActivity.startActivity(
            requireContext(),
            screenData = ScreenData(packageInfo, screenData?.userApp ?: false)
        )
    }

    override fun handleCheckBoxClicked() {
        appActivityController.handleCheckBoxClicked()
    }
}

@Parcelize
data class ViewPagerAdapterScreenData(
    val userApps: List<PackageInfoContainer>,
    val systemApps: List<PackageInfoContainer>
) : java.io.Serializable, Parcelable

@Parcelize
data class FragmentScreenData(
    val installedApps: List<PackageInfoContainer>,
    val userApp: Boolean
) : java.io.Serializable, Parcelable


