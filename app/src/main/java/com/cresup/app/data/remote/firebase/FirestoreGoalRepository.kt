package com.cresup.app.data.remote.firebase

import com.cresup.app.domain.model.Goal
import com.cresup.app.domain.repository.GoalRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreGoalRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : GoalRepository {

    private val uid get() = auth.currentUser?.uid.orEmpty()
    private val col get() = firestore.collection("users").document(uid).collection("goals")

    override fun getAllGoals(): Flow<List<Goal>> = callbackFlow {
        val reg = col.orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.map { it.toGoal() } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    override suspend fun insert(goal: Goal) {
        val id = System.currentTimeMillis()
        val g = goal.copy(id = id)
        col.document(id.toString()).set(g.toMap()).await()
    }

    override suspend fun update(goal: Goal) {
        col.document(goal.id.toString()).set(goal.toMap()).await()
    }

    override suspend fun delete(goal: Goal) {
        col.document(goal.id.toString()).delete().await()
    }

    override suspend fun addContribution(goalId: Long, amount: Double) {
        val snap = col.document(goalId.toString()).get().await()
        val goal = snap.toGoal()
        val newAmount = (goal.currentAmount + amount).coerceAtMost(goal.targetAmount)
        val isCompleted = newAmount >= goal.targetAmount
        col.document(goalId.toString()).update(
            mapOf(
                "currentAmount" to newAmount,
                "isCompleted" to isCompleted
            )
        ).await()
    }
}
