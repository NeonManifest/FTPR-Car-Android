package com.example.myapitest

class Place(private val lat: Double, private val long: Double) {
    fun getLat(): Double {
        return lat
    }
    fun getLong(): Double {
        return long
    }
}