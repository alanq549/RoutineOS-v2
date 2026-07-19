package com.alan.routineos.feature.account.model

data class UserProfile(
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val plan: String
)

data class SettingItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val iconName: String,
    val type: SettingType = SettingType.NAVIGATION
)

enum class SettingType {
    NAVIGATION, TOGGLE
}

data class AccountSectionModel(
    val title: String,
    val items: List<SettingItem>
)
