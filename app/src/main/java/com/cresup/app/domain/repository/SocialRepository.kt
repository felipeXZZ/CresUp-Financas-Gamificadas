package com.cresup.app.domain.repository

import com.cresup.app.domain.model.FeedItem
import com.cresup.app.domain.model.FriendRequest
import com.cresup.app.domain.model.PublicProfile
import kotlinx.coroutines.flow.Flow

interface SocialRepository {
    fun getFriends(): Flow<List<PublicProfile>>
    fun getIncomingRequests(): Flow<List<FriendRequest>>
    fun getSentRequests(): Flow<List<FriendRequest>>
    fun getFeed(): Flow<List<FeedItem>>
    suspend fun searchByCode(code: String): PublicProfile?
    suspend fun sendFriendRequest(toProfile: PublicProfile, myProfile: PublicProfile)
    suspend fun acceptRequest(request: FriendRequest)
    suspend fun rejectRequest(requestId: String)
    suspend fun removeFriend(friendUid: String)
    suspend fun syncPublicProfile(profile: PublicProfile)
    suspend fun postToMyFeed(item: FeedItem)
}
