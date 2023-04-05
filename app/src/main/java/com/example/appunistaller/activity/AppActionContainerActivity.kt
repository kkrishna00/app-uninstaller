package com.example.appunistaller.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import com.example.appunistaller.fragment.AppActionContainerFragment
import com.example.appunistaller.databinding.ActivityAppActionContainerLayoutBinding
import kotlinx.parcelize.Parcelize

class AppActionContainerActivity : AppCompatActivity() {


    private lateinit var binding: ActivityAppActionContainerLayoutBinding

    private val screenData: ScreenData by lazy(LazyThreadSafetyMode.NONE) {
        intent.extras?.getParcelable(SCREEN_DATA)!!
    }

    companion object {
        private const val SCREEN_DATA = "SCREEN_DATA"
        fun startActivity(context: Context, screenData: ScreenData) {
            val intent = Intent(context, AppActionContainerActivity::class.java).apply {
                val bundle = Bundle()
                bundle.putParcelable(SCREEN_DATA, screenData)
                putExtras(bundle)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppActionContainerLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction().replace(
            binding.container.id,
            AppActionContainerFragment.newInstance(screenData)
        ).commit()
    }
}

@Parcelize
data class ScreenData(val packageInfo: PackageInfo, val userApp: Boolean) : java.io.Serializable, Parcelable