package com.server.features.user.scheduleTime;

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserScheduleRequest(
    val f: String
)