package com.cresup.app.data.remote.firebase

import com.cresup.app.domain.model.FeedItem
import com.cresup.app.domain.model.FeedItemType
import com.cresup.app.domain.model.PublicProfile
import com.cresup.app.domain.repository.SocialRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreSocialRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : SocialRepository {

    private val uid get() = auth.currentUser?.uid.orEmpty()
    private val friendsCol get() = firestore.collection("users").document(uid).collection("friends")
    private val feedCol get() = firestore.collection("users").document(uid).collection("feed")
    private val profilesCol get() = firestore.collection("publicProfiles")

    override fun getFriends(): Flow<List<PublicProfile>> = callbackFlow {
        val reg = friendsCol.addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            val list = snap?.documents?.mapNotNull { doc ->
                runCatching {
                    PublicProfile(
                        uid = doc.getString("uid") ?: return@mapNotNull null,
                        name = doc.getString("name") ?: "",
                        userCode = doc.getString("userCode") ?: "",
                        level = doc.getLong("level")?.toInt() ?: 1,
                        levelName = doc.getString("levelName") ?: "Poupador Iniciante",
                        xp = doc.getLong("xp")?.toInt() ?: 0
                    )
                }.getOrNull()
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { reg.remove() }
    }

    override fun getFeed(): Flow<List<FeedItem>> = callbackFlow {
        val reg = feedCol
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { doc ->
                    runCatching {
                        FeedItem(
                            id = doc.id,
                            type = runCatching {
                                FeedItemType.valueOf(doc.getString("type") ?: "ACHIEVEMENT")
                            }.getOrDefault(FeedItemType.ACHIEVEMENT),
                            actorName = doc.getString("actorName") ?: "",
                            message = doc.getString("message") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    }.getOrNull()
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    override suspend fun searchByCode(code: String): PublicProfile? {
        if (uid.isEmpty()) return null
        return try {
            val snap = profilesCol
                .whereEqualTo("userCode", code.uppercase().trim())
                .limit(1)
                .get()
                .await()
            val doc = snap.documents.firstOrNull() ?: return null
            if (doc.getString("uid") == uid) return null  // não adicionar a si mesmo
            PublicProfile(
                uid = doc.getString("uid") ?: return null,
                name = doc.getString("name") ?: "",
                userCode = doc.getString("userCode") ?: "",
                level = doc.getLong("level")?.toInt() ?: 1,
                levelName = doc.getString("levelName") ?: "Poupador Iniciante",
                xp = doc.getLong("xp")?.toInt() ?: 0
            )
        } catch (e: Exception) { null }
    }

    override suspend fun addFriend(profile: PublicProfile) {
        friendsCol.document(profile.uid).set(
            mapOf(
                "uid" to profile.uid,
                "name" to profile.name,
                "userCode" to profile.userCode,
                "level" to profile.level,
                "levelName" to profile.levelName,
                "xp" to profile.xp,
                "addedAt" to System.currentTimeMillis()
            )
        ).await()
    }

    override suspend fun removeFriend(friendUid: String) {
        friendsCol.document(friendUid).delete().await()
    }

    override suspend fun syncPublicProfile(profile: PublicProfile) {
        if (uid.isEmpty()) return
        val code = if (profile.userCode.isEmpty()) generateUserCode() else profile.userCode
        if (profile.userCode.isEmpty()) {
            firestore.collection("users").document(uid).update("userCode", code).await()
        }
        profilesCol.document(uid).set(
            mapOf(
                "uid" to uid,
                "name" to profile.name,
                "userCode" to code,
                "level" to profile.level,
                "levelName" to profile.levelName,
                "xp" to profile.xp
            )
        ).await()
    }

    override suspend fun postToMyFeed(item: FeedItem) {
        val ref = feedCol.document()
        ref.set(
            mapOf(
                "type" to item.type.name,
                "actorName" to item.actorName,
                "message" to item.message,
                "timestamp" to item.timestamp
            )
        ).await()
    }

    private fun generateUserCode(): String {
        val chars = uid.filter { it.isLetterOrDigit() }.uppercase()
        val part = (chars.take(2) + chars.takeLast(2)).take(4).padEnd(4, '0')
        return "CRES-$part"
    }
}
