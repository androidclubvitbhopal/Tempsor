# IoT Weather Predictor App

Welcome new and full-of-energy developers for Android Development! This project is an IoT-controlled weather predictor Android application that utilizes TensorFlow Lite (TFLite) integrated models to predict weather conditions based on temperature and humidity inputs through IoT Devices.

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
+ Tap the "Predict" button to obtain the predicted weather condition.
+ View the predicted weather condition along with a corresponding weather icon.

## Major Dependencies

+ TensorFlow Lite: For running machine learning models on Android.
+ AndroidX: For modern Android development.
+ Kotlin: The programming language used for developing the application.

+ All other minor dependencies are specified in the [build.gradle file](https://github.com/adsmehra/IOT-Weather-Predictor/blob/main/app/build.gradle.kts).

## Dependencies 
### For TensorFlow

```
    implementation("org.tensorflow:tensorflow-lite-support:0.1.0")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.1.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.3.0")
````


### For Testing Framework in Java
```
     testImplementation("junit:junit:4.13.2")
````
This dependency refers to JUnit, a widely-used testing framework for Java. It enables developers to create and execute unit tests for their Java code. Unit tests are employed to evaluate specific components or functions within the codebase, ensuring their functionality aligns with expectations.


### For Testing Android in Java
```
     androidTestImplementation("androidx.test.ext:junit:1.1.5")
````
This dependency extends JUnit to cater specifically to Android testing needs, offering extra features and seamless integration with the Android testing environment. It empowers developers to author and execute JUnit tests within an Android project effortlessly.


### For testing framework for writing UI tests in Android applications
```
     androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
````
Espresso serves as a testing framework tailored for creating UI tests within Android apps. It enables developers to mimic user actions on the app's interface and confirm the app's proper functionality. This dependency encompasses the fundamental features of Espresso essential for crafting UI tests.

## Import TensorFlow Interpreter
```kotlin
import org.tensorflow.lite.Interpreter
```


# Weather-Predictor Model access
You can get the model from "asset" section or follow the link -> [Tflite file](https://github.com/adsmehra/IOT-Weather-Predictor/blob/main/app/src/main/assets/Weather_predictor.tflite)

## Flow diagram of the model
![flow iot](https://github.com/adsmehra/IOT-Weather-Predictor/assets/64251955/fa75ef1b-2c70-4941-b041-cf6c83e6696f)

# Code Snippets

## Main onCreate Method

Here, We:
+ Initialize our UI Components,
+ Create variables for our cloud IOT data,
+ We then provide this data to our TFLite file for processing,
+ Throughout, we use Exception Handling to make sure we avoid errors.
```
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
```


