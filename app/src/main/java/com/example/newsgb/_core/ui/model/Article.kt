package com.example.newsgb._core.ui.model

import android.os.Parcelable
import com.example.newsgb.utils.ui.Category
import kotlinx.parcelize.Parcelize

@Parcelize
data class Article(
    val category: Category,
    val sourceName: String,
    val author: String,
    val title: String,
    val description: String,
    val contentUrl: String,
    val imageUrl: String?,
    val publishedDate: String,
    val content: String,
    val isChecked: Boolean = false
) : Parcelable