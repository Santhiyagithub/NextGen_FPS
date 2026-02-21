package com.example.fpscamera

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.util.Range
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var textureView: TextureView
    private lateinit var recordButton: Button
    private lateinit var fpsSpinner: Spinner
    private lateinit var resSpinner: Spinner
    private lateinit var isoSeekBar: SeekBar
    private lateinit var shutterSeekBar: SeekBar
    private lateinit var isoStatusText: TextView
    private lateinit var shutterStatusText: TextView
    private lateinit var timerText: TextView
    private lateinit var controlsCard: View
    private lateinit var gridOverlay: View
    private lateinit var galleryButton: View
    private lateinit var recordingOverlay: View
    private lateinit var recordingInfoText: TextView
    private lateinit var settingsIcon: View
    private lateinit var autoIsoButton: Button
    private lateinit var autoShutterButton: Button
    private lateinit var previewButton: TextView

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var previewRequestBuilder: CaptureRequest.Builder? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    private lateinit var cameraManager: CameraManager
    private var cameraId: String = "0"
    private var characteristics: CameraCharacteristics? = null
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var videoContentUri: Uri? = null

    private var selectedFps = 30
    private var selectedSize = Size(1920, 1080)
    private var isoRange: Range<Int>? = null
    private var shutterRange: Range<Long>? = null

    private var isAutoIso = true
    private var isAutoShutter = true
    private var isControlsVisible = true

    // Last recorded video info
    private var lastIso: Int = 0
    private var lastShutter: Long = 0
    private var lastBitrate: Int = 0

    private var recordingStartTime: Long = 0
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            val millis = SystemClock.elapsedRealtime() - recordingStartTime
            val seconds = (millis / 1000).toInt()
            val minutes = seconds / 60
            val hours = minutes / 60
            timerText.text = String.format("%02d:%02d:%02d", hours % 24, minutes % 60, seconds % 60)
            timerHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        textureView = findViewById(R.id.textureView)
        recordButton = findViewById(R.id.recordButton)
        fpsSpinner = findViewById(R.id.fpsSpinner)
        resSpinner = findViewById(R.id.resSpinner)
        isoSeekBar = findViewById(R.id.isoSeekBar)
        shutterSeekBar = findViewById(R.id.shutterSeekBar)
        isoStatusText = findViewById(R.id.isoStatusText)
        shutterStatusText = findViewById(R.id.shutterStatusText)
        timerText = findViewById(R.id.timerText)
        controlsCard = findViewById(R.id.controlsCard)
        gridOverlay = findViewById(R.id.gridOverlay)
        galleryButton = findViewById(R.id.galleryButton)
        recordingOverlay = findViewById(R.id.recordingOverlay)
        recordingInfoText = findViewById(R.id.recordingInfoText)
        settingsIcon = findViewById(R.id.settingsIcon)
        autoIsoButton = findViewById(R.id.autoIsoButton)
        autoShutterButton = findViewById(R.id.autoShutterButton)
        previewButton = findViewById(R.id.previewButton)

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        setupSpinners()
        setupSeekBars()
        setupButtons()

        recordButton.setOnClickListener {
            if (isRecording) stopRecording() else startRecording()
        }

        galleryButton.setOnClickListener {
            openGallery()
        }

        settingsIcon.setOnClickListener {
            toggleControls()
        }

        previewButton.setOnClickListener {
            launchPreviewScreen()
        }

        if (allPermissionsGranted()) {
            textureView.surfaceTextureListener = surfaceTextureListener
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun launchPreviewScreen() {
        if (videoContentUri == null) {
            Toast.makeText(this, "No video recorded yet", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, PreviewActivity::class.java).apply {
            putExtra("VIDEO_URI", videoContentUri.toString())
            putExtra("FPS", selectedFps)
            putExtra("WIDTH", selectedSize.width)
            putExtra("HEIGHT", selectedSize.height)
            putExtra("ISO", lastIso)
            putExtra("SHUTTER", lastShutter)
            putExtra("BITRATE", lastBitrate)
        }
        startActivity(intent)
    }

    private fun setupButtons() {
        autoIsoButton.setOnClickListener {
            isAutoIso = !isAutoIso
            updateAutoButtonUI(autoIsoButton, isAutoIso)
            updateAeMode()
        }

        autoShutterButton.setOnClickListener {
            isAutoShutter = !isAutoShutter
            updateAutoButtonUI(autoShutterButton, isAutoShutter)
            updateAeMode()
        }
    }

    private fun updateAeMode() {
        if (isAutoIso && isAutoShutter) {
            previewRequestBuilder?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
        } else {
            previewRequestBuilder?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
        }
        updateManualSettings()
    }

    private fun updateAutoButtonUI(button: Button, isAuto: Boolean) {
        if (isAuto) {
            button.alpha = 1.0f
            button.setBackgroundColor(Color.parseColor("#FFD700"))
        } else {
            button.alpha = 0.5f
            button.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun toggleControls() {
        isControlsVisible = !isControlsVisible
        if (isControlsVisible) {
            controlsCard.visibility = View.VISIBLE
            controlsCard.animate().translationY(0f).alpha(1f).setDuration(300).start()
        } else {
            controlsCard.animate().translationY(controlsCard.height.toFloat() + 50).alpha(0f).setDuration(300).withEndAction {
                controlsCard.visibility = View.GONE
            }.start()
        }
    }

    private fun openGallery() {
        val onePlusPhotos = "com.oneplus.gallery"
        val intent = packageManager.getLaunchIntentForPackage(onePlusPhotos)
        
        if (intent != null) {
            startActivity(intent)
        } else {
            val fallback = Intent(Intent.ACTION_VIEW)
            fallback.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "vnd.android.cursor.dir/video")
            fallback.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            try {
                startActivity(fallback)
            } catch (e: Exception) {
                Toast.makeText(this, "Gallery app not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpinners() {
        val resOptions = listOf(Size(1920, 1080), Size(1280, 720), Size(640, 480))
        val resAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, resOptions.map { "${it.width}x${it.height}" })
        resSpinner.adapter = resAdapter
        resSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSize = resOptions[position]
                (view as? TextView)?.setTextColor(Color.WHITE)
                updateFpsOptions()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateFpsOptions() {
        try {
            val cameraIds = cameraManager.cameraIdList
            if (cameraIds.isEmpty()) return
            cameraId = cameraIds[0]
            characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val map = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: return
            
            val highSpeedFpsRanges = map.getHighSpeedVideoFpsRangesFor(selectedSize) ?: emptyArray()
            
            val fpsOptions = mutableListOf(30, 60)
            if (highSpeedFpsRanges.any { it.upper >= 120 }) fpsOptions.add(120)
            if (highSpeedFpsRanges.any { it.upper >= 240 }) fpsOptions.add(240)

            val fpsAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, fpsOptions.map { "${it} FPS" })
            fpsSpinner.adapter = fpsAdapter
            fpsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedFps = fpsOptions[position]
                    (view as? TextView)?.setTextColor(Color.WHITE)
                    updateShutterRangeForFps()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            isoRange = characteristics?.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
            shutterRange = characteristics?.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)

            isoSeekBar.max = (isoRange?.upper ?: 100) - (isoRange?.lower ?: 100)
            updateShutterRangeForFps()
            
        } catch (e: Exception) {
            Log.e("FPSCamera", "Error updating FPS options", e)
        }
    }

    private fun updateShutterRangeForFps() {
        val frameTimeNs = 1_000_000_000L / selectedFps
        val minShutter = shutterRange?.lower ?: 1000L
        val clampedMaxShutter = Math.min(shutterRange?.upper ?: frameTimeNs, frameTimeNs)
        shutterSeekBar.max = 100
    }

    private fun setupSeekBars() {
        isoSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    isAutoIso = false
                    updateAutoButtonUI(autoIsoButton, false)
                    updateManualSettings()
                }
                val iso = (isoRange?.lower ?: 100) + progress
                isoStatusText.text = "ISO $iso"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        shutterSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    isAutoShutter = false
                    updateAutoButtonUI(autoShutterButton, false)
                    updateManualSettings()
                }
                val frameTimeNs = 1_000_000_000L / selectedFps
                val min = shutterRange?.lower ?: 1000L
                val max = Math.min(shutterRange?.upper ?: frameTimeNs, frameTimeNs)
                
                val shutterTime = min + (progress.toDouble() / 100.0 * (max - min)).toLong()
                if (shutterTime > 0) {
                    shutterStatusText.text = "1/${1_000_000_000L / shutterTime}"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateManualSettings() {
        previewRequestBuilder?.let {
            if (isAutoIso && isAutoShutter) {
                it.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
            } else {
                it.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
                
                if (!isAutoIso) {
                    val iso = (isoRange?.lower ?: 100) + isoSeekBar.progress
                    it.set(CaptureRequest.SENSOR_SENSITIVITY, iso)
                }
                
                if (!isAutoShutter) {
                    val frameTimeNs = 1_000_000_000L / selectedFps
                    val min = shutterRange?.lower ?: 1000L
                    val max = Math.min(shutterRange?.upper ?: frameTimeNs, frameTimeNs)
                    val shutterTime = min + (shutterSeekBar.progress.toDouble() / 100.0 * (max - min)).toLong()
                    it.set(CaptureRequest.SENSOR_EXPOSURE_TIME, shutterTime)
                }
            }
            
            updateRepeatingRequest()
        }
    }

    private fun updateRepeatingRequest() {
        try {
            captureSession?.setRepeatingRequest(previewRequestBuilder!!.build(), captureCallback, backgroundHandler)
        } catch (e: Exception) {
            Log.e("FPSCamera", "Failed to update repeating request", e)
        }
    }

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
            super.onCaptureCompleted(session, request, result)
            
            val iso = result.get(CaptureResult.SENSOR_SENSITIVITY)
            val shutter = result.get(CaptureResult.SENSOR_EXPOSURE_TIME)
            
            runOnUiThread {
                if (isAutoIso && iso != null && !isoSeekBar.isPressed) {
                    isoStatusText.text = "ISO $iso"
                    isoSeekBar.progress = iso - (isoRange?.lower ?: 100)
                }
                if (isAutoShutter && shutter != null && shutter > 0 && !shutterSeekBar.isPressed) {
                    shutterStatusText.text = "1/${1_000_000_000L / shutter}"
                    val frameTimeNs = 1_000_000_000L / selectedFps
                    val min = shutterRange?.lower ?: 1000L
                    val max = Math.min(shutterRange?.upper ?: frameTimeNs, frameTimeNs)
                    if (max > min) {
                       shutterSeekBar.progress = ((shutter - min).toDouble() / (max - min) * 100).toInt()
                    }
                }
            }
        }
    }

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture) = true
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        startBackgroundThread()
        try {
            cameraManager.openCamera(cameraId, cameraStateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e("FPSCamera", "Cannot open camera", e)
        }
    }

    private val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            startPreview()
        }
        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            cameraDevice = null
        }
        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            cameraDevice = null
        }
    }

    private fun startPreview() {
        textureView.visibility = View.VISIBLE
        gridOverlay.visibility = View.VISIBLE
        val texture = textureView.surfaceTexture ?: return
        texture.setDefaultBufferSize(selectedSize.width, selectedSize.height)
        val surface = Surface(texture)

        try {
            previewRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder?.addTarget(surface)
            
            val outputs = listOf(OutputConfiguration(surface))
            val sessionConfig = SessionConfiguration(
                SessionConfiguration.SESSION_REGULAR,
                outputs,
                mainExecutor,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        updateRepeatingRequest()
                    }
                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.e("FPSCamera", "Preview configuration failed")
                    }
                }
            )
            cameraDevice?.createCaptureSession(sessionConfig)
        } catch (e: CameraAccessException) {
            Log.e("FPSCamera", "Failed to start preview", e)
        }
    }

    private fun startRecording() {
        if (cameraDevice == null) return

        try {
            captureSession?.stopRepeating()
            closePreviewSession()
            
            runOnUiThread {
                textureView.visibility = View.GONE
                gridOverlay.visibility = View.GONE
                controlsCard.visibility = View.GONE
                
                recordingOverlay.visibility = View.VISIBLE
                recordingOverlay.alpha = 0f
                recordingOverlay.animate().alpha(1f).setDuration(500).start()
                
                val blink = AlphaAnimation(0.4f, 1.0f)
                blink.duration = 800
                blink.repeatMode = Animation.REVERSE
                blink.repeatCount = Animation.INFINITE
                recordingInfoText.startAnimation(blink)
                
                setBrightness(0.01f)
                onRecordingStartedUI()
            }

            // Capture current settings for player details
            lastIso = (isoRange?.lower ?: 100) + isoSeekBar.progress
            val frameTimeNs = 1_000_000_000L / selectedFps
            val minS = shutterRange?.lower ?: 1000L
            val maxS = Math.min(shutterRange?.upper ?: frameTimeNs, frameTimeNs)
            lastShutter = minS + (shutterSeekBar.progress.toDouble() / 100.0 * (maxS - minS)).toLong()
            lastBitrate = when {
                selectedSize.width >= 1920 && selectedFps >= 240 -> 80_000_000
                selectedSize.width >= 1280 && selectedFps >= 240 -> 50_000_000
                selectedFps >= 120 -> 40_000_000
                else -> 20_000_000
            }

            setUpMediaRecorder()
            val recorderSurface = mediaRecorder?.surface ?: return
            val surfaces = listOf(recorderSurface)

            val recordRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)!!
            recordRequestBuilder.addTarget(recorderSurface)
            recordRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
            
            recordRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, lastIso)
            recordRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, lastShutter)

            if (selectedFps >= 120) {
                val fpsRange = Range(selectedFps, selectedFps)
                recordRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fpsRange)
            }

            val sessionCallback = object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    
                    try {
                        if (session is CameraConstrainedHighSpeedCaptureSession) {
                            val requestList = session.createHighSpeedRequestList(recordRequestBuilder.build())
                            session.setRepeatingBurst(requestList, null, backgroundHandler)
                        } else {
                            session.setRepeatingRequest(recordRequestBuilder.build(), null, backgroundHandler)
                        }
                        mediaRecorder?.start()
                    } catch (e: Exception) {
                        Log.e("FPSCamera", "Failed to start recording session", e)
                        runOnUiThread { stopRecording() }
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e("FPSCamera", "Recording session configuration failed")
                    runOnUiThread { stopRecording() }
                }
            }

            if (selectedFps >= 120) {
                cameraDevice?.createConstrainedHighSpeedCaptureSession(surfaces, sessionCallback, backgroundHandler)
            } else {
                val outputs = surfaces.map { OutputConfiguration(it) }
                val sessionConfig = SessionConfiguration(
                    SessionConfiguration.SESSION_REGULAR,
                    outputs,
                    mainExecutor,
                    sessionCallback
                )
                cameraDevice?.createCaptureSession(sessionConfig)
            }

        } catch (e: Exception) {
            Log.e("FPSCamera", "Error starting recording", e)
            stopRecording()
        }
    }

    private fun onRecordingStartedUI() {
        isRecording = true
        recordingStartTime = SystemClock.elapsedRealtime()
        timerHandler.post(timerRunnable)
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            Log.e("FPSCamera", "MediaRecorder stop failed", e)
        }
        mediaRecorder?.reset()
        
        isRecording = false
        timerHandler.removeCallbacks(timerRunnable)
        
        runOnUiThread {
            recordingInfoText.clearAnimation()

            recordingOverlay.animate().alpha(0f).setDuration(300).withEndAction {
                recordingOverlay.visibility = View.GONE
            }.start()
            
            setBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)
            timerText.text = "00:00:00"
            controlsCard.visibility = View.VISIBLE
            isControlsVisible = true
            Toast.makeText(this, "Video saved to Gallery", Toast.LENGTH_SHORT).show()
            startPreview()
        }
    }

    private fun setBrightness(value: Float) {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = value
        window.attributes = layoutParams
    }

    private fun setUpMediaRecorder() {
        val name = "VIDEO_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/FPSCamera")
        }

        videoContentUri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
        val fileDescriptor = contentResolver.openFileDescriptor(videoContentUri!!, "rw")?.fileDescriptor

        mediaRecorder = MediaRecorder(this).apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncoder(MediaRecorder.VideoEncoder.HEVC)
            setVideoSize(selectedSize.width, selectedSize.height)
            setVideoFrameRate(selectedFps)
            setCaptureRate(selectedFps.toDouble())
            setVideoEncodingBitRate(lastBitrate)
            setOrientationHint(90)
            setOutputFile(fileDescriptor)
            prepare()
        }
    }

    private fun closePreviewSession() {
        captureSession?.close()
        captureSession = null
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            Log.e("FPSCamera", "Interrupted", e)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS && allPermissionsGranted()) {
            textureView.surfaceTextureListener = surfaceTextureListener
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeCamera()
        stopBackgroundThread()
    }

    private fun closeCamera() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
        mediaRecorder?.release()
        mediaRecorder = null
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
}
