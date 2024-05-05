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

## Dependencies for Tensor

```python
    implementation("org.tensorflow:tensorflow-lite-support:0.1.0")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.1.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.3.0")
````
