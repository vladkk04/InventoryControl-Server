package com.server.features.organisation.order;

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class Order(
    @BsonId
    val id: ObjectId = ObjectId(),
    val products: List<OrderProduct>,
    val discount: OrderDiscount,
    val comment: String? = null,
    val attachments: List<Attachment>,
    @BsonProperty("organisation_id")
    val organisationId: String,
    @BsonProperty("ordered_by")
    val orderedBy: String,
)

@Serializable
data class OrderDto(
    val id: String,
    val products: List<OrderProduct>,
    val discount: OrderDiscount,
    val comment: String? = null,
    val attachments: List<Attachment>,
    @SerialName("organisation_id")
    val organisationId: String,
    @SerialName("ordered_by")
    val orderedBy: String,
    @SerialName("ordered_at")
    val orderedAt: Long
)

fun Order.mapToDto() = OrderDto(
    id = this.id.toHexString(),
    products = this.products,
    discount = this.discount,
    comment = this.comment,
    attachments = this.attachments,
    organisationId = this.organisationId,
    orderedBy = this.orderedBy,
    orderedAt = this.id.date.time
)

