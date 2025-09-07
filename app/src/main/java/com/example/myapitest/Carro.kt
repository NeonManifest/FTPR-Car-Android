package com.example.myapitest

import java.time.Year

data class Carro(
    var id: String,
    var imageUrl: String,
    var year: String,
    var name: String,
    var licence: String,
    var place: Place
)
