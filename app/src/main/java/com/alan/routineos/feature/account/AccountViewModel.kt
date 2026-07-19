package com.alan.routineos.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.feature.account.data.FakeAccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class AccountViewModel(
    private val repository: FakeAccountRepository = FakeAccountRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState(isLoading = true))
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getUserProfile(),
                repository.getAccountSections()
            ) { profile, sections ->
                AccountUiState(
                    isLoading = false,
                    profile = profile,
                    sections = sections
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun onLogout() {
        // Implementation for logout
    }
}
