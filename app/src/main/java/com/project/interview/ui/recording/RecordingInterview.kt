package com.project.interview.ui.recording

import android.Manifest
import android.Manifest.permission.CAMERA
import android.Manifest.permission.RECORD_AUDIO
import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.project.interview.R
import com.project.interview.databinding.ActivityRecordingInterviewBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Timer
import java.util.concurrent.ExecutorService
import kotlin.concurrent.timer

class RecordingInterview : AppCompatActivity(){
    private lateinit var binding : ActivityRecordingInterviewBinding

    /**
     * Record
     */
    private var recording : Recording? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private lateinit var cameraExecutor: ExecutorService

    /**
     * Timer
     */
    private var timeTask : Timer? = null
    private var time = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordingInterviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.title.text = intent.getStringExtra("name")!!

        /**
         * 권한 설정
         */
        TedPermission.create()
            .setPermissionListener(permission)
            .setDeniedMessage("권한이 거부되었습니다. 설정 > 권한에서 허용해주세요.")
            .setPermissions(CAMERA,  RECORD_AUDIO)
            .check()

        binding.button.setOnClickListener {
            captureVideo()
        }
    }

    /**
     * Toolbar Menu
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return false
    }

    /**
     * Record
     */
    private fun startCamera(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.preview.surfaceProvider)
                }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider
                    .bindToLifecycle(this, cameraSelector, preview, videoCapture)
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureVideo(){
        val videoCapture = this.videoCapture ?: return

        //CameraX에서 요청 작업을 완료할 때까지 UI 사용 중지
        binding.button.isEnabled = false

        // 진행 중인 녹화가 있다면 중지
        val curRecording = recording
        if (curRecording != null) {
            // Stop the current recording session.
            curRecording.stop()
            recording = null
            return
        }

        // create and start a new recording session
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name) // 파일명
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Interview/${intent.getStringExtra("name")!!}") //저장 경로
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        recording = videoCapture.output
            .prepareRecording(this, mediaStoreOutputOptions)
            .apply {
                // 오디오 권한이 있다면 오디오 설정
                if (PermissionChecker.checkSelfPermission(this@RecordingInterview,
                        Manifest.permission.RECORD_AUDIO) ==
                    PermissionChecker.PERMISSION_GRANTED)
                {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when(recordEvent) {
                    // 녹화 시작
                    is VideoRecordEvent.Start -> {
                        startTimer()
                        binding.button.apply {
                            setImageResource(R.drawable.baseline_stop_circle_24)
                            isEnabled = true
                        }
                    }
                    // 녹화 종료
                    is VideoRecordEvent.Finalize -> {
                        stopTimer()
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded: " + "${recordEvent.outputResults.outputUri}"
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                            Log.d(TAG, msg)
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(TAG, "Video capture ends with error: " + "${recordEvent.error}")
                        }
                        binding.button.apply {
                            setImageResource(R.drawable.baseline_fiber_manual_record_24)
                            isEnabled = true
                        }
                        finish()
                    }
                }
            }
    }
    /**
     * Timer
     */
    private fun startTimer(){
        resetTimer()

        timeTask = timer(period = 1000){
            time++

            val m = time / 60
            val sec = time % 60

            runOnUiThread {
                binding.minute.text = "$m"
                binding.second.text = "$sec"
            }
        }
    }

    private fun stopTimer() = timeTask?.cancel()


    private fun resetTimer(){
        time = 0
        binding.minute.text = getString(R.string.zero)
        binding.second.text = getString(R.string.zero)
    }

    /**
     * 권한
     */
    private val permission = object : PermissionListener{
        override fun onPermissionGranted() {
            Toast.makeText(this@RecordingInterview, "권한 허가", Toast.LENGTH_SHORT).show()
            startCamera()
        }

        override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
            Toast.makeText(this@RecordingInterview, "권한 거부", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}