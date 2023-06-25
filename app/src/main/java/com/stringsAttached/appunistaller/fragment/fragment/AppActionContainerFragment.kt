package com.stringsAttached.appunistaller.fragment.fragment

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stringsAttached.appunistaller.fragment.adapter.ActionContainerAdapter
import com.stringsAttached.appunistaller.fragment.pojo.AppActionsContainer
import com.stringsAttached.appunistaller.fragment.pojo.AppController
import com.stringsAttached.appunistaller.fragment.activity.ScreenData
import com.example.appunistaller.databinding.FragmentAppActionContainerBinding
import java.text.DateFormat
import java.util.*


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
            val firstInstallTime = it.firstInstallTime
            val simple: DateFormat = DateFormat.getDateTimeInstance()
            val result = Date(firstInstallTime)
            binding.appInstallTime.text = simple.format(result)
            binding.ImageView.setImageDrawable(it.applicationInfo?.loadUnbadgedIcon(activity?.packageManager))
            binding.appName.text = activity?.packageManager?.let { it1 ->
                it.applicationInfo.loadLabel(it1).toString()
            }
            binding.appVersion.text = it.versionName
            binding.crossButton.setOnClickListener {
                finishActivity()
            }
        }
    }

    private fun initRecyclerView() {
        binding.actionListRv.apply {
            if (adapter == null) {
                adapter = ActionContainerAdapter(
                    dataSet = getListOfActions(), this@AppActionContainerFragment
                )
                layoutManager = LinearLayoutManager(
                    this@AppActionContainerFragment.context,
                    RecyclerView.VERTICAL,
                    false
                )
            }
        }
    }

    private fun getListOfActions(): List<AppActionsContainer> {
        val list = mutableListOf(
            AppActionsContainer(AppActionsContainer.ActionType.LAUNCH),
            AppActionsContainer(AppActionsContainer.ActionType.DETAILS),
            AppActionsContainer(AppActionsContainer.ActionType.UPDATE),
            AppActionsContainer(AppActionsContainer.ActionType.GO_TO_PLAY_STORE),
            AppActionsContainer(AppActionsContainer.ActionType.ADD_SHORTCUT),
        )

        if (screenData?.userApp == true) {
            list.add(2, AppActionsContainer(AppActionsContainer.ActionType.UNINSTALL))
        }
        return list
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
            Toast.makeText(activity, "Failed to open app details", Toast.LENGTH_SHORT).show()
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
                try {
                    startActivity(
                        activity?.packageManager?.getLaunchIntentForPackage(
                            packageName
                        )
                    )
                } catch (exception : NullPointerException) {
                    Toast.makeText(activity, "CANNOT LAUNCH THIS", Toast.LENGTH_SHORT).show()
                    exception.printStackTrace()
                }
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
        screenData?.packageInfo?.let {
            activity?.packageManager?.let { packageManager ->
                createAppShortCut(
                    context = requireContext(),
                    appName = it.applicationInfo.loadLabel(packageManager).toString(),
                    packageName = it.packageName,
                    icon = it.applicationInfo.loadIcon(packageManager)
                )
            }
        }
        finishActivity()
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        var bitmap: Bitmap? = null
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }
        bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            ) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun createAppShortCut(
        context: Context, appName: String?, packageName: String?, icon: Drawable
    ) {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            val shortcutInfoCompat = packageName?.let { pack ->
                activity?.packageManager?.getLaunchIntentForPackage(
                    pack
                )?.let { intent ->
                    ShortcutInfoCompat.Builder(requireContext(), pack)
                        .setIntent(Intent(Intent.EXTRA_SHORTCUT_INTENT, Uri.parse(packageName)))
                        .setIntent(intent).setShortLabel(appName!!)
                        .setIcon(drawableToBitmap(icon)?.let { IconCompat.createWithBitmap(it) })
                        .build()
                }
            }
            if (shortcutInfoCompat != null) {
                ShortcutManagerCompat.requestPinShortcut(context, shortcutInfoCompat, null)
            } else {
                Toast.makeText(activity, "CANNOT CREATE SHORTCUT", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "launcher does not support short cut icon", Toast.LENGTH_LONG)
                .show()
        }
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
                Toast.makeText(activity, "Failed to launch google Play", Toast.LENGTH_SHORT).show()
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