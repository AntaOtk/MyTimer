package com.example.mytimer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_RESET = 3
        private const val DELAY = 1000L
    }

    private var inputEditText: EditText? = null
    private var startButton: Button? = null
    private var resetButton: Button? = null
    private var timeText: TextView? = null
    private var mainThreadHandler: Handler? = null
    private var timerState = STATE_DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inputEditText = findViewById(R.id.inputTime)
        startButton = findViewById(R.id.startTimerButton)
        timeText = findViewById(R.id.outputTime)
        resetButton = findViewById(R.id.resetTimerButton)

        mainThreadHandler = Handler(Looper.getMainLooper())
        resetButton?.isEnabled = false

        startButton?.setOnClickListener {
            val secondsCount =
                inputEditText?.text?.toString()?.takeIf { it.isNotBlank() }?.toLong() ?: 0L

            if (secondsCount <= 0) {
                showMessage("Can't start timer with no time!")
            } else {
                startTimer(secondsCount)
                startButton?.isEnabled = false
                resetButton?.isEnabled = true
            }
        }

        resetButton?.setOnClickListener {
            timerState = STATE_RESET
        }
    }

    private fun startTimer(duration: Long) {
        timerState = STATE_PLAYING
        val startTime = System.currentTimeMillis()
        mainThreadHandler?.post(
            createUpdateTimerTask(startTime, duration * DELAY)
        )
    }


    private fun showMessage(message: String) {
        val rootView = findViewById<View>(android.R.id.content)?.rootView
        if (rootView != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

    }

    private fun createUpdateTimerTask(startTime: Long, duration: Long): Runnable {
        return object : Runnable {
            override fun run() {
                val elapsedTime = System.currentTimeMillis() - startTime
                val remainingTime = duration - elapsedTime
                if (remainingTime <= 0) timerState = STATE_PREPARED
                when (timerState) {
                    STATE_PLAYING -> {
                        val seconds = remainingTime / DELAY
                        timeText?.text = String.format("%d:%02d", seconds / 60, seconds % 60)
                        mainThreadHandler?.postDelayed(this, DELAY)
                    }
                    STATE_RESET -> {
                        mainThreadHandler?.removeCallbacks(this)
                        startButton?.isEnabled = true
                        resetButton?.isEnabled = false
                        timeText?.text = ""
                    }
                    STATE_PREPARED -> {
                        mainThreadHandler?.removeCallbacks(this)
                        timeText?.text = "Done!"
                        startButton?.isEnabled = true
                        resetButton?.isEnabled = false
                        showMessage("Done!")
                    }

                }

            }
        }

    }
}
