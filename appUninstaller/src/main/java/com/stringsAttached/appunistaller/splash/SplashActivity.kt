package com.stringsAttached.appunistaller.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.stringsAttached.appunistaller.BuildConfig
import com.stringsAttached.appunistaller.R
import com.stringsAttached.appunistaller.activity.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setupStatusBar()
        lifecycleScope.launch {
            delay(800L)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        }
    }

    private fun setupStatusBar() {
        val view = findViewById<TextView>(R.id.appVersion)
        view.text = buildString {
            append("Build number - ")
            append(BuildConfig.VERSION_NAME)
        }
        window?.statusBarColor = Color.parseColor("#0096B1")
    }
}