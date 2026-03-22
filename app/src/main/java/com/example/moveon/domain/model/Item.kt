package com.example.moveon.domain.model

data class Item(
    val id: String,
    val boxId: String,
    val name: String,
    val quantity: Int,
    val description: String,
    val imageUrl: String,
    val isFragile: Boolean
)