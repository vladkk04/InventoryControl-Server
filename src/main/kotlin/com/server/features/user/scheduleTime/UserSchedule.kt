package com.server.features.user.scheduleTime;

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class UserSchedule(
    @BsonId
    val id: ObjectId = ObjectId()
)