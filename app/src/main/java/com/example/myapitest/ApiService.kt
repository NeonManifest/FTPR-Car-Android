package com.example.myapitest

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("car")
    suspend fun getCars(): List<Carro>

    @GET("car/{id}")
    suspend fun getCarById(@Path("id") id: String): Carro

    @POST("car")
    suspend fun createCar(@Body car: Carro): Carro

    @DELETE("car/{id}")
    suspend fun deleteCar(@Path("id") id: String)

    @PATCH("car/{id}")
    suspend fun updateCar(@Path("id") id: String, @Body car: Carro): Carro

}