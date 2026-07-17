package dev.stefano.enuventory.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dev.stefano.enuventory.domain.model.User
import dev.stefano.enuventory.domain.model.UserRole
import dev.stefano.enuventory.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        // Listener yang otomatis emit tiap kali auth state berubah (login/logout)
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                // Ambil role dari Firestore collection "users"
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        val roleString = doc.getString("role") ?: "user"
                        val role = if (roleString == "admin") UserRole.Admin else UserRole.RegularUser
                        trySend(
                            User(
                                uid = firebaseUser.uid,
                                name = doc.getString("name") ?: firebaseUser.displayName ?: "",
                                email = firebaseUser.email ?: "",
                                role = role,
                                disabled = doc.getBoolean("disabled") ?: false
                            )
                        )
                    }
                    .addOnFailureListener {
                        // Fallback: user ada tapi data Firestore gagal diambil
                        trySend(
                            User(
                                uid = firebaseUser.uid,
                                name = firebaseUser.displayName ?: "",
                                email = firebaseUser.email ?: "",
                                role = UserRole.RegularUser
                            )
                        )
                    }
            }
        }

        firebaseAuth.addAuthStateListener(listener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    override suspend fun signIn(email: String, password: String) {
        val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val uid = authResult.user?.uid ?: return

        // Akun yang dinonaktifkan admin (lihat UserRepositoryImpl.setUserDisabled) gak boleh
        // lanjut login -- Firebase Auth sendiri gak tahu soal ini, jadi dicek manual dari profil
        // Firestore-nya sebelum dianggap berhasil.
        val isDisabled = firestore.collection("users").document(uid).get().await()
            .getBoolean("disabled") ?: false
        if (isDisabled) {
            firebaseAuth.signOut()
            throw IllegalStateException("Akun kamu telah dinonaktifkan oleh admin.")
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}
