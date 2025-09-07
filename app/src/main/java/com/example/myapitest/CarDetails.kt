package com.example.myapitest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
    private val viewModel: CarViewModel by viewModels()

    private val editCarLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                // Update the car object itself
                car.apply {
                    id = intent.getStringExtra("CAR_ID") ?: id
                    name = intent.getStringExtra("CAR_NAME") ?: name
                    year = intent.getStringExtra("CAR_YEAR") ?: year
                    licence = intent.getStringExtra("CAR_LICENCE") ?: licence
                    imageUrl = intent.getStringExtra("CAR_IMAGE_URL") ?: imageUrl
                    place = Place(
                        intent.getDoubleExtra("CAR_LAT", place.getLat()),
                        intent.getDoubleExtra("CAR_LONG", place.getLong())
                    )
                }
                // Update the bindings from the car object
                binding.apply {
                    carName.text = car.name
                    carYear.text = car.year
                    carLicense.text = car.licence
                    carPlace.text = "Lat: ${car.place.getLat()}, Lng: ${car.place.getLong()}"
                    Picasso.get()
                        .load(car.imageUrl)
                        .placeholder(R.drawable.ic_add_photo)
                        .error(R.drawable.ic_add_photo)
                        .fit()
                        .centerCrop()
                        .into(carImage)
                }
            }
        }
    }


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
        setupActionBar()
        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(binding.mapContainer.id, mapFragment)
            .commit()
        mapFragment.getMapAsync(this)
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayShowHomeEnabled(false)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayShowCustomEnabled(false)
        supportActionBar?.title = getString(R.string.app_name)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.details_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit_button -> {
                val intent = Intent(this, EditCarActivity::class.java).apply {
                    putExtra("CAR_ID", car.id)
                    putExtra("CAR_IMAGE_URL", car.imageUrl)
                    putExtra("CAR_YEAR", car.year)
                    putExtra("CAR_NAME", car.name)
                    putExtra("CAR_LICENCE", car.licence)
                    putExtra("CAR_LAT", car.place.getLat())
                    putExtra("CAR_LONG", car.place.getLong())
                }
                editCarLauncher.launch(intent)
                true
            }
            R.id.action_delete_button -> {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Car")
                    .setMessage("Are you sure you want to delete ${car.name}?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        viewModel.deleteCar(car.id)
                        Toast.makeText(this, "${car.name} deleted", Toast.LENGTH_SHORT).show()
                        finish()
                        dialog.dismiss()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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