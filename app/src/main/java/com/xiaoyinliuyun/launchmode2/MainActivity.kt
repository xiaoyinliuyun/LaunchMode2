package com.xiaoyinliuyun.launchmode2

import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.lang.Thread.sleep

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_LaunchMode2)
        setContentView(R.layout.activity_main)

        sleep(2000)
    }

    fun onFirst(view: android.view.View) {
        val intent = Intent(this, FirstActivity::class.java);
        startActivity(intent);
    }
}