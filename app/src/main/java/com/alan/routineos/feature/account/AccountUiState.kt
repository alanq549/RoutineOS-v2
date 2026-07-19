package com.alan.routineos.feature.account

import com.alan.routineos.feature.account.model.*

data class AccountUiState(
    val isLoading: Boolean = false,
    val profile: UserProfile? = null,
    val sections: List<AccountSectionModel> = emptyList(),
    val version: String = "1.0"
)
