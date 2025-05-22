package com.server.features

import kotlinx.serialization.Serializable


enum class AppRoleCategory {
    ALL,
    PRODUCT,
    ORDER,
    USER,
    ORGANIZATION
}

enum class Permission {
    CREATE, EDIT, DELETE, ALL
}

@Serializable
data class RolePermission(
    val category: AppRoleCategory,
    val permissions: Set<Permission>
)