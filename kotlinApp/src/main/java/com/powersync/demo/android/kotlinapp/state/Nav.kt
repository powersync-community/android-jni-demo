package com.powersync.demo.android.kotlinapp.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class Screen {
    data object Home : Screen()
    data object Todos : Screen()
}

class NavController(initialScreen: Screen) {
    private val _currentScreen = MutableStateFlow<Screen>(initialScreen)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    fun navigate(screen: Screen) {
        _currentScreen.value = screen
    }
}
