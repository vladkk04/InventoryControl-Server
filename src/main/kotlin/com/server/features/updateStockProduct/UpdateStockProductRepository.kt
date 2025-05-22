package com.server.features.updateStockProduct;

import com.server.features.common.BaseRepository

abstract class UpdateStockProductRepository : BaseRepository<ProductUpdateStock>() {

    abstract suspend fun deleteByOrderId(id: String): Boolean

    abstract suspend fun deleteProductInUpdatedStock(organisationId: String, productId: String): Boolean

    abstract suspend fun getByOrderId(id: String): ProductUpdateStock

    abstract suspend fun getAllByProductId(productId: String): List<ChangeStockProduct>

    abstract suspend fun getAllByOrganisationIdView(organisationId: String): List<ProductUpdateStockViewDto>

    abstract suspend fun getAllByOrganisationId(organisationId: String): List<ProductUpdateStock>

}