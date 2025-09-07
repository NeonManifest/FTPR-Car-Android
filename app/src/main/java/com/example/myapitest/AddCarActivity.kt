package com.example.myapitest

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myapitest.databinding.ActivityAddCarBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class AddCarActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityAddCarBinding
    private lateinit var googleMap: GoogleMap

    private var selectedImageUri: Uri? = null
    private var selectedLocation: LatLng? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageReference: StorageReference = storage.reference
    private val viewModel: CarViewModel by viewModels()

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                enableUserLocation()
            } else {
                Toast.makeText(this, getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }


    // Activity result launcher for picking an image
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                selectedImageUri = uri
                binding.imageViewCar.setImageURI(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupMap()
        setupClickListeners()
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Enable zoom controls
        googleMap.uiSettings.isZoomControlsEnabled = true

        // Check for location permission
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            enableUserLocation()
        } else {
            // Request permission
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        googleMap.setOnMapClickListener { latLng ->
            selectedLocation = latLng
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng).title(getString(R.string.title_selected_location)))
            binding.textViewLocation.text = getString(R.string.location_text, latLng.latitude, latLng.longitude)
        }
    }

    private fun setupClickListeners() {
        binding.btnSelectImage.setOnClickListener {
            openImagePicker()
        }

        binding.btnSubmit.setOnClickListener {
            if (validateForm()) {
                uploadImageAndSaveCar()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png", "image/jpg"))
        }
        imagePickerLauncher.launch(intent)
    }

    private fun validateForm(): Boolean {
        val name = binding.editTextName.text.toString().trim()
        val year = binding.editTextYear.text.toString().trim()
        val licence = binding.editTextLicence.text.toString().trim()

        if (selectedImageUri == null) {
            Toast.makeText(this, R.string.please_image, Toast.LENGTH_SHORT).show()
            return false
        }

        if (name.isEmpty()) {
            binding.editTextName.error = getString(R.string.please_car)
            return false
        }

        if (year.isEmpty() || year.length != 4 || year.toIntOrNull() !in 1900..2100) {
            binding.editTextYear.error = getString(R.string.please_year)
            return false
        }

        if (licence.isEmpty()) {
            binding.editTextLicence.error = getString(R.string.please_licence)
            return false
        }

        if (selectedLocation == null) {
            Toast.makeText(this, getString(R.string.please_location), Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun uploadImageAndSaveCar() {
        binding.btnSubmit.isEnabled = false

        val imageRef = storageReference.child("car_images/${UUID.randomUUID()}.jpg")
        selectedImageUri?.let { uri ->
            try {
                // Take persistable URI permission so the app can read the file later
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Ignore if already granted
            }

            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                imageRef.putStream(inputStream)
                    .addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            saveCarToDatabase(downloadUri.toString())
                        }
                    }
                    .addOnFailureListener { e ->
                        binding.btnSubmit.isEnabled = true
                        Toast.makeText(this, getString(R.string.upload_failed,e.message), Toast.LENGTH_SHORT).show()
                    }
            } else {
                binding.btnSubmit.isEnabled = true
                Toast.makeText(this, getString(R.string.image_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveCarToDatabase(imageUrl: String) {
        val name = binding.editTextName.text.toString().trim()
        val year = binding.editTextYear.text.toString().trim()
        val licence = binding.editTextLicence.text.toString().trim()
        val location = selectedLocation!!

        val car = Carro(
            id = UUID.randomUUID().toString(),
            imageUrl = imageUrl,
            year = year,
            name = name,
            licence = licence,
            place = Place(location.latitude, location.longitude)
        )

        saveCarToApi(car)
    }

    private fun saveCarToApi(car: Carro) {
        viewModel.createCar(car)
        Toast.makeText(this, getString(R.string.car_added), Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun enableUserLocation() {
        // Enable the blue "My Location" dot on the Google Map
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        googleMap.isMyLocationEnabled = true

        // Get the last known location from FusedLocationProviderClient
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val userLatLng = LatLng(it.latitude, it.longitude)
                selectedLocation = userLatLng

                // Clear previous markers
                googleMap.clear()

                // Add a marker at the user's current location
                googleMap.addMarker(
                    MarkerOptions()
                        .position(userLatLng)
                        .title(getString(R.string.your_location))
                )

                // Move the camera to user's location with zoom
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))

                // Update UI (optional)
                binding.textViewLocation.text = getString(R.string.location_text, it.latitude, it.longitude)
            }
        }
    }

}
