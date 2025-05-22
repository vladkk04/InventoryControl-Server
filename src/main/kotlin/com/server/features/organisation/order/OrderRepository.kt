package com.server.features.organisation.order;

import com.server.features.common.BaseRepository

abstract class OrderRepository : BaseRepository<Order>() {

    abstract suspend fun getAllByOrganisationId(organisationId: String): List<Order>

    abstract suspend fun deleteProduct(productId: String): Boolean

    abstract suspend fun updateById(id: String, update: OrderRequest): Boolean

}