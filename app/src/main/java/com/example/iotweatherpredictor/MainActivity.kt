package com.example.iotweatherpredictor

import android.content.res.AssetFileDescriptor
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        val service = retrofit.create(ApiService::class.java)       //Initialise Retrofit Service

        try {
            tflite = Interpreter(loadModelFile())       //Initialise TfLite Model
            predictBtn.setOnClickListener {
                var weatherData: Array<String>
                fetchData(service){weatherInfo ->        //Use Data inside the callback
                    weatherData = weatherInfo

                    if (weatherData.size == 3){     //Verify Data as per Requirement
                        updateUI(weatherData)        //Update UI
                    }
                    else{Toast.makeText(this,"Failed to load Data",Toast.LENGTH_SHORT).show()}
                }
            }

        } catch (ex: Exception) {       //Handle Initialization Error
            ex.printStackTrace()
            Toast.makeText(this, "Failed to initialize model: ${ex.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(weatherData: Array<String>){
        //Get Data
        val temperature = weatherData[0]
        val humidity = weatherData[1]
        val weather = weatherData[2]
        val weatherIcon = when(weather) {
            "Sunny" -> R.drawable.sunny
            "Cloudy" -> R.drawable.cloudy
            "Partly Cloudy" -> R.drawable.partly_cloudy
            "Rainy" -> R.drawable.rainy
            "Cold" -> R.drawable.cold
            else -> 0
        }

        // Update UI on Separate Thread
        runOnUiThread {
            temptv.text = temperature
            humidtv.text = humidity
            resultTv.text = "Predicted Weather: \n$weather"
            resultTv.setCompoundDrawablesRelativeWithIntrinsicBounds(0, weatherIcon, 0, 0)
        }
    }

    private fun fetchData(service: ApiService, callback: (Array<String>) -> Unit) {
        /*
        callback function returns a String Array
        FetchData is an Asynchronous Network Method,
        Values cannot be used outside the function due to Network Latency,
        We need to handle it inside the callback or in a subsequent asynchronous operation.
        */
        service.getSensorData().enqueue(object : Callback<List<SensorData>> {
            override fun onResponse(
                call: Call<List<SensorData>>,
                response: Response<List<SensorData>>
            ) {
                if (response.isSuccessful) {
                    val sensorDataList = response.body()
                    if (!sensorDataList.isNullOrEmpty()) {
                        val latestSensorData = sensorDataList.last()        //Get Last Inserted data from Web JSON file
                        val temperature = latestSensorData.temperature.toFloat()
                        val humidity = latestSensorData.humidity.toFloat()

                        val weather = predictWeather(temperature, humidity)     //Predict Weather

                        val weatherInfo = arrayOf(temperature.toString(),humidity.toString(),weather)

                        callback(weatherInfo)        //Return Values


                    }
                    //Handle Errors
                    else {
                        Log.d("SensorData", "Sensor data list is null or empty")
                        callback(arrayOf())
                    }
                } else {
                    Log.e("API Call", "Failed to fetch data: ${response.message()}")
                    callback(arrayOf())
                }
            }

            override fun onFailure(call: Call<List<SensorData>>, t: Throwable) {
                Log.e("API Call", "Failed to fetch data", t)
                callback(arrayOf())
            }
        })
    }

    private fun loadModelFile(): ByteBuffer {
        //Load TfLite Model from Assets Directory
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
        inputFeature0.put(floatArrayOf(temperatureC, humidityPer))

        byteBuffer.rewind()     //Rewind Cursor to reading next value

        val outputs = Array(1) { FloatArray(5) }        // Runs model inference and gets result.
        tflite.run(inputFeature0, outputs)

        val maxIndex = outputs[0].indices.maxByOrNull { outputs[0][it] } ?: -1      //Get Index Value for Predicted Weather
        val predictedClassIndex = if (maxIndex != -1) maxIndex else 0

        val weatherConditions =
            arrayOf("Cloudy", "Cold", "Rainy", "Sunny", "Partly Cloudy")        //Default Set of Weather Data
        val predictedWeather = weatherConditions[predictedClassIndex]
        Log.d("Weather", "Predicted: $predictedClassIndex")

        return predictedWeather     //Return Predicted Weather
    }

    override fun onDestroy() {
        super.onDestroy()
        tflite.close()
    }
}
