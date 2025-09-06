package com.example.myapitest

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CarViewModel: ViewModel() {
    private val apiService = RetrofitClient.apiService
    private val repository = CarsRepository(apiService)
    val cars = MutableLiveData<List<Carro>>()
    val error = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()

    fun fetchCars() {
        loading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getCars()
                result.fold(
                    onSuccess = { cars.value = it },
                    onFailure = { error.value = it.message }
                )
                loading.value = false
            } catch (e: Exception) {
                error.value = e.message
            }
        }
    }

}