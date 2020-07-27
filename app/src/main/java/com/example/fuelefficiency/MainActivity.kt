package com.example.fuelefficiency

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {

    val mean = floatArrayOf(5.477707f,
        195.318471f,
        104.869427f,
        2990.251592f,
        15.559236f,
        75.898089f,
        0.624204f,
        0.178344f,
        0.197452f
    )
    val std = floatArrayOf(
        1.699788f,
        104.331589f,
        38.096214f,
        843.898596f,
        2.789230f,
        3.675642f,
        0.485101f,
        0.383413f,
        0.398712f
    )

    lateinit var interpreter:Interpreter
    var org_int by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val org = arrayOf("USA", "Europe", "Japan")

        val arrayAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,org)
        origin.adapter= arrayAdapter

        origin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                org_int = p2
            }

        }

        try {
            interpreter = Interpreter(loadModelFile()!!)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        predict.setOnClickListener {
            val floats =
                Array(1) { FloatArray(9) }
            floats[0][0] =
                (Cylinder.getText().toString().toFloat() - mean[0]) / std[0]
            floats[0][1] =
                (Displacement.getText().toString().toFloat() - mean[1]) / std[1]
            floats[0][2] =
                (HorsePower.getText().toString().toFloat() - mean[2]) / std[2]
            floats[0][3] =
                (Weight.getText().toString().toFloat() - mean[3]) / std[3]
            floats[0][4] =
                (Acceleration.getText().toString().toFloat() - mean[4]) / std[4]
            floats[0][5] =
                (modelyear.getText().toString().toFloat() - mean[5]) / std[5]

            when(org_int) {
                0 -> {
                    floats[0][6] = (1 - mean[6]) / std[6]
                    floats[0][7] = (0 - mean[7]) / std[7]
                    floats[0][8] = (0 - mean[8]) / std[8]
                }
                1 -> {
                    floats[0][6] = (0 - mean[6]) / std[6]
                    floats[0][7] = (1 - mean[7]) / std[7]
                    floats[0][8] = (0 - mean[8]) / std[8]
                }
                2 -> {
                    floats[0][6] = (0 - mean[6]) / std[6]
                    floats[0][7] = (0 - mean[7]) / std[7]
                    floats[0][8] = (1 - mean[8]) / std[8]
                }
            }
            val res: Float = doInference(floats)
            val intent = Intent(this,result::class.java).apply {
                putExtra("result",res)
            }
            startActivity(intent)

        }
    }
    fun doInference(input: Array<FloatArray>): Float {
        val output = Array(1) { FloatArray(1) }
        interpreter.run(input, output)
        return output[0][0]
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer? {
        val assetFileDescriptor = this.assets.openFd("automobile.tflite")
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = fileInputStream.getChannel()
        val startOffset = assetFileDescriptor.startOffset
        val length = assetFileDescriptor.length
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, length)
    }
}