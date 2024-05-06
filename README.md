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

```python
    implementation("org.tensorflow:tensorflow-lite-support:0.1.0")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.1.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.3.0")
````


### For Testing Framework in Java
```python
     testImplementation("junit:junit:4.13.2")
````
This dependency refers to JUnit, a widely-used testing framework for Java. It enables developers to create and execute unit tests for their Java code. Unit tests are employed to evaluate specific components or functions within the codebase, ensuring their functionality aligns with expectations.


### For Testing Android in Java
```python
     androidTestImplementation("androidx.test.ext:junit:1.1.5")
````
This dependency extends JUnit to cater specifically to Android testing needs, offering extra features and seamless integration with the Android testing environment. It empowers developers to author and execute JUnit tests within an Android project effortlessly.


### For testing framework for writing UI tests in Android applications
```python
     androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
````
Espresso serves as a testing framework tailored for creating UI tests within Android apps. It enables developers to mimic user actions on the app's interface and confirm the app's proper functionality. This dependency encompasses the fundamental features of Espresso essential for crafting UI tests.

## Import related to TensorFlow
```python
import org.tensorflow.lite.Interpreter
```


# Weather-Predictor Model access
You can get the model from "asset" section or follow the link -> https://github.com/adsmehra/IOT-Weather-Predictor/blob/main/app/src/main/assets/Weather_predictor.tflite
