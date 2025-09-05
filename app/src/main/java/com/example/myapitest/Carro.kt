package com.example.myapitest

import java.time.Year

data class Carro(
    val id: String,
    val imageUrl: String,
    val year: Year,
    val name: String,
    val licence: String,
    val place: Place
)
