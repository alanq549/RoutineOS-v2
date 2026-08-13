package com.alan.routineos

import android.app.Application
import com.alan.routineos.data.local.DatabaseSeed
import com.alan.routineos.data.local.dao.ActivityDefinitionDao
import com.alan.routineos.data.local.dao.ActivityNodeDao
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import javax.inject.Provider

@HiltAndroidApp
class RoutineApp : Application() {

    @Inject
    lateinit var definitionDaoProvider: Provider<ActivityDefinitionDao>

    @Inject
    lateinit var nodeDaoProvider: Provider<ActivityNodeDao>

    override fun onCreate() {
        super.onCreate()
        // Trigger dev seed
        DatabaseSeed.seedHierarchy(definitionDaoProvider, nodeDaoProvider)
    }
}
