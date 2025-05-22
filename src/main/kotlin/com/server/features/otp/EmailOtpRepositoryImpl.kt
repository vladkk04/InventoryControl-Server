package com.server.features.otp

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.Updates.combine
import com.mongodb.client.model.Updates.set
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit

class EmailOtpRepositoryImpl(
    db: MongoDatabase
) : EmailOtpRepository {

    companion object {
        const val EXPIRE_IN_MIN = 15L
        const val DIGITS = 6
    }

    private val otpCodeCollection = db.getCollection<OtpCode>("otp_codes")


    private suspend fun insertOtpCode(otpCode: OtpCode): Boolean {
        otpCodeCollection.createIndex(
            Indexes.ascending("expire_at"),
            IndexOptions().expireAfter(0, TimeUnit.SECONDS)
        )

        return otpCodeCollection.insertOne(otpCode).wasAcknowledged()
    }

    private suspend fun getOtpCode(email: String): OtpCode? {
        val filter = eq(OtpCode::email.name, email)
        return otpCodeCollection.find(filter).firstOrNull()
    }

    private suspend fun updateOtp(otp: OtpCode): Boolean {
        val filter = eq(OtpCode::email.name, otp.email)
        val update = combine(
            set(OtpCode::otp.name, otp.otp),
            set(OtpCode::expireAt.name, otp.expireAt)
        )
        return otpCodeCollection.updateOne(filter, update).wasAcknowledged()
    }


    private suspend fun deleteOtpCode(email: String): Boolean {
        val filter = eq(OtpCode::email.name, email)
        return otpCodeCollection.deleteOne(filter).wasAcknowledged()
    }


    override suspend fun generateOneTimeOtp(email: String): String {
        val newOtp = OtpCode(
            email = email,
            otp = Random().nextInt(100000, 999999).toString(),
            expireAt = Date.from(Instant.now().plus(EXPIRE_IN_MIN, ChronoUnit.DAYS))
        )

        if (getOtpCode(email) == null) {
            insertOtpCode(
                OtpCode(
                    email = email,
                    otp = newOtp.otp,
                    expireAt = newOtp.expireAt
                )
            )
        } else {
            updateOtp(newOtp)
        }

        return newOtp.otp
    }

    override suspend fun validateOneTimeOtp(email: String, otp: String): Boolean {

        val otpCodeFromServer = getOtpCode(email) ?: return false

        return if (otpCodeFromServer.otp == otp) {
            deleteOtpCode(email)
            true
        } else {
            false
        }
    }

}
