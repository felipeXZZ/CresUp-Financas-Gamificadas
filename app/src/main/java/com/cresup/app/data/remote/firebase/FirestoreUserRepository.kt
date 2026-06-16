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
    private val profilesCol get() = firestore.collection("publicProfiles")

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
        profilesCol.document(uid).set(
            mapOf(
                "uid" to uid,
                "name" to current.name,
                "userCode" to current.userCode,
                "level" to level,
                "levelName" to levelName,
                "xp" to newXp
            )
        ).await()
    }

    override suspend fun updateStreak() { }

    override suspend fun createDefaultUser() {
        val snap = userDoc.get().await()
        if (!snap.exists()) {
            val email = auth.currentUser?.email ?: ""
            val name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
            val code = generateUserCode()
            val user = User(name = name, userCode = code)
            userDoc.set(user.toMap()).await()
            profilesCol.document(uid).set(
                mapOf(
                    "uid" to uid,
                    "name" to name,
                    "userCode" to code,
                    "level" to 1,
                    "levelName" to "Poupador Iniciante",
                    "xp" to 0
                )
            ).await()
        }
    }

    private fun generateUserCode(): String {
        val chars = uid.filter { it.isLetterOrDigit() }.uppercase()
        val part = (chars.take(2) + chars.takeLast(2)).take(4).padEnd(4, '0')
        return "CRES-$part"
    }
}
