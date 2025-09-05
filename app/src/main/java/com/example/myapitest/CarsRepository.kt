package com.example.myapitest

class CarsRepository(private val apiService: ApiService) {

    suspend fun getCars(): Result<List<Carro>> {
        return try {
            val users = apiService.getCars()
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCarById(id: String): Result<Carro> {
        return try {
            val user = apiService.getCarById(id)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCar(car: Carro): Result<Carro> {
        return try {
            val createdCar = apiService.createCar(car)
            Result.success(createdCar)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCar(id: String): Result<Unit> {
        return try {
            apiService.deleteCar(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCar(id: String, car: Carro): Result<Carro> {
        return try {
            val updatedCar = apiService.updateCar(id, car)
            Result.success(updatedCar)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}