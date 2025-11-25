package com.copernic.android_recu.data.firebase

import android.net.Uri
import com.copernic.android_recu.model.Equipo
import com.copernic.android_recu.model.Liga
import com.copernic.android_recu.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseService {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val USERS_COLLECTION = "users"

    // ----------- AUTH -----------
    suspend fun registrarUsuario(username: String, email: String, password: String): Result<Unit> {
        return try {
            // 1️⃣ Comprobar si el username ya existe
            val usernameCheck = db.collection(USERS_COLLECTION)
                .whereEqualTo("username", username)
                .get()
                .await()

            if (!usernameCheck.isEmpty) throw Exception("El nombre de usuario ya está en uso.")

            // 2️⃣ Crear usuario en Auth
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("No se pudo obtener el ID del usuario.")

            // 3️⃣ Crear documento en Firestore
            val user = User(id = uid, username = username, email = email)
            db.collection(USERS_COLLECTION).document(uid).set(user).await()

            Result.success(Unit)

        } catch (e: Exception) {
            val message = e.message ?: "Ha ocurrido un error."
            Result.failure(Exception(message))
        }
    }

    suspend fun loginUser(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            val msg = e.message ?: "Correo o contraseña incorrectos."
            Result.failure(Exception(msg))
        }
    }

    suspend fun enviarCorreoRecuperacion(correo: String): Result<Unit> {
        return try {
            auth.setLanguageCode("es")
            auth.sendPasswordResetEmail(correo).await()
            Result.success(Unit)
        } catch (e: Exception) {
            val msg = e.message ?: "No se pudo enviar el correo de recuperación."
            Result.failure(Exception(msg))
        }
    }

    fun getCurrentUser() = auth.currentUser
    fun logout() = auth.signOut()

    // ----------- LIGAS -----------
    suspend fun addLiga(liga: Liga) {
        val ref = db.collection("ligas").document()
        val ligaConId = liga.copy(id = ref.id)
        ref.set(ligaConId).await()
    }

    suspend fun updateLiga(liga: Liga) {
        db.collection("ligas").document(liga.id).set(liga).await()
    }

    suspend fun deleteLiga(id: String) {
        db.collection("ligas").document(id).delete().await()
    }

    suspend fun obtenerLigas(): List<Liga> =
        try {
            db.collection("ligas").get().await().toObjects(Liga::class.java)
        } catch (e: Exception) { emptyList() }

    // ----------- EQUIPOS -----------
    fun generateEquipoId(): String =
        db.collection("equipos").document().id

    suspend fun addEquipo(equipo: Equipo) {
        db.collection("equipos").document(equipo.id).set(equipo).await()
    }

    suspend fun updateEquipo(equipo: Equipo) {
        db.collection("equipos").document(equipo.id).set(equipo).await()
    }

    suspend fun deleteEquipo(id: String) {
        db.collection("equipos").document(id).delete().await()
    }

    suspend fun obtenerEquipos(): List<Equipo> =
        try {
            db.collection("equipos").get().await().toObjects(Equipo::class.java)
        } catch (e: Exception) { emptyList() }

    // ----------- STORAGE -----------
    suspend fun subirImagenAStorage(uri: Uri, carpeta: String): String {
        val storageRef = FirebaseStorage.getInstance().reference
        val nombreArchivo = "${UUID.randomUUID()}.jpg"
        val archivoRef = storageRef.child("$carpeta/$nombreArchivo")

        archivoRef.putFile(uri).await()
        return archivoRef.downloadUrl.await().toString()
    }

    // ----------- PERFIL -----------

    suspend fun obtenerPerfilUsuario(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            db.collection(USERS_COLLECTION).document(uid).get().await()
                .toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun actualizarPerfil(username: String, email: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("No hay usuario logueado."))

        return try {
            // Comprobar si username ya existe en otro usuario
            val check = db.collection(USERS_COLLECTION)
                .whereEqualTo("username", username)
                .get()
                .await()

            if (!check.isEmpty && check.documents.first().id != uid) {
                return Result.failure(Exception("El nombre de usuario ya está en uso."))
            }

            // Actualizar email en Auth
            auth.currentUser!!.updateEmail(email).await()

            // Actualizar Firestore
            db.collection(USERS_COLLECTION).document(uid).update(
                mapOf(
                    "username" to username,
                    "email" to email
                )
            ).await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Error al actualizar perfil."))
        }
    }

}
