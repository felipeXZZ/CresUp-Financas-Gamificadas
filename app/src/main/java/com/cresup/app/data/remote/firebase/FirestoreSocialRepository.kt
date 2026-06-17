package com.cresup.app.data.remote.firebase

import com.cresup.app.domain.model.FeedItem
import com.cresup.app.domain.model.FeedItemType
import com.cresup.app.domain.model.FriendRequest
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
    private val requestsCol get() = firestore.collection("friendRequests")

    override fun getFriends(): Flow<List<PublicProfile>> = callbackFlow {
        val reg = friendsCol.addSnapshotListener { snap, err ->
            if (err != null) { close(); return@addSnapshotListener }
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

    override fun getIncomingRequests(): Flow<List<FriendRequest>> = callbackFlow {
        val reg = requestsCol
            .whereEqualTo("toUid", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { doc ->
                    runCatching {
                        if (doc.getString("status") != "pending") return@mapNotNull null
                        FriendRequest(
                            id = doc.id,
                            fromUid = doc.getString("fromUid") ?: return@mapNotNull null,
                            toUid = doc.getString("toUid") ?: "",
                            fromName = doc.getString("fromName") ?: "",
                            fromCode = doc.getString("fromCode") ?: "",
                            fromLevel = doc.getLong("fromLevel")?.toInt() ?: 1,
                            fromLevelName = doc.getString("fromLevelName") ?: "Poupador Iniciante",
                            fromXp = doc.getLong("fromXp")?.toInt() ?: 0,
                            status = "pending",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    }.getOrNull()
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    override fun getSentRequests(): Flow<List<FriendRequest>> = callbackFlow {
        val reg = requestsCol
            .whereEqualTo("fromUid", uid)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { doc ->
                    runCatching {
                        if (doc.getString("status") != "pending") return@mapNotNull null
                        FriendRequest(
                            id = doc.id,
                            fromUid = doc.getString("fromUid") ?: "",
                            toUid = doc.getString("toUid") ?: return@mapNotNull null,
                            toName = doc.getString("toName") ?: "",
                            toCode = doc.getString("toCode") ?: "",
                            toLevel = doc.getLong("toLevel")?.toInt() ?: 1,
                            toLevelName = doc.getString("toLevelName") ?: "Poupador Iniciante",
                            toXp = doc.getLong("toXp")?.toInt() ?: 0,
                            status = "pending",
                            timestamp = doc.getLong("timestamp") ?: 0L
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
                if (err != null) { close(); return@addSnapshotListener }
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
            if (doc.getString("uid") == uid) return null
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

    override suspend fun sendFriendRequest(toProfile: PublicProfile, myProfile: PublicProfile) {
        if (uid.isEmpty()) return
        val requestId = "${uid}_${toProfile.uid}"

        val friendSnap = friendsCol.document(toProfile.uid).get().await()
        if (friendSnap.exists()) throw Exception("already_friends")

        val existing = requestsCol.document(requestId).get().await()
        if (existing.exists()) throw Exception("already_sent")

        val reverseId = "${toProfile.uid}_${uid}"
        val reverse = requestsCol.document(reverseId).get().await()
        if (reverse.exists() && reverse.getString("status") == "pending") {
            throw Exception("has_incoming")
        }

        requestsCol.document(requestId).set(
            mapOf(
                "fromUid" to uid,
                "toUid" to toProfile.uid,
                "fromName" to myProfile.name,
                "fromCode" to myProfile.userCode,
                "fromLevel" to myProfile.level,
                "fromLevelName" to myProfile.levelName,
                "fromXp" to myProfile.xp,
                "toName" to toProfile.name,
                "toCode" to toProfile.userCode,
                "toLevel" to toProfile.level,
                "toLevelName" to toProfile.levelName,
                "toXp" to toProfile.xp,
                "status" to "pending",
                "timestamp" to System.currentTimeMillis()
            )
        ).await()
    }

    override suspend fun acceptRequest(request: FriendRequest) {
        friendsCol.document(request.fromUid).set(
            mapOf(
                "uid" to request.fromUid,
                "name" to request.fromName,
                "userCode" to request.fromCode,
                "level" to request.fromLevel,
                "levelName" to request.fromLevelName,
                "xp" to request.fromXp,
                "addedAt" to System.currentTimeMillis()
            )
        ).await()

        val myProfile = profilesCol.document(uid).get().await()
        val myName = myProfile.getString("name") ?: ""
        val myCode = myProfile.getString("userCode") ?: ""
        val myLevel = myProfile.getLong("level")?.toInt() ?: 1
        val myLevelName = myProfile.getString("levelName") ?: ""
        val myXp = myProfile.getLong("xp")?.toInt() ?: 0

        runCatching {
            firestore.collection("users").document(request.fromUid).collection("friends")
                .document(uid).set(
                    mapOf(
                        "uid" to uid,
                        "name" to myName,
                        "userCode" to myCode,
                        "level" to myLevel,
                        "levelName" to myLevelName,
                        "xp" to myXp,
                        "addedAt" to System.currentTimeMillis()
                    )
                ).await()
        }

        requestsCol.document(request.id).delete().await()

        val reverseId = "${uid}_${request.fromUid}"
        runCatching { requestsCol.document(reverseId).delete().await() }
    }

    override suspend fun rejectRequest(requestId: String) {
        requestsCol.document(requestId).delete().await()
    }

    override suspend fun removeFriend(friendUid: String) {
        friendsCol.document(friendUid).delete().await()
        runCatching {
            firestore.collection("users").document(friendUid).collection("friends")
                .document(uid).delete().await()
        }
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
        feedCol.document().set(
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
