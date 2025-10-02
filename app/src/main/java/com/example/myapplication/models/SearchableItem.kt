package com.example.myapplication.models

data class SearchableItem(
    val title: String,
    val description: String,
    val searchText: String,
    val sourceFile: String
)