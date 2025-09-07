package com.example.myapitest

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.databinding.ActivityCarDetailsBinding
import com.squareup.picasso.Picasso

class CarDetails : AppCompatActivity() {

    private lateinit var car: Carro
    private lateinit var binding: ActivityCarDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        car = Carro(
            id = intent.getStringExtra("CAR_ID") ?: "",
            imageUrl = intent.getStringExtra("CAR_IMAGE_URL") ?: "",
            year = intent.getStringExtra("CAR_YEAR") ?: "",
            name = intent.getStringExtra("CAR_NAME") ?: "",
            licence = intent.getStringExtra("CAR_LICENCE") ?: "",
            place = Place(
                lat = intent.getDoubleExtra("CAR_LAT", 0.0),
                long = intent.getDoubleExtra("CAR_LONG", 0.0)
            )
        )

        // Setup views
        setupCarDetails()

    }

    private fun setupCarDetails() {
        binding.carName.text = car.name
        binding.carYear.text = car.year
        binding.carLicense.text = car.licence
        binding.carPlace.text = "${car.place.getLat()}, ${car.place.getLong()}"
        binding.carImage.contentDescription = car.name
        binding.carImage.scaleType = ImageView.ScaleType.CENTER_CROP
        binding.carImage.adjustViewBounds = true
        binding.carImage.maxHeight = 200
        binding.carImage.maxWidth = 200

        // Load image
        if (!car.imageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(car.imageUrl)
                .into(binding.carImage)
        }
    }

}