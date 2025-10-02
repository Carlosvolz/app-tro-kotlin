package com.example.myapplication

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import kotlin.jvm.java

class SplashActivity : AppCompatActivity() {


    private val SPLASH_DELAY: Long = 2500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val appTitle: TextView = findViewById(R.id.app_intro)


        appTitle.alpha = 0f
        appTitle.translationX = -200f
        appTitle.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(1500)
            .start()


        Handler(Looper.getMainLooper()).postDelayed({

            val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)


            val onboardingCompleted = sharedPref.getBoolean("onboarding_completed", false)


            val intent = if (onboardingCompleted) {

                Intent(this, MainActivity::class.java)
            } else {

                Intent(this, OnboardingActivity::class.java)
            }

            startActivity(intent)
            finish()

        }, SPLASH_DELAY)
    }
}