package com.cresup.app.data.remote.firebase

import com.cresup.app.domain.model.Transaction
import com.cresup.app.domain.repository.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject

class FirestoreTransactionRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : TransactionRepository {

    private val uid get() = auth.currentUser?.uid.orEmpty()
    private val col get() = firestore.collection("users").document(uid).collection("transactions")

    override fun getAllTransactions(): Flow<List<Transaction>> = callbackFlow {
        val reg = col.orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.toTransaction() } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    override fun getTransactionsByMonth(year: Int, month: Int): Flow<List<Transaction>> = callbackFlow {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
        val end = cal.timeInMillis

        val reg = col
            .whereGreaterThanOrEqualTo("date", start)
            .whereLessThanOrEqualTo("date", end)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.toTransaction() } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    override suspend fun insert(transaction: Transaction) {
        val id = System.currentTimeMillis()
        val t = transaction.copy(id = id)
        col.document(id.toString()).set(t.toMap()).await()
    }

    override suspend fun update(transaction: Transaction) {
        col.document(transaction.id.toString()).set(transaction.toMap()).await()
    }

    override suspend fun delete(transaction: Transaction) {
        col.document(transaction.id.toString()).delete().await()
    }

    override suspend fun getById(id: Long): Transaction? {
        return col.document(id.toString()).get().await().toTransaction()
    }
}
