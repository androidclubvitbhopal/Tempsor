package com.example.iotweatherpredictor

data class IoTHub(
    val MessageId: String?,
    val CorrelationId: String?,
    val ConnectionDeviceId: String,
    val ConnectionDeviceGenerationId: String,
    val EnqueuedTime: String
)
