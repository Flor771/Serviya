package com.example.data.repository

import com.example.data.models.Category
import com.example.data.models.DefaultCategories
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CategoryRepository {
    private val _categoriesState = MutableStateFlow<List<Category>>(DefaultCategories.list)
    val categoriesState = _categoriesState.asStateFlow()

    fun addCategory(category: Category) {
        val list = _categoriesState.value.toMutableList()
        list.add(category)
        _categoriesState.value = list
    }
}
