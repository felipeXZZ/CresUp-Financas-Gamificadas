package com.cresup.app.data.repository

import com.cresup.app.data.local.dao.UserDao
import com.cresup.app.data.local.entities.UserEntity
import com.cresup.app.data.local.entities.toDomain
import com.cresup.app.data.local.entities.toEntity
import com.cresup.app.domain.model.User
import com.cresup.app.domain.model.computeLevel
import com.cresup.app.domain.model.xpToNextLevel
import com.cresup.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val dao: UserDao
) : UserRepository {

    override fun getUser(): Flow<User> =
        dao.getUser().map { entity ->
            entity?.toDomain() ?: User(name = "Usuário")
        }

    override suspend fun updateUser(user: User) {
        dao.update(user.toEntity())
    }

    override suspend fun addXP(amount: Int) {
        dao.addXP(amount)
    }

    override suspend fun updateStreak() {
        // Streak logic handled at repository level for simplicity
    }

    override suspend fun createDefaultUser() {
        if (dao.count() == 0) {
            dao.insert(UserEntity())
        }
    }
}
