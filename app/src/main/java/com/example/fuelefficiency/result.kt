package com.example.fuelefficiency

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_result.*

class result : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        val res = intent.getFloatExtra("result", 0.0F)
        result_view.setText("Approx MPG for given data is:"+res)
    }
}