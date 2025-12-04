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

// Servicio para manejar Firebase: Auth, Firestore y Storage
class FirebaseService {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()       // Instancia de Firebase Auth
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()       // Instancia de Firestore

    private val USERS_COLLECTION = "users"                            // Nombre colección de usuarios

    // ----------- AUTENTICACIÓN -----------

    // Registrar un usuario nuevo
    suspend fun registrarUsuario(username: String, email: String, password: String): Result<Unit> {
        return try {
            // Comprobar si el username ya existe
            val usernameCheck = db.collection(USERS_COLLECTION)
                .whereEqualTo("username", username)
                .get()
                .await()

            if (!usernameCheck.isEmpty) throw Exception("El nombre de usuario ya está en uso.")

            // Crear usuario en Auth
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("No se pudo obtener el ID del usuario.")

            // Crear documento en Firestore
            val user = User(id = uid, username = username, email = email)
            db.collection(USERS_COLLECTION).document(uid).set(user).await()

            Result.success(Unit)
        } catch (e: Exception) {
            val message = e.message ?: "Ha ocurrido un error."
            Result.failure(Exception(message))
        }
    }

    // Login de usuario
    suspend fun loginUser(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            val msg = e.message ?: "Correo o contraseña incorrectos."
            Result.failure(Exception(msg))
        }
    }

    // Enviar correo de recuperación
    suspend fun enviarCorreoRecuperacion(correo: String): Result<Unit> {
        return try {
            auth.setLanguageCode("es")  // Español
            auth.sendPasswordResetEmail(correo).await()
            Result.success(Unit)
        } catch (e: Exception) {
            val msg = e.message ?: "No se pudo enviar el correo de recuperación."
            Result.failure(Exception(msg))
        }
    }

    // Obtener usuario actual
    fun getCurrentUser() = auth.currentUser

    // Cerrar sesión
    fun logout() = auth.signOut()

    // ----------- LIGAS -----------

    // Añadir una liga nueva
    suspend fun addLiga(liga: Liga) {
        val ref = db.collection("ligas").document()
        val ligaConId = liga.copy(id = ref.id) // Asignar ID generado
        ref.set(ligaConId).await()
    }

    // Actualizar liga existente
    suspend fun updateLiga(liga: Liga) {
        db.collection("ligas").document(liga.id).set(liga).await()
    }

    // Eliminar liga
    suspend fun deleteLiga(id: String) {
        db.collection("ligas").document(id).delete().await()
    }

    // Obtener todas las ligas
    suspend fun obtenerLigas(): List<Liga> =
        try {
            db.collection("ligas").get().await().toObjects(Liga::class.java)
        } catch (e: Exception) { emptyList() }

    // ----------- EQUIPOS -----------

    // Generar ID para un equipo nuevo
    fun generateEquipoId(): String =
        db.collection("equipos").document().id

    // Añadir equipo
    suspend fun addEquipo(equipo: Equipo) {
        db.collection("equipos").document(equipo.id).set(equipo).await()
    }

    // Actualizar equipo
    suspend fun updateEquipo(equipo: Equipo) {
        db.collection("equipos").document(equipo.id).set(equipo).await()
    }

    // Eliminar equipo
    suspend fun deleteEquipo(id: String) {
        db.collection("equipos").document(id).delete().await()
    }

    // Obtener todos los equipos
    suspend fun obtenerEquipos(): List<Equipo> =
        try {
            db.collection("equipos").get().await().toObjects(Equipo::class.java)
        } catch (e: Exception) { emptyList() }

    // ----------- STORAGE -----------

    // Subir imagen a Storage y devolver URL
    suspend fun subirImagenAStorage(uri: Uri, carpeta: String): String {
        val storageRef = FirebaseStorage.getInstance().reference
        val nombreArchivo = "${UUID.randomUUID()}.jpg"
        val archivoRef = storageRef.child("$carpeta/$nombreArchivo")

        archivoRef.putFile(uri).await()                     // Subir archivo
        return archivoRef.downloadUrl.await().toString()    // Obtener URL
    }

    // ----------- PERFIL -----------

    // Obtener perfil del usuario actual
    suspend fun obtenerPerfilUsuario(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            db.collection(USERS_COLLECTION).document(uid).get().await()
                .toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Actualizar perfil del usuario
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

            // Actualizar datos en Firestore
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
