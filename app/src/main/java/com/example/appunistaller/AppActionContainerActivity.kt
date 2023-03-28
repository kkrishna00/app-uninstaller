package com.example.appunistaller

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class AppActionContainerActivity : AppCompatActivity() {


    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, AppActionContainerActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_action_container_layout)

    }
}