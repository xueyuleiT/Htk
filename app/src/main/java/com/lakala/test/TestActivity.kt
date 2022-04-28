package com.lakala.test

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import com.lakala.htk.HtkParams
import com.lakala.htk.HtkWebActivity
import com.lakala.test.databinding.ActivityTestBinding

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding
    private var isFullscreen: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.hide()

        isFullscreen = true
        binding.btnTest.setOnClickListener {
            val intent = Intent(this, HtkWebActivity::class.java)
            val htkParams = HtkParams()
            htkParams.title = "测试"
            htkParams.url = "https://tkfront.lakala.com/tk-account/index.html#/withdraw"
            htkParams.appType = "qtk"
            htkParams.channelId = "qtk"
//            htkParams.statusColor = R.color.black_3
//            htkParams.backColor = R.color.white
            htkParams.temporaryToken = binding.et.text.toString()
            val bundle = Bundle()
            bundle.putParcelable("params",htkParams)
            intent.putExtras(bundle)
            startActivity(intent)
        }

    }

}