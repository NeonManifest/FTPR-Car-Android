package com.example.myapitest

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.databinding.ActivityCarDetailsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import com.google.android.gms.maps.model.LatLng

class CarDetails : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var car: Carro
    private lateinit var binding: ActivityCarDetailsBinding
    private lateinit var map: GoogleMap

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
        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(binding.mapContainer.id, mapFragment)
            .commit()
        mapFragment.getMapAsync(this)
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

    override fun onMapReady(map: GoogleMap) {
        this.map = map

        // Example location (São Paulo)
        val carLocation = LatLng(car.place.getLat(), car.place.getLong())
        this.map.apply {
            addMarker(MarkerOptions().position(carLocation).title("Car Location"))
            moveCamera(CameraUpdateFactory.newLatLngZoom(carLocation, 14f))
        }
    }

}