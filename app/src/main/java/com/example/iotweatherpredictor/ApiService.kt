package com.example.iotweatherpredictor

import retrofit2.Call
import retrofit2.http.GET
interface ApiService {
    @GET("0_161b469607c84a04a037505b8ebeaca3_1.json") //Json file name
    fun getSensorData(): Call<List<SensorData>>
}