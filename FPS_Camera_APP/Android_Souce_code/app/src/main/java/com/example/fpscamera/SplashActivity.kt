package com.example.fpscamera

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.logo)
        val appName = findViewById<TextView>(R.id.appName)

        // Initial state
        logo.visibility = View.INVISIBLE
        appName.visibility = View.INVISIBLE

        // Animation for Logo: Scale up and Fade in
        val logoAnimation = AnimationSet(true).apply {
            addAnimation(ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f))
            addAnimation(AlphaAnimation(0.0f, 1.0f))
            duration = 1200
            interpolator = AccelerateDecelerateInterpolator()
            fillAfter = true
        }

        // Animation for Text: Fade in and Slide up slightly
        val textAnimation = AlphaAnimation(0.0f, 1.0f).apply {
            duration = 1000
            startOffset = 800
            fillAfter = true
        }

        Handler(Looper.getMainLooper()).postDelayed({
            logo.visibility = View.VISIBLE
            logo.startAnimation(logoAnimation)
            
            appName.visibility = View.VISIBLE
            appName.startAnimation(textAnimation)
        }, 300)

        // Navigate to DashboardActivity after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 3000)
    }
}
