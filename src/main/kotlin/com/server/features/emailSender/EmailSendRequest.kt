package com.server.features.emailSender

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmailSendRequest(
    val from: Recipient,
    val to: Recipient,
    val subject: String,
    val text: String? = null,
    val html: String? = null,
    @SerialName("template_id")
    val templateId: String? = null,
    val personalization: List<PersonalizationVariable> = emptyList()
) {
    @Serializable
    data class Recipient(
        val email: String,
        val name: String? = null
    )


    @Serializable
    data class PersonalizationVariable(
        val email: String,
        val data: PersonalizationData
    )


    @Serializable
    sealed class PersonalizationData {
        @Serializable
        data class ConfirmEmailData(
            val name: String,
            @SerialName("account_name")
            val accountName: String,
            @SerialName("confirm_link")
            val confirmLink: String
        ) : PersonalizationData()
    }
}

