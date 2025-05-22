package com.server.features.organisation.user;

import com.server.features.common.BaseRepository
import com.server.features.organisation.OrganisationRole

abstract class OrganisationUserRepository : BaseRepository<OrganisationUserEntity>() {

    abstract suspend fun assignRoleByUserId(id: String, role: OrganisationRole): Boolean

    abstract suspend fun changeOrganisationUserNameByUserId(id: String, update: UpdateOrganisationUserRequest): Boolean

    abstract suspend fun getAllByOrganisationId(organisationId: String): List<OrganisationUserDto>

    abstract suspend fun getAllByUserId(userId: String): List<OrganisationUserEntity>

    abstract suspend fun getAllPendingAndAccepted(): List<OrganisationUserView>

    abstract suspend fun getByUserId(userId: String, organisationId: String): OrganisationUserEntity

    abstract suspend fun getAllViewByUserId(userId: String): List<OrganisationUserView>

    abstract suspend fun makeUserInactive(organisationUserid: String): Boolean

    abstract suspend fun cancelInviteByUserId(userId: String): Boolean

    abstract suspend fun cancelInviteByEmail(email: String): Boolean

    abstract suspend fun makeUserActive(organisationUserid: String): Boolean
}