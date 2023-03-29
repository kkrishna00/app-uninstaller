package com.example.appunistaller

import android.content.pm.PackageInfo
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appunistaller.databinding.FragmentAppContainerBinding
import kotlinx.parcelize.Parcelize

private const val SCREEN_DATA = "SCREEN_DATA"

class AppContainerFragment : Fragment(), MainActivityController {

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

    private var screenData: FragmentScreenData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            screenData = it.getParcelable(SCREEN_DATA)
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
            if(adapter == null) {
                adapter = screenData?.installedApps?.let { CustomAdapter(it, this@AppContainerFragment) }
                layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            }
        }
    }

    override fun handleActionButton(packageInfo: PackageInfo) {
        AppActionContainerActivity.startActivity(
            requireContext(),
            screenData = ScreenData(packageInfo)
        )
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


class DemoCollectionPagerAdapter(fm: FragmentManager, private val screenData: ViewPagerAdapterScreenData) :
    FragmentStatePagerAdapter(fm) {

    override fun getCount(): Int = 2

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                AppContainerFragment.newInstance(
                    screenData = FragmentScreenData(
                        installedApps = screenData.userApps,
                        userApp = true
                    )
                )
            }
            1 -> {
                AppContainerFragment.newInstance(
                    screenData = FragmentScreenData(
                        installedApps = screenData.systemApps,
                        userApp = false
                    )
                )
            }
            else -> {
                AppContainerFragment.newInstance(
                    screenData = FragmentScreenData(
                        installedApps = screenData.userApps,
                        userApp = true
                    )
                )
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> {
                "USER APPS"
            }
            1 -> {
                "SYSTEM APPS"
            }
            else -> {
                "INVALID"
            }
        }
    }
}
