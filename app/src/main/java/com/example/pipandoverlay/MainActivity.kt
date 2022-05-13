package com.example.pipandoverlay

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.pipandoverlay.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var overlayView: ViewGroup
    private var mWindowManager: WindowManager? = null
    private var isShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        overlayView = FrameLayout(this)
        overlayView.setLayoutParams(
            LinearLayout.LayoutParams(100, 100)
        )

        overlayView.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_200))

        mWindowManager = getSystemService(WINDOW_SERVICE) as? WindowManager

        binding.btnShowOverlay.setOnClickListener {
            if (isShowing) {
                removeView()
            } else {
                isShowing = true
                showOverlay()
            }

        }
    }

    private fun showOverlay() {
        val windowManagerParams = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
            else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            format = PixelFormat.TRANSPARENT
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.CENTER
        }

        LayoutInflater.from(this).inflate(R.layout.overlay_view, overlayView)

        val btn = overlayView.findViewById<Button>(R.id.btn_ClickMe)
        btn.setOnClickListener {
            removeView()
            Toast.makeText(this, "Iam clicked", Toast.LENGTH_SHORT).show()
        }

        val videoView = overlayView.findViewById<VideoView>(R.id.myVideoView)
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)
        videoView.setVideoURI(
            Uri.parse(
                "android.resource://" + packageName + "/" + R.raw.videoplayback
            )
        )
        videoView.start()

        mWindowManager?.addView(overlayView, windowManagerParams)
    }

    fun removeView() {
        if (isShowing) {
            isShowing = false
            mWindowManager?.removeView(overlayView)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val intent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val action = RemoteAction(
            Icon.createWithResource(
                this,
                R.drawable.ic_baseline_access_time_24
            ), "test", "test", intent
        )

        val actions = listOf(
            action, action, action, action, action, action, action, action
        )

        val pipParams =
            PictureInPictureParams.Builder()
                .setAspectRatio(Rational(2, 3))
                .setActions(actions)
                .build()

        enterPictureInPictureMode(pipParams)
    }
}