package com.example.iotweatherpredictor

import android.content.res.AssetFileDescriptor
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.iotweatherpredictor.ml.WeatherPredictor
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {

    lateinit var predictBtn: Button
    lateinit var tempTv: TextView
    lateinit var humidTv: TextView
    lateinit var resultTv: TextView
    private lateinit var tflite: Interpreter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        predictBtn = findViewById(R.id.predictBtn)
        tempTv = findViewById(R.id.tempTV)
        humidTv = findViewById(R.id.humidTV)
        resultTv = findViewById(R.id.resultTV)

        try {
            tflite = Interpreter(loadModelFile())
            predictBtn.setOnClickListener {
                resultTv.text = null
                val temp = tempTv.text.toString()
                val humidity = tempTv.text.toString()

                if (temp.isEmpty() || humidity.isEmpty()) {
                    // Toast message indicating that the fields are empty
                    Toast.makeText(
                        this,
                        "Could not detect",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    try {
                        val temperatureC = temp.toFloat()
                        val humidityPer = humidity.toFloat()
                        val model = WeatherPredictor.newInstance(this)
                        val weather = predictWeather(temperatureC, humidityPer)
                        resultTv.text = "Predicted Weather: " + weather
                        var weatherLogo = 0
                        when (weather) {
                            "Sunny" -> weatherLogo = R.drawable.sunny
                            "Cloudy" -> weatherLogo = R.drawable.cloudy
                            "Partly Cloudy" -> weatherLogo = R.drawable.partly_cloudy
                            "Rainy" -> weatherLogo = R.drawable.rainy
                            "Cold" -> weatherLogo = R.drawable.cold
                        }
                        resultTv.setCompoundDrawablesWithIntrinsicBounds(0, weatherLogo, 0, 0)
                        // Releases model resources if no longer used.
                        model.close()
                    } catch (e: NumberFormatException) {
                        // Handles the case where the user entered a non-numeric value
                        Toast.makeText(
                            this,
                            "Invalid values for temperature and humidity",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
            Toast.makeText(this, "Failed to initialize model: ${ex.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun loadModelFile(): ByteBuffer {
        val fileDescriptor: AssetFileDescriptor = assets.openFd("Weather_predictor.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declareLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declareLength)
    }

    private fun predictWeather(temperatureC: Float, humidityPer: Float): String {
        val byteBuffer =
            ByteBuffer.allocateDirect(2 * 4) // Assuming 2 input features and 4 bytes per float
        byteBuffer.order(ByteOrder.nativeOrder())
        val floatBuffer = byteBuffer.asFloatBuffer()
        floatBuffer.put(floatArrayOf(temperatureC, humidityPer))
        byteBuffer.rewind()

        // Creates inputs for reference.
        val inputFeature0 =
            ByteBuffer.allocateDirect(1 * 2 * 4).apply {
                order(ByteOrder.nativeOrder())
            }.asFloatBuffer().apply {
                put(floatArrayOf(temperatureC, humidityPer))
            }

        // Runs model inference and gets result.
        val outputs = Array(1) { FloatArray(5) }
        tflite.run(inputFeature0, outputs)

        val maxIndex = outputs[0].indices.maxByOrNull { outputs[0][it] } ?: -1
        val predictedClassIndex = if (maxIndex != -1) maxIndex else 0

        //val predictedClassIndex = outputs[0].indexOf(outputs[0].maxOrNull() ?: 0f)
        val weatherConditions =
            arrayOf("Cloudy", "Cold", "Rainy", "Sunny", "Partly Cloudy")
        val predictedWeather = weatherConditions[predictedClassIndex]
        Log.d("Weather", "Predicted: $predictedClassIndex")

        return predictedWeather

    }

    override fun onDestroy() {
        super.onDestroy()
        tflite.close()
    }
}