package com.server.features.user


fun User.mapToDto() = UserResponse(
    id = this.id.toString(),
    email = this.email,
    fullName = this.fullName,
    imageUrl = this.imageUrl,
    registeredAt = this.id.timestamp
)