// app/src/main/java/com/example/musicplayer/MainActivity.kt
package com.ad.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ad.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.simpleName
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}