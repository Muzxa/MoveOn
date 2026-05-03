package com.example.moveon.data.mapper

import com.example.moveon.data.remote.dto.SavedAddressDto
import com.example.moveon.domain.model.SavedAddress

fun SavedAddressDto.toDomain(): SavedAddress {
    return SavedAddress(
        id = address_id,
        label = label,
        addressLine1 = address_line_1,
        addressLine2 = address_line_2,
        city = city,
        lat = lat,
        lng = lng,
        isDefault = is_default,
        updatedAt = updated_at,
        createdAt = created_at
    )
}

fun SavedAddress.toDto(): SavedAddressDto {
    return SavedAddressDto(
        address_id = id,
        label = label,
        address_line_1 = addressLine1,
        address_line_2 = addressLine2,
        city = city,
        lat = lat,
        lng = lng,
        is_default = isDefault,
        updated_at = updatedAt,
        created_at = createdAt
    )
}

