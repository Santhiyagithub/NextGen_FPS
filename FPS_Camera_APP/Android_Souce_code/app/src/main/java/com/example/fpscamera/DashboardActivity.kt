package com.example.fpscamera

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        findViewById<CardView>(R.id.btnFpsCamera).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<CardView>(R.id.btnTemporalDetection).setOnClickListener {
            val intent = Intent(this, WebActivity::class.java)
            intent.putExtra("URL", "https://huggingface.co/spaces/yoga28v28/Video_temporal_error_detection")
            intent.putExtra("TITLE", "Temporal Error Detection")
            startActivity(intent)
        }
    }
}
