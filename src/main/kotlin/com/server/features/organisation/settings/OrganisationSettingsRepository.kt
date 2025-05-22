package com.server.features.organisation.settings;

import com.server.features.common.BaseRepository

abstract class OrganisationSettingsRepository : BaseRepository<OrganisationSettings>() {

    abstract suspend fun getByOrganisationId(organisationId: String): OrganisationSettings

    abstract suspend fun updateByOrganisationId(organisationId: String, update: OrganisationSettingsRequest): Boolean

}