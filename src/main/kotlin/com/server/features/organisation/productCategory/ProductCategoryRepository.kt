package com.server.features.organisation.productCategory;

import com.server.features.common.BaseRepository

abstract class ProductCategoryRepository : BaseRepository<ProductCategory>() {

    abstract suspend fun getAllByOrganisationId(organisationId: String): List<ProductCategory>

    abstract suspend fun deleteByOrganisationId(organisationId: String): Boolean

    abstract suspend fun updateById(id: String, update: ProductCategoryRequest): Boolean

}