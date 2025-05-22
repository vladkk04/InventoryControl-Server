package com.server.features.user.scheduleTime;

import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase

class UserScheduleRepositoryImpl(
    override val db: MongoDatabase,
) : UserScheduleRepository() {

    override val collection: MongoCollection<UserSchedule>
        get() = db.getCollection("userschedules")

}