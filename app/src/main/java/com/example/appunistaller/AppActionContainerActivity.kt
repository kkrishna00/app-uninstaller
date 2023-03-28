package com.example.appunistaller

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.appunistaller.databinding.ActivityAppActionContainerLayoutBinding

class AppActionContainerActivity : AppCompatActivity() {


    private lateinit var binding: ActivityAppActionContainerLayoutBinding

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, AppActionContainerActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppActionContainerLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction().replace(binding.container.id, AppActionContainerFragment.newInstance("krishna","kumar")).commit()
    }
}