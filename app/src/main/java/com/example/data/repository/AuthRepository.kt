package com.example.data.repository

import android.util.Log
import com.example.data.models.User
import com.example.data.models.UserRole
import com.example.data.network.RetrofitClient
import com.example.data.network.LoginRequest
import com.example.data.network.RegisterRequest
import com.example.data.network.UserUpdateRequest
import com.example.data.network.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthRepository {

    private val _currentUserState = MutableStateFlow<User?>(null)
    val currentUserState = _currentUserState.asStateFlow()
    
    private val _allUsersState = MutableStateFlow<List<User>>(emptyList())
    val allUsersState = _allUsersState.asStateFlow()

    init {
        checkCurrentAuthSession()
    }

    private fun checkCurrentAuthSession() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (RetrofitClient.authToken != null) {
                    val res = RetrofitClient.authApi.getMe()
                    if (res.isSuccessful && res.body() != null) {
                        _currentUserState.value = res.body()!!.toDomain()
                    } else {
                        logout()
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error checking session", e)
            }
        }
    }

    suspend fun register(
        nombre: String,
        email: String,
        telefono: String,
        pass: String,
        confirmPass: String,
        rol: UserRole
    ): Result<User> {
        if (pass != confirmPass) return Result.failure(Exception("Las contraseñas no coinciden"))
        return try {
            val req = RegisterRequest(
                fullName = nombre,
                email = email,
                password = pass,
                role = rol.value,
                phone = telefono,
                province = "Santo Domingo"
            )
            val res = RetrofitClient.authApi.register(req)
            if (res.isSuccessful && res.body() != null) {
                val tokenResp = res.body()!!
                RetrofitClient.authToken = tokenResp.accessToken
                val user = tokenResp.user.toDomain()
                _currentUserState.value = user
                Result.success(user)
            } else {
                Result.failure(Exception("Registration failed: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, pass: String): Result<User> {
        return try {
            val res = RetrofitClient.authApi.login(LoginRequest(email, pass))
            if (res.isSuccessful && res.body() != null) {
                val tokenResp = res.body()!!
                RetrofitClient.authToken = tokenResp.accessToken
                val user = tokenResp.user.toDomain()
                _currentUserState.value = user
                Result.success(user)
            } else {
                Result.failure(Exception("Login failed: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return Result.success(Unit) 
    }

    fun logout() {
        RetrofitClient.authToken = null
        _currentUserState.value = null
    }

    fun switchRoleForTesting(role: UserRole) {
        _currentUserState.value = _currentUserState.value?.copy(rol = role.value)
    }

    suspend fun updateUserProfile(updatedUser: User): Result<Unit> {
        return try {
            val req = UserUpdateRequest(
                fullName = updatedUser.nombre,
                phone = updatedUser.telefono,
                province = updatedUser.zona,
                description = updatedUser.descripcion
            )
            val res = RetrofitClient.authApi.updateProfile(req)
            if (res.isSuccessful) {
                _currentUserState.value = res.body()!!.toDomain()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Update failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(uid: String): User? {
        return _allUsersState.value.find { it.uid == uid } ?: _currentUserState.value
    }

    suspend fun submitVerificationRequest(
        idCardNumber: String,
        frontPhotoUrl: String = "",
        backPhotoUrl: String = "",
        infotepCourseName: String = "",
        infotepDocUrl: String = ""
    ): Result<Unit> {
        return Result.success(Unit) 
    }

    suspend fun approveWorkerVerification(uid: String): Result<Unit> {
        return Result.success(Unit)
    }

    suspend fun rejectWorkerVerification(uid: String): Result<Unit> {
        return Result.success(Unit)
    }
}
