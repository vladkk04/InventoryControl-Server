package com.server.features.organisation.settings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonProperty

@Serializable
data class ThresholdSettings(
    @SerialName("normal_threshold_percentage")
    val normalThresholdPercentage: Double,
    @SerialName("medium_threshold_percentage")
    val mediumThresholdPercentage: Double,
    @SerialName("critical_threshold_percentage")
    val criticalThresholdPercentage: Double,
)
