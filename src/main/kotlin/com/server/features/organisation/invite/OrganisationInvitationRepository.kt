package com.server.features.organisation.invite;

import com.server.features.common.BaseRepository
import com.server.features.organisation.invite.models.OrganisationInvitationEntity
import com.server.features.organisation.invite.models.OrganisationInvitationViewDto
import com.server.features.organisation.invite.models.StatusInvitation

abstract class OrganisationInvitationRepository : BaseRepository<OrganisationInvitationEntity>() {

    abstract suspend fun getAllView(email: String?, userId: String?): List<OrganisationInvitationViewDto>

    abstract suspend fun getAllByOrganisationId(organisationId: String): List<OrganisationInvitationEntity>

    abstract suspend fun inviteAgain(entity: OrganisationInvitationEntity): Boolean

    abstract suspend fun updateInvitationStatus(invitationId: String, status: StatusInvitation): Boolean

    abstract suspend fun deleteByUserId(userId: String): Boolean

    abstract suspend fun deleteByEmail(email: String): Boolean

}