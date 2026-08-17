package com.alan.routineos

import android.app.Application
import com.alan.routineos.data.local.DatabaseSeed
import com.alan.routineos.data.local.dao.*
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import javax.inject.Provider

@HiltAndroidApp
class RoutineApp : Application() {

    @Inject
    lateinit var definitionDao: ActivityDefinitionDao

    @Inject
    lateinit var nodeDao: ActivityNodeDao

    @Inject
    lateinit var ruleDao: ScheduleRuleDao

    @Inject
    lateinit var metaDao: MetadataSchemaDao

    override fun onCreate() {
        super.onCreate()
        // Trigger dev seed with all core DAOs
        DatabaseSeed.seedAll(definitionDao, nodeDao, ruleDao, metaDao)
    }
}
