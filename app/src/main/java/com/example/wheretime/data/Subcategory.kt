package com.example.wheretime.data

enum class Subcategory(val category: Category, val displayName: String) {
    STUDYING(Category.PRODUCTIVE, "Studying"),
    WORKING(Category.PRODUCTIVE, "Working"),
    SPORTS(Category.PRODUCTIVE, "Sports"),
    COOKING(Category.PRODUCTIVE, "Cooking"),
    WATCHING_TV(Category.UNPRODUCTIVE, "Watching TV");

    companion object {
        fun all(): List<Subcategory> = values().toList()
    }
}
