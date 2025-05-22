package com.server.features.organisation

import com.server.features.common.BaseRepository

abstract class OrganisationRepository : BaseRepository<Organisation>() {

    abstract suspend fun updateOrganisation(id: String, update: OrganisationRequest): Boolean

    abstract suspend fun getAllByIds(ids: Set<String>): List<Organisation>

    abstract suspend fun getAllOrganisationsByUserId(userId: String): List<Organisation>

}