package com.cresup.app.domain.repository

import com.cresup.app.domain.model.FeedItem
import com.cresup.app.domain.model.PublicProfile
import kotlinx.coroutines.flow.Flow

interface SocialRepository {
    fun getFriends(): Flow<List<PublicProfile>>
    fun getFeed(): Flow<List<FeedItem>>
    suspend fun searchByCode(code: String): PublicProfile?
    suspend fun addFriend(profile: PublicProfile)
    suspend fun removeFriend(friendUid: String)
    suspend fun syncPublicProfile(profile: PublicProfile)
    suspend fun postToMyFeed(item: FeedItem)
}
