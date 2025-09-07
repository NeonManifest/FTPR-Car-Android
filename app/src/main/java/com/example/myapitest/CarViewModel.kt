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

    fun createCar(car: Carro) {
        loading.value = true
        viewModelScope.launch {
            try {
                val result = repository.createCar(car)
                result.fold(
                    onSuccess = { cars.value = cars.value?.plus(it) },
                    onFailure = { error.value = it.message }
                )
            } catch (e: Exception) {
                loading.value = false
            }

        }
    }

    fun deleteCar(id: String) {
        loading.value = true
        viewModelScope.launch {
            try {
                val result = repository.deleteCar(id)
                result.fold(
                    onSuccess = { cars.value = cars.value?.minus(cars.value?.find { it.id == id }!!) },
                    onFailure = { error.value = it.message }
                )
            } catch (e: Exception) {
                loading.value = false
            }

        }
    }

    fun updateCar(id: String, car: Carro) {
        loading.value = true
        viewModelScope.launch {
            try {
                val result = repository.updateCar(id, car)
                result.fold(
                    onSuccess = { carro ->
                        cars.value = cars.value?.minus(cars.value?.find { it.id == id }!!)
                        cars.value = cars.value?.plus(carro) },
                    onFailure = { error.value = it.message }
                )
            } catch (e: Exception) {
                loading.value = false
            }
        }
    }

}