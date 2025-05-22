package com.server.features.user

import com.server.features.auth.requests.ChangeInfoUserRequest
import com.server.features.common.BaseRepository
import com.server.features.security.hashing.SaltedHash

abstract class UserRepository: BaseRepository<User>() {

    abstract suspend fun changePassword(id: String, saltedHash: SaltedHash): Boolean

    abstract suspend fun changeEmail(id: String, newEmail: String): Boolean

    abstract suspend fun changeUserInfo(id: String, changeInfoUserRequest: ChangeInfoUserRequest): Boolean

    abstract suspend fun isUserExists(email: String): Boolean

    abstract suspend fun getByEmail(email: String): User

    abstract suspend fun confirmEmail(email: String): Boolean

    abstract suspend fun updatePassword(email: String, password: String, newSalt: String): Boolean

}