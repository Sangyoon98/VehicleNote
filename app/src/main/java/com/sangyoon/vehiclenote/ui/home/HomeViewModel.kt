package com.sangyoon.vehiclenote.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.vehiclenote.domain.usecase.GetAllVehiclesUseCase
import com.sangyoon.vehiclenote.domain.usecase.SearchVehicleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllVehiclesUseCase: GetAllVehiclesUseCase,
    private val searchVehicleUseCase: SearchVehicleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadVehicles()
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.SearchQueryChanged -> {
               _state.update { it.copy(searchQuery = intent.query) }
                if (intent.query.isNotBlank()) {
                    searchVehicles(intent.query)
                } else {
                    loadVehicles()
                }
            }
            is HomeIntent.SearchActiveChanged -> {
                _state.update {
                    it.copy(
                        isSearchActive = intent.isActive,
                        searchQuery = if (!intent.isActive) "" else it.searchQuery
                    )
                }
            }
            is HomeIntent.DeleteVehicle -> {

            }
            is HomeIntent.AddVehicleClicked -> {

            }
            is HomeIntent.VehicleClicked -> {

            }
            is HomeIntent.Refresh -> {
                loadVehicles()
            }
        }
    }

    private fun loadVehicles() {
        getAllVehiclesUseCase()
            .onEach { vehicles ->
                _state.update {
                    it.copy(
                        vehicles = vehicles,
                        isLoading = false,
                        error = null
                    )
                }
            }
            .catch { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun searchVehicles(query: String) {
        if (query.isBlank()) {
            loadVehicles()
            return
        }

        searchVehicleUseCase(query)
            .onEach { vehicles ->
                _state.update {
                    it.copy(
                        vehicles = vehicles,
                        isLoading = false,
                        error = null
                    )
                }
            }
            .catch { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}