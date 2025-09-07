package com.example.myapitest

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.databinding.ActivityAddCarBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.UUID

class EditCarActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityAddCarBinding
    private lateinit var googleMap: GoogleMap

    private var selectedImageUri: Uri? = null
    private lateinit var currentCar: Carro
    private var selectedLocation: LatLng? = null

    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageReference: StorageReference = storage.reference
    private val viewModel: CarViewModel by viewModels()

    // Modern way to handle activity results
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let {
                selectedImageUri = it
                binding.imageViewCar.setImageURI(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentCar = Carro(
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

        populateFields()
        setupMap()
        setupClickListeners()
    }

    private fun populateFields() {
        binding.editTextName.setText(currentCar.name)
        binding.editTextYear.setText(currentCar.year)
        binding.editTextLicence.setText(currentCar.licence)
        Picasso.get()
            .load(currentCar.imageUrl)
            .fit()
            .centerCrop()
            .into(binding.imageViewCar)
        binding.textViewLocation.text = "Lat: ${currentCar.place.getLat()}, Lng: ${currentCar.place.getLong()}"
        selectedLocation = LatLng(currentCar.place.getLat(), currentCar.place.getLong())
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Show existing location
        selectedLocation?.let {
            googleMap.addMarker(MarkerOptions().position(it).title("Current Location"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }

        // Allow user to select new location
        googleMap.setOnMapClickListener { latLng ->
            selectedLocation = latLng
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
            binding.textViewLocation.text = "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}"
        }
    }

    private fun setupClickListeners() {
        binding.btnSelectImage.setOnClickListener {
            openImagePicker()
        }

        binding.btnSubmit.setOnClickListener {
            if (validateForm()) {
                uploadImageAndUpdateCar()
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

        if (name.isEmpty()) {
            binding.editTextName.error = "Car name is required"
            return false
        }

        if (year.isEmpty() || year.length != 4 || year.toIntOrNull() !in 1900..2100) {
            binding.editTextYear.error = "Please enter a valid year (1900-2100)"
            return false
        }

        if (licence.isEmpty()) {
            binding.editTextLicence.error = "Licence plate is required"
            return false
        }

        if (selectedLocation == null) {
            Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun uploadImageAndUpdateCar() {
        binding.btnSubmit.isEnabled = false

        if (selectedImageUri != null) {
            // User selected new image → upload it
            val imageRef = storageReference.child("car_images/${UUID.randomUUID()}.jpg")
            contentResolver.takePersistableUriPermission(
                selectedImageUri!!,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            val inputStream = contentResolver.openInputStream(selectedImageUri!!)
            inputStream?.use { stream ->
                imageRef.putStream(stream)
                    .addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            updateCarInApi(downloadUri.toString())
                        }
                    }
                    .addOnFailureListener { e ->
                        binding.btnSubmit.isEnabled = true
                        Toast.makeText(this, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            // No new image → keep current URL
            updateCarInApi(currentCar.imageUrl)
        }
    }

    private fun updateCarInApi(imageUrl: String) {
        val updatedCar = Carro(
            id = currentCar.id,
            imageUrl = imageUrl,
            year = binding.editTextYear.text.toString().trim(),
            name = binding.editTextName.text.toString().trim(),
            licence = binding.editTextLicence.text.toString().trim(),
            place = Place(selectedLocation!!.latitude, selectedLocation!!.longitude)
        )

        viewModel.updateCar(currentCar.id, updatedCar)
        Toast.makeText(this, "Car updated successfully!", Toast.LENGTH_SHORT).show()
        finishWithResult(updatedCar)
    }

    private fun finishWithResult(updatedCar: Carro) {
        val resultIntent = Intent().apply {
            putExtra("CAR_ID", updatedCar.id)
            putExtra("CAR_NAME", updatedCar.name)
            putExtra("CAR_YEAR", updatedCar.year)
            putExtra("CAR_LICENCE", updatedCar.licence)
            putExtra("CAR_IMAGE_URL", updatedCar.imageUrl)
            putExtra("CAR_LAT", updatedCar.place.getLat())
            putExtra("CAR_LONG", updatedCar.place.getLong())
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }


}
