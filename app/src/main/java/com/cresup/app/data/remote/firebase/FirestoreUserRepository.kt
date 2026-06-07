package com.cresup.app.data.remote.firebase

import com.cresup.app.domain.model.User
import com.cresup.app.domain.model.computeLevel
import com.cresup.app.domain.model.xpToNextLevel
import com.cresup.app.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserRepository {

    private val uid get() = auth.currentUser?.uid.orEmpty()
    private val userDoc get() = firestore.collection("users").document(uid)

    override fun getUser(): Flow<User> = callbackFlow {
        val reg = userDoc.addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            val user = if (snap != null && snap.exists()) {
                snap.toUser()
            } else {
                val email = auth.currentUser?.email ?: ""
                User(name = email.substringBefore("@").replaceFirstChar { it.uppercase() })
            }
            trySend(user)
        }
        awaitClose { reg.remove() }
    }

    override suspend fun updateUser(user: User) {
        userDoc.set(user.toMap()).await()
    }

    override suspend fun addXP(amount: Int) {
        val snap = userDoc.get().await()
        val current = if (snap.exists()) snap.toUser() else User()
        val newXp = current.xp + amount
        val (level, levelName) = computeLevel(newXp)
        userDoc.update(
            mapOf(
                "xp" to newXp,
                "level" to level,
                "levelName" to levelName,
                "xpToNextLevel" to xpToNextLevel(level)
            )
        ).await()
    }

    override suspend fun updateStreak() { }

    override suspend fun createDefaultUser() {
        val snap = userDoc.get().await()
        if (!snap.exists()) {
            val email = auth.currentUser?.email ?: ""
            val name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
            userDoc.set(User(name = name).toMap()).await()
        }
    }
}
