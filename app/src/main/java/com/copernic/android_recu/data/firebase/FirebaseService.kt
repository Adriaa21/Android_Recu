package com.copernic.android_recu.data.firebase

import com.copernic.android_recu.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseService {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val USERS_COLLECTION = "users"

    suspend fun registerUser(username: String, email: String, password: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("No se pudo obtener UID del usuario")

            // Verificar que el username no exista
            val usernameCheck = db.collection(USERS_COLLECTION)
                .whereEqualTo("username", username)
                .get()
                .await()

            if (!usernameCheck.isEmpty) {
                throw Exception("El nombre de usuario ya existe")
            }

            val user = User(id = uid, username = username, email = email)
            db.collection(USERS_COLLECTION).document(uid).set(user).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUser() = auth.currentUser

    fun logout() {
        auth.signOut()
    }
}