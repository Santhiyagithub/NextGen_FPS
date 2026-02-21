package com.example.fpscamera

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class PreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        val videoUriString = intent.getStringExtra("VIDEO_URI")
        val fps = intent.getIntExtra("FPS", 0)
        val width = intent.getIntExtra("WIDTH", 0)
        val height = intent.getIntExtra("HEIGHT", 0)
        val iso = intent.getIntExtra("ISO", 0)
        val shutter = intent.getLongExtra("SHUTTER", 0)
        val bitrate = intent.getIntExtra("BITRATE", 0)

        val videoView = findViewById<VideoView>(R.id.previewVideoView)
        val detailsText = findViewById<TextView>(R.id.previewDetailsText)
        val backButton = findViewById<ImageButton>(R.id.backToCameraButton)

        if (videoUriString != null) {
            val uri = Uri.parse(videoUriString)
            videoView.setVideoURI(uri)
            videoView.setOnPreparedListener { it.isLooping = true }
            videoView.start()

            updateDetails(uri, fps, width, height, iso, shutter, bitrate, detailsText)
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun updateDetails(uri: Uri, fps: Int, width: Int, height: Int, iso: Int, shutter: Long, bitrate: Int, detailsText: TextView) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(this, uri)
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
            val durationSec = durationMs / 1000
            
            val details = StringBuilder()
            details.append("Selected FPS: $fps\n")
            details.append("Current FPS: $fps\n")
            details.append("Encoder: HEVC (H.265)\n")
            details.append("Resolution: ${width}x${height}\n")
            details.append("ISO: $iso\n")
            if (shutter > 0) {
                details.append("Shutter: 1/${1_000_000_000L / shutter}\n")
            } else {
                details.append("Shutter: AUTO\n")
            }
            details.append("Bitrate: ${bitrate / 1_000_000} Mbps\n")
            details.append("Duration: 00:${String.format("%02d", durationSec)}")
            
            detailsText.text = details.toString()
        } catch (e: Exception) {
            detailsText.text = "Error retrieving details"
        } finally {
            retriever.release()
        }
    }
}
