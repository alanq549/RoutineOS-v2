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
            val now = LocalTime.now()
            // Align with the next full minute
            val millisUntilNextMinute = ((60 - now.second) * 1000L) - (now.nano / 1_000_000L)
            delay(if (millisUntilNextMinute > 0) millisUntilNextMinute else 60_000L)
            emit(Unit)
        }
    }
}
