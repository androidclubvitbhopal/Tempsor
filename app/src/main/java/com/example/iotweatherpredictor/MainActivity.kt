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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {

    lateinit var predictBtn: Button
    lateinit var tempTv: TextView
    lateinit var humidTv: TextView
    lateinit var temptv: TextView
    lateinit var humidtv: TextView
    lateinit var resultTv: TextView
    private lateinit var tflite: Interpreter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        predictBtn = findViewById(R.id.predictBtn)
        temptv=findViewById(R.id.tempTV)
        humidtv=findViewById(R.id.humidTV)
        resultTv = findViewById(R.id.resultTV)

        val baseUrl = "https://sensor1data.blob.core.windows.net/onlinesensordata/"
        val accKey =
            "zin++eomthOe501JF2P7VJefVr646GhbuCMbqaMyMfdl59eH6n3fwIbGKzuzbnfyg61aRqE1cjsv+ASt5YbUjQ=="

        val retrofit = Retrofit.Builder()
            .baseUrl("$baseUrl?${accKey}")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(ApiService::class.java)

        try {
            tflite = Interpreter(loadModelFile())
            predictBtn.setOnClickListener {
                var values:FloatArray=fetchData(service)
                var temperatureC=values[0]
                var humidityPer=values[1]
                temptv.text= temperatureC.toString()
                humidtv.text=humidityPer.toString()
                resultTv.text = null
                val temp = temptv.text.toString()
                val humidity = humidtv.text.toString()

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

    private fun fetchData(service: ApiService): FloatArray {
        var tempFin=0.0f
        var humidFin=0.0f
        service.getSensorData().enqueue(object : Callback<List<SensorData>> {
            override fun onResponse(
                call: Call<List<SensorData>>,
                response: Response<List<SensorData>>
            ) {
                if (response.isSuccessful) {
                    val sensorDataList = response.body()
                    if (!sensorDataList.isNullOrEmpty()) {
                        val latestSensorData = sensorDataList[0]
                        val temperature = latestSensorData.temperature
                        val humidity = latestSensorData.humidity
                        tempFin=temperature.toFloat()
                        humidFin=humidity.toFloat()
                        Log.d(
                            "SensorData",
                            "Temperature: $temperature, Humidity: $humidity"
                        )
                        Log.d("SensorData", "Temperature: $tempFin, Humidity: $humidFin")

                    } else {
                        Log.d("SensorData", "Sensor data list is null or empty")
                    }

                } else {
                    Log.e("API Call", "Failed to fetch data: ${response.message()}")
                }

            }

            override fun onFailure(call: Call<List<SensorData>>, t: Throwable) {
                Log.e("API Call", "Failed to fetch data", t)
            }
        })
        return floatArrayOf(tempFin, humidFin)
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
        val inputFeature0 = byteBuffer.asFloatBuffer()
        inputFeature0.put(floatArrayOf(temp, humid))
        byteBuffer.rewind()
        // Creates inputs for reference.
        // val inputFeature0 =
        //     ByteBuffer.allocateDirect(1 * 2 * 4).apply {
        //         order(ByteOrder.nativeOrder())
        //     }.asFloatBuffer().apply {
        //         put(floatArrayOf(temperatureC, humidityPer))
        //     }

        // Runs model inference and gets result.
        val outputs = Array(1) { FloatArray(5) }
        tflite.run(inputFeature0, outputs)

        val maxIndex = outputs[0].indices.maxByOrNull { outputs[0][it] } ?: -1
        val predictedClassIndex = if (maxIndex != -1) maxIndex else 0

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
