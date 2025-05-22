package com.server.features.organisation.settings;

import com.server.features.organisation.OrganisationRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId
import java.util.*

data class OrganisationSettings(
    @BsonId
    val id: ObjectId = ObjectId(),
    @BsonProperty("notification_settings")
    val notificationSettings: NotificationSettings = NotificationSettings(
        notificationTime = "07:00",
        notificationDays = setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY),
        notifiableRoles = setOf(OrganisationRole.EMPLOYEE)
    ),
    @BsonProperty("threshold_settings")
    val thresholdSettings: ThresholdSettings = ThresholdSettings(
        normalThresholdPercentage = 100.00,
        mediumThresholdPercentage = 50.00,
        criticalThresholdPercentage = 25.00
    ),
    @BsonProperty("organisation_id")
    val organisationId: String
)

@Serializable
data class OrganisationSettingsDto(
    val id: String,
    @SerialName("notification_settings")
    val notificationSettings: NotificationSettings,
    @SerialName("threshold_settings")
    val thresholdSettings: ThresholdSettings,
    @SerialName("organisation_id")
    val organisationId: String
)

fun OrganisationSettings.mapToDto() = OrganisationSettingsDto(
    id = this.id.toHexString(),
    notificationSettings = this.notificationSettings,
    thresholdSettings = this.thresholdSettings,
    organisationId = this.organisationId
)