package com.cresup.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cresup.app.data.local.dao.ChallengeDao
import com.cresup.app.data.local.dao.GoalDao
import com.cresup.app.data.local.dao.TransactionDao
import com.cresup.app.data.local.dao.UserDao
import com.cresup.app.data.local.entities.ChallengeEntity
import com.cresup.app.data.local.entities.GoalEntity
import com.cresup.app.data.local.entities.TransactionEntity
import com.cresup.app.data.local.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        TransactionEntity::class,
        GoalEntity::class,
        ChallengeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun goalDao(): GoalDao
    abstract fun challengeDao(): ChallengeDao
}
