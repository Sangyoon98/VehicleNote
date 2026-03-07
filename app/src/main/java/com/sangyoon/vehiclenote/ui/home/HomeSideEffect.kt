package com.sangyoon.vehiclenote.ui.home

sealed interface HomeSideEffect {
    data object NavigateToAdd : HomeSideEffect
    data class NavigateToDetail(val vehicleId: Long) : HomeSideEffect
    data class ShowSnackbar(val message: String) : HomeSideEffect
}
