package com.server.features.organisation.settings;

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrganisationSettingsRequest(
    @SerialName("threshold_settings")
    val thresholdSettings: ThresholdSettings,
    @SerialName("notification_settings")
    val notificationSettings: NotificationSettings
)