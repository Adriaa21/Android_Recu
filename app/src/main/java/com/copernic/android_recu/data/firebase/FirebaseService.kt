package com.copernic.android_recu.data.firebase

import com.copernic.android_recu.model.Equipo
import com.copernic.android_recu.model.Liga
import com.copernic.android_recu.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseService {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val USERS_COLLECTION = "users"

    suspend fun registrarUsuario(username: String, email: String, password: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("No se pudo obtener el ID del usuario.")

            val usernameCheck = db.collection(USERS_COLLECTION)
                .whereEqualTo("username", username)
                .get()
                .await()

            if (!usernameCheck.isEmpty) {
                throw Exception("El nombre de usuario ya está en uso.")
            }

            val user = User(id = uid, username = username, email = email)
            db.collection(USERS_COLLECTION).document(uid).set(user).await()

            Result.success(Unit)
        } catch (e: Exception) {
            val message = when {
                e.message?.contains("email address is already in use", true) == true ->
                    "Este correo ya está registrado."
                e.message?.contains("password", true) == true ->
                    "La contraseña no cumple los requisitos mínimos."
                else -> e.message ?: "Ha ocurrido un error inesperado."
            }
            Result.failure(Exception(message))
        }
    }

    suspend fun loginUser(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Correo o contraseña incorrectos."))
        }
    }

    suspend fun enviarCorreoRecuperacion(correo: String): Result<Unit> {
        return try {
            auth.setLanguageCode("es")
            auth.sendPasswordResetEmail(correo).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("No se pudo enviar el correo de recuperación."))
        }
    }

    fun getCurrentUser() = auth.currentUser

    fun logout() {
        auth.signOut()
    }

    // Guarda la liga y establece el id dentro del documento
    suspend fun addLiga(liga: Liga) {
        val docRef = db.collection("ligas").document()
        val ligaConId = liga.copy(id = docRef.id)
        docRef.set(ligaConId).await()
    }

    suspend fun obtenerLigas(): List<Liga> {
        return try {
            val snapshot = db.collection("ligas").get().await()
            snapshot.toObjects(Liga::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addEquipo(equipo: Equipo) {
        db.collection("equipos").document(equipo.id).set(equipo).await()
    }

}
