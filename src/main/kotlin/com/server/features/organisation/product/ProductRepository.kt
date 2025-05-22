package com.server.features.organisation.product;

import com.server.features.common.BaseRepository
import com.server.features.updateStockProduct.ProductStockUpdate

abstract class ProductRepository : BaseRepository<Product>() {

    abstract suspend fun getAllByOrganisationId(organisationId: String): List<Product>

    abstract suspend fun getAllIn(listIds: List<String>): List<Product>

    abstract suspend fun updateProductStock(updatedProductStock: List<ProductStockUpdate>)

    abstract suspend fun updateById(id: String, update: ProductUpdate): Boolean

}