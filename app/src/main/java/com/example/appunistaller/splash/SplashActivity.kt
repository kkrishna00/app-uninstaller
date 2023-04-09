package com.example.appunistaller.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.appunistaller.R
import com.example.appunistaller.activity.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setupStatusBar()
        lifecycleScope.launch {
            delay(1500L)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        }
    }

    private fun setupStatusBar() {
        window?.statusBarColor = Color.parseColor("#0096B1")
    }
}