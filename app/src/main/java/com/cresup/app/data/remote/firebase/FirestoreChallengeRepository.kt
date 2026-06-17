package com.cresup.app.data.remote.firebase

import com.cresup.app.domain.model.Challenge
import com.cresup.app.domain.model.DefaultChallenges
import com.cresup.app.domain.repository.ChallengeRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreChallengeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ChallengeRepository {

    private val uid get() = auth.currentUser?.uid.orEmpty()
    private val col get() = firestore.collection("users").document(uid).collection("challenges")

    override fun getAllChallenges(): Flow<List<Challenge>> = callbackFlow {
        val reg = col.addSnapshotListener { snap, err ->
            if (err != null) { close(); return@addSnapshotListener }
            val list = snap?.documents?.map { it.toChallenge() } ?: emptyList()
            trySend(list.sortedBy { it.id })
        }
        awaitClose { reg.remove() }
    }

    override suspend fun insert(challenge: Challenge) {
        col.document(challenge.id.toString()).set(challenge.toMap()).await()
    }

    override suspend fun update(challenge: Challenge) {
        col.document(challenge.id.toString()).set(challenge.toMap()).await()
    }

    override suspend fun activateChallenge(id: Long) {
        col.document(id.toString()).update(
            mapOf(
                "isActive" to true,
                "startedAt" to System.currentTimeMillis()
            )
        ).await()
    }

    override suspend fun incrementProgress(id: Long): Boolean {
        val snap = col.document(id.toString()).get().await()
        val challenge = snap.toChallenge()
        val newProgress = (challenge.progressCurrent + 1).coerceAtMost(challenge.durationDays)
        val isCompleted = newProgress >= challenge.durationDays
        col.document(id.toString()).update(
            mapOf(
                "progressCurrent" to newProgress,
                "isCompleted" to isCompleted,
                "isActive" to !isCompleted
            )
        ).await()
        return isCompleted
    }

    override suspend fun seedDefaultChallenges() {
        val snap = col.get().await()
        if (snap.isEmpty) {
            DefaultChallenges.forEach { challenge ->
                col.document(challenge.id.toString()).set(challenge.toMap()).await()
            }
        }
    }
}
