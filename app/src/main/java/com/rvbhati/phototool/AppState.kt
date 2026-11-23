package com.rvbhati.phototool

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AppState {
    var isDarkMode by mutableStateOf(true)
    var isHindi by mutableStateOf(true)

    fun t(hindi: String, english: String): String {
        return if (isHindi) hindi else english
    }
}
