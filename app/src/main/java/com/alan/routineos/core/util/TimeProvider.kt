package com.alan.routineos.core.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

interface TimeProvider {
    fun now(): LocalTime
    val minuteTicker: Flow<Unit>
}

@Singleton
class DefaultTimeProvider @Inject constructor() : TimeProvider {
    override fun now(): LocalTime = LocalTime.now()

    override val minuteTicker: Flow<Unit> = flow {
        emit(Unit) // Initial emission
        while (true) {
            delay(60_000)
            emit(Unit)
        }
    }
}
