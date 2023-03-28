package com.example.appunistaller

import android.R
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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

    override fun uninstallApp() {
        val packageName = screenData?.packageInfo?.packageName
        try {
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        } catch (exception: ActivityNotFoundException) {
            exception.printStackTrace()
            Toast.makeText(requireContext(), "NO APP FOUND", Toast.LENGTH_SHORT).show()
        }
        finishActivity()
    }

    override fun getAppDetails() {
        val packageName = screenData?.packageInfo?.packageName

        try {
            //Open the specific App Info page:
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName");
            startActivity(intent);
        } catch (exception: ActivityNotFoundException) {
            exception.printStackTrace();
            //Open the generic Apps page:
            //  Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
            startActivity(Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS))
        }
        finishActivity()
    }

    private fun openApp() {
        val packageName = screenData?.packageInfo?.packageName
        packageName?.let {
            if (isAppInstalled(requireActivity(), packageName)) {
                startActivity(
                    activity?.packageManager?.getLaunchIntentForPackage(
                        packageName
                    )
                )
            } else {
                Toast.makeText(activity, "App not installed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isAppInstalled(activity: Activity, packageName: String): Boolean {
        val pm = activity.packageManager
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
        }
        return false
    }

    override fun createShortcutOnHomeScreen() {
        createShortCut()
        finishActivity()
    }

    fun createShortCut() {
        val appName = activity?.packageManager?.let {
            screenData?.packageInfo?.applicationInfo?.loadLabel(
                it
            )
        }
        val appIcon = activity?.packageManager?.let {
            screenData?.packageInfo?.applicationInfo?.loadIcon(
                it
            )
        }
        val packageName = screenData?.packageInfo?.packageName
        val shortcutIntent = Intent("com.android.launcher.action.INSTALL_SHORTCUT")
        shortcutIntent.putExtra("duplicate", false)
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName)
        val icon: Parcelable =
            Intent.ShortcutIconResource.fromContext(requireContext(), R.mipmap.sym_def_app_icon)
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon)
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, Intent(packageName))
        requireContext().sendBroadcast(shortcutIntent)
    }

    override fun searchOnGooglePlay() {
        val appPackageName = screenData?.packageInfo?.packageName

        appPackageName?.let {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$appPackageName")
                    )
                )
            } catch (exception: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                    )
                )
            }
        }
        finishActivity()
    }

    override fun launchApp() {
        openApp()
        finishActivity()
    }

    private fun finishActivity() {
        activity?.setResult(Activity.RESULT_OK)
        activity?.finish()
    }
}