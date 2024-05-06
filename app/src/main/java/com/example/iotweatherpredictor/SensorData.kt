package com.example.iotweatherpredictor

data class SensorData(
    val temperature: Double,
    val humidity: Double,
    val EventProcessedUtcTime: String,
    val PartitionId: Int,
    val EventEnqueuedUtcTime: String,
    val IoTHub: IoTHub
)
