package dev.stefano.enuventory.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dev.stefano.enuventory.di.SecondaryAuth
import dev.stefano.enuventory.domain.model.User
import dev.stefano.enuventory.domain.model.UserRole
import dev.stefano.enuventory.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    @SecondaryAuth private val secondaryAuth: FirebaseAuth
) : UserRepository {

    // Nama collection di Firestore -- sama dengan yang dipakai AuthRepositoryImpl buat profil user.
    private val usersCollection = firestore.collection("users")

    override fun getRegularUsers(): Flow<List<User>> = callbackFlow {
        val listener = usersCollection.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val users = snapshot.documents
                .mapNotNull { doc -> doc.toUser() }
                .filter { it.role == UserRole.RegularUser }
                // Sorted client-side (bukan Firestore .orderBy()) supaya gak butuh
                // composite index -- pola yang sama dipakai di repository lain.
                .sortedBy { it.name.lowercase() }
            trySend(users)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun createUser(name: String, email: String, password: String): User {
        val authResult = secondaryAuth.createUserWithEmailAndPassword(email, password).await()
        val uid = authResult.user?.uid
            ?: throw IllegalStateException("Gagal membuat akun: UID tidak ditemukan")

        usersCollection.document(uid).set(
            mapOf(
                "name" to name,
                "email" to email,
                "role" to "user",
                "disabled" to false
            )
        ).await()

        // Bersihin sesi di instance sekunder -- gak mempengaruhi sesi Admin di FirebaseAuth utama.
        secondaryAuth.signOut()

        return User(uid = uid, name = name, email = email, role = UserRole.RegularUser)
    }

    override suspend fun updateUserName(uid: String, name: String) {
        usersCollection.document(uid).update("name", name).await()
    }

    override suspend fun setUserDisabled(uid: String, disabled: Boolean) {
        usersCollection.document(uid).update("disabled", disabled).await()
    }

    // ── Private mapper helpers ──────────────────────────────────────────────

    private fun com.google.firebase.firestore.DocumentSnapshot.toUser(): User? {
        if (!exists()) return null
        return try {
            val roleString = getString("role") ?: "user"
            User(
                uid = id,
                name = getString("name") ?: "",
                email = getString("email") ?: "",
                role = if (roleString == "admin") UserRole.Admin else UserRole.RegularUser,
                disabled = getBoolean("disabled") ?: false
            )
        } catch (e: Exception) {
            null
        }
    }
}
