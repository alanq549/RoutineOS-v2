package com.alan.routineos.feature.account.data

import com.alan.routineos.feature.account.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeAccountRepository {
    fun getUserProfile(): Flow<UserProfile> = flowOf(
        UserProfile(
            name = "Alan Geovani",
            email = "alan@email.com",
            avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCAIb2EHWh1pANMPoLx_8khf4byCRGHYFZYb4JzqkSC-2MIGwsgd0jJjnzz-ylmCmAxOjzxiBuugqK-9x-_6jyEDqxzWfQEPcX4APYJoYhUyb1r3m3lcTMag1ymDX7hBUj5_HolIk9UjgncBBdcV8-I4sV_OvtZmQXETTS5ZmPq7q7eVOimpsWmLFY76rKB8r_J7BVQOgfD8kLTd0yKWPlL_pn2dDSiZCwR9DuEbDdl4t3G7o3KXsft9k7WZFyImyGc--ddfMq3cFDI",
            plan = "Technical Premium"
        )
    )

    fun getAccountSections(): Flow<List<AccountSectionModel>> = flowOf(
        listOf(
            AccountSectionModel(
                title = "PREFERENCIAS",
                items = listOf(
                    SettingItem("appearance", "Apariencia", "Oscuro", "palette"),
                    SettingItem("notifications", "Notificaciones", null, "notifications"),
                    SettingItem("accessibility", "Accesibilidad", null, "accessibility")
                )
            ),
            AccountSectionModel(
                title = "PRIVACIDAD Y DATOS",
                items = listOf(
                    SettingItem("privacy", "Privacidad", null, "lock"),
                    SettingItem("data", "Datos", null, "database")
                )
            ),
            AccountSectionModel(
                title = "APLICACIÓN",
                items = listOf(
                    SettingItem("about", "Acerca de RoutineOS", null, "info"),
                    SettingItem("backup", "Copia de seguridad", null, "cloud_upload")
                )
            )
        )
    )
}
