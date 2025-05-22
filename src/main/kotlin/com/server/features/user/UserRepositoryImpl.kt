package com.server.features.user

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates.*
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.server.exceptions.DatabaseException
import com.server.features.auth.requests.ChangeInfoUserRequest
import com.server.features.security.hashing.SaltedHash
import org.bson.types.ObjectId

class UserRepositoryImpl(
    override val db: MongoDatabase,
) : UserRepository() {

    override val collection: MongoCollection<User>
        get() = db.getCollection("users")

    override suspend fun changePassword(id: String, saltedHash: SaltedHash): Boolean {
        return updateById(
            id = id,
            update = combine(
                set(User::password.name, saltedHash.hash),
                set(User::salt.name, saltedHash.salt)
            ),
            updateOptions = UpdateOptions(),
        )
    }

    override suspend fun changeEmail(id: String, newEmail: String): Boolean {
        return updateById(
            id = id,
            update = set(User::email.name, newEmail),
            updateOptions = UpdateOptions()
        )
    }

    override suspend fun changeUserInfo(id: String, changeInfoUserRequest: ChangeInfoUserRequest): Boolean {
        return updateById(
            id = id,
            update = combine(
                set("full_name", changeInfoUserRequest.fullName),
                set("image_url", changeInfoUserRequest.logoUrl)
            ),
            updateOptions = UpdateOptions()
        )
    }

    override suspend fun isUserExists(email: String): Boolean = try {
        getByEmail(email)
        true
    } catch (e: DatabaseException.NotFoundException) {
        false
    }


    override suspend fun getByEmail(email: String): User =
        getBy(eq(User::email.name, email), "User not found with this email")


    override suspend fun confirmEmail(email: String): Boolean {
        return updateOne(
            filter = eq(User::email.name, email),
            update = combine(
                currentDate("email_confirmed_at"),
            ),
            updateOptions = UpdateOptions().upsert(true)
        )
    }

    override suspend fun updatePassword(email: String, password: String, newSalt: String): Boolean {
        return updateOne(
            filter = eq(User::email.name, email),
            update = combine(
                set(User::password.name, password),
                set(User::salt.name, newSalt)
            ),
            updateOptions = UpdateOptions()
        )
    }
}