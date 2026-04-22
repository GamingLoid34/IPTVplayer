package com.valladares.iptvplayer.data.xtream.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Shared category model for live, VOD and series category endpoints.
 */
@Serializable
data class XtreamCategoryDto(
    @SerialName("category_id") val categoryId: String,
    @SerialName("category_name") val categoryName: String,
    @Serializable(with = FlexibleIntSerializer::class)
    @SerialName("parent_id") val parentId: Int? = null
)
