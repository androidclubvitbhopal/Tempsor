# IoT Weather Predictor App

This Android app fetches real-time temperature and humidity data from IoT devices via Azure Blob Storage and predicts weather conditions using a machine learning model, implemented with TensorFlow Lite.

## Prerequisites
+ Your PC should have Android Studio installed.
+ An Android Phone (or you may install and test the app on a virtual device)

## Features

+ Predicts weather conditions based on temperature and humidity
+ Integrates TensorFlow Lite models on Android Devices
+ Provides visual feedback with weather icons for predicted conditions

## Installation

+ Clone this repository to your local machine using 'git clone'.
+ Open the project in Android Studio.
+ Build and run the application on an Android device or emulator.

## Usage

+ Enter the temperature and humidity values.
+ Tap the "Predict" button to fetch the latest sensor data.
+ The application will display the fetched temperature and humidity values and the predicted weather condition along with a corresponding weather icon.

## Libraries Used

+ TensorFlow Lite: For running machine learning models on Android.
+ Retrofit: For making network requests and handling REST API responses
+ Gson: For parsing JSON responses into Kotlin data classes
+ All other minor dependencies are specified in the [build.gradle file](https://github.com/adsmehra/IOT-Weather-Predictor/blob/main/app/build.gradle.kts).

## Dependencies 
### For TensorFlow

```
    implementation("org.tensorflow:tensorflow-lite-support:0.1.0")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.1.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.3.0")
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.google.code.gson:gson:2.8.8'
````

## Import TensorFlow Interpreter
```kotlin
    import org.tensorflow.lite.Interpreter
    import com.example.demo.ml.WeatherPredictor
    import retrofit2.Call
    import retrofit2.Callback
    import retrofit2.Response
    import retrofit2.Retrofit
    import retrofit2.converter.gson.GsonConverterFactory
```

# Weather-Predictor Model access
You can get the model from "asset" section or follow the link -> [Tflite file](https://github.com/adsmehra/IOT-Weather-Predictor/blob/main/app/src/main/assets/Weather_predictor.tflite)

## Flow diagram of the model
![flow iot](https://github.com/adsmehra/IOT-Weather-Predictor/assets/64251955/fa75ef1b-2c70-4941-b041-cf6c83e6696f)

# Code Snippets

## SensorData Class
+ The SensorData class defines the structure of sensor data fetched from Azure Blob Storage, including temperature, humidity, and IoT Hub details.
```kotlin
data class SensorData(
    val temperature: Double,
    val humidity: Double,
    val EventProcessedUtcTime: String,
    val PartitionId: Int,
    val EventEnqueuedUtcTime: String,
    val IoTHub: IoTHub
)
```
## IoTHubData Class
+ The IoTHub class contains IoT Hub-specific information like message and device IDs. 
```kotlin
data class IoTHub(
    val MessageId: String?,
    val CorrelationId: String?,
    val ConnectionDeviceId: String,
    val ConnectionDeviceGenerationId: String,
    val EnqueuedTime: String
)
```
## API Service Interface
+ This ApiService interface defines a Retrofit API for fetching sensor data from a JSON file hosted on a server.
+ It specifies a GET request to retrieve a list of SensorData objects from the specified JSON file.
```kotlin
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("0_161b469607c84a04a037505b8ebeaca3_1.json") //Json file name
    fun getSensorData(): Call<List<SensorData>>
}
```

## Main onCreate Method

Here, We:
+ Initialize our UI Components,
+ Create variables for our cloud IOT data,
+ We then provide this data to our TFLite file for processing,
+ Throughout, we use Exception Handling to make sure we avoid errors.

## Retrofit Setup
+ The following code initializes Retrofit with a base URL and an access key, creates a service for making API calls, and defines a method for fetching sensor data.
```kotlin
        val baseUrl = "https://sensor1data.blob.core.windows.net/onlinesensordata/"
        val accKey =
            "zin++eomthOe501JF2P7VJefVr646GhbuCMbqaMyMfdl59eH6n3fwIbGKzuzbnfyg61aRqE1cjsv+ASt5YbUjQ=="

        val retrofit = Retrofit.Builder()
            .baseUrl("$baseUrl?${accKey}")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(ApiService::class.java)
```

## Fetch Data fun
+ Makes an asynchronous API call using Retrofit's enqueue method.
+ Parses the response to extract the latest sensor data (temperature and humidity).
+ Returns a FloatArray containing the temperature and humidity values.

```kotlin
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

```
## Predict Weather fun
+ Takes temperature (in Celsius) and humidity (in percentage) as inputs.
+ Uses a TensorFlow Lite model to predict the weather condition.
+ Returns the predicted weather condition (Sunny, Cloudy, Partly Cloudy, Rainy, Cold).

```kotlin
    private fun predictWeather(temperatureC: Float, humidityPer: Float): String {
    val byteBuffer =
        ByteBuffer.allocateDirect(2 * 4) // Assuming 2 input features and 4 bytes per float
    byteBuffer.order(ByteOrder.nativeOrder())
    val inputFeature0 = byteBuffer.asFloatBuffer()
    inputFeature0.put(floatArrayOf(temperatureC, humidityPer))
    byteBuffer.rewind()

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


```


