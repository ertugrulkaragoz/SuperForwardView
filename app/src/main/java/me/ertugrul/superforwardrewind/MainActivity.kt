package me.ertugrul.superforwardrewind

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import me.ertugrul.lib.Forward
import me.ertugrul.lib.OnAnimationEndListener
import me.ertugrul.lib.OnAnimationStartListener
import me.ertugrul.lib.Rewind

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val forward = findViewById<Forward>(R.id.forward)
        val rewind = findViewById<Rewind>(R.id.rewind)

        forward.onAnimationStart = {
            Log.e("onAnimationStart", "started")
        }
        forward.onAnimationEnd = {
            Log.e("onAnimationEnd", "ended")
        }
        forward.setOnAnimationStartListener(object : OnAnimationStartListener {
            override fun onAnimationStart() {
                Log.e("OnAnimationStartListenr", "started")
            }
        })
        forward.setOnAnimationEndListener(object : OnAnimationEndListener {
            override fun onAnimationEnd() {
                Log.e("OnAnimationEndListenr", "ended")
            }
        })


        rewind.onAnimationStart = {
            Log.e("onAnimationStart", "started")
        }
        rewind.onAnimationEnd = {
            Log.e("onAnimationEnd", "ended")
        }
        rewind.setOnAnimationStartListener(object : OnAnimationStartListener {
            override fun onAnimationStart() {
                Log.e("OnAnimationStartListenr", "started")
            }
        })
        rewind.setOnAnimationEndListener(object : OnAnimationEndListener {
            override fun onAnimationEnd() {
                Log.e("OnAnimationEndListenr", "ended")
            }
        })
    }
}
