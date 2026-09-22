package com.alan.routineos.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alan.routineos.data.local.RoutineOSDatabase
import com.alan.routineos.data.local.MIGRATION_6_7
import com.alan.routineos.data.local.MIGRATION_7_8
import com.alan.routineos.data.local.MIGRATION_8_9
import com.alan.routineos.data.local.MIGRATION_9_10
import com.alan.routineos.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. BACKLOG ITEMS (New Table)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `backlog_items` (
                    `id` TEXT NOT NULL, 
                    `definitionId` TEXT, 
                    `title` TEXT NOT NULL, 
                    `status` TEXT NOT NULL, 
                    PRIMARY KEY(`id`), 
                    FOREIGN KEY(`definitionId`) REFERENCES `activity_definitions`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_backlog_items_definitionId` ON `backlog_items` (`definitionId`)")

            // 2. DAILY INSTANCES (Refactor)
            // SQLite indices are associated with the database, so if we rename the table, the index still exists
            // and points to the new table name. To reuse the index name on the NEW table, we must drop the old ones.
            db.execSQL("DROP INDEX IF EXISTS `index_daily_instances_sourceRuleId_scheduledDate`")
            db.execSQL("DROP INDEX IF EXISTS `index_daily_instances_parentInstanceId`")
            db.execSQL("ALTER TABLE `daily_instances` RENAME TO `temp_daily_instances`")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `daily_instances` (
                    `id` TEXT NOT NULL, 
                    `targetId` TEXT, 
                    `targetType` TEXT NOT NULL, 
                    `scheduledDate` INTEGER NOT NULL, 
                    `titleSnapshot` TEXT NOT NULL, 
                    `descriptionSnapshot` TEXT NOT NULL, 
                    `plannedStartTime` INTEGER, 
                    `plannedEndTime` INTEGER, 
                    `plannedDurationMinutes` INTEGER, 
                    `status` TEXT NOT NULL, 
                    `mobility` TEXT NOT NULL, 
                    `sourceRuleId` TEXT, 
                    `parentInstanceId` TEXT, 
                    `backlogId` TEXT, 
                    PRIMARY KEY(`id`), 
                    FOREIGN KEY(`parentInstanceId`) REFERENCES `daily_instances`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, 
                    FOREIGN KEY(`backlogId`) REFERENCES `backlog_items`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL, 
                    FOREIGN KEY(`sourceRuleId`) REFERENCES `schedule_rules`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                )
            """.trimIndent())
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_daily_instances_sourceRuleId_scheduledDate` ON `daily_instances` (`sourceRuleId`, `scheduledDate`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_instances_parentInstanceId` ON `daily_instances` (`parentInstanceId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_instances_backlogId` ON `daily_instances` (`backlogId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_instances_sourceRuleId` ON `daily_instances` (`sourceRuleId`)")

            db.execSQL("""
                INSERT INTO `daily_instances` (
                    id, targetId, targetType, scheduledDate, titleSnapshot, descriptionSnapshot, 
                    plannedStartTime, plannedEndTime, plannedDurationMinutes, status, mobility, 
                    sourceRuleId, parentInstanceId, backlogId
                )
                SELECT id, targetId, targetType, scheduledDate, titleSnapshot, descriptionSnapshot, 
                       plannedStartTime, plannedEndTime, plannedDurationMinutes, status, mobility, 
                       sourceRuleId, parentInstanceId, NULL
                FROM `temp_daily_instances`
            """.trimIndent())
            db.execSQL("DROP TABLE `temp_daily_instances`")

            // 3. ACTIVITY EXECUTIONS (Refactor)
            db.execSQL("DROP INDEX IF EXISTS `index_activity_executions_nodeId`")
            db.execSQL("DROP INDEX IF EXISTS `index_activity_executions_dailyInstanceId`")
            db.execSQL("ALTER TABLE `activity_executions` RENAME TO `temp_activity_executions`")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `activity_executions` (
                    `id` TEXT NOT NULL, 
                    `nodeId` TEXT, 
                    `dailyInstanceId` TEXT, 
                    `scheduledDate` INTEGER NOT NULL, 
                    `completedAt` INTEGER NOT NULL, 
                    `metadataJson` TEXT NOT NULL, 
                    `activityIdSnapshot` TEXT NOT NULL, 
                    `systemIdSnapshot` TEXT, 
                    `titleSnapshot` TEXT NOT NULL, 
                    PRIMARY KEY(`id`), 
                    FOREIGN KEY(`nodeId`) REFERENCES `activity_nodes`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL, 
                    FOREIGN KEY(`dailyInstanceId`) REFERENCES `daily_instances`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_activity_executions_nodeId` ON `activity_executions` (`nodeId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_activity_executions_dailyInstanceId` ON `activity_executions` (`dailyInstanceId`)")

            db.execSQL("""
                INSERT INTO `activity_executions` (
                    id, nodeId, dailyInstanceId, scheduledDate, completedAt, metadataJson, 
                    activityIdSnapshot, systemIdSnapshot, titleSnapshot
                )
                SELECT 
                    t.id, t.nodeId, t.dailyInstanceId, t.scheduledDate, t.completedAt, t.metadataJson,
                    COALESCE(n.activityDefinitionId, 'UNKNOWN'),
                    d.systemId,
                    COALESCE(n.title, 'Deleted Activity')
                FROM `temp_activity_executions` t
                LEFT JOIN `activity_nodes` n ON t.nodeId = n.id
                LEFT JOIN `activity_definitions` d ON n.activityDefinitionId = d.id
            """.trimIndent())
            db.execSQL("DROP TABLE `temp_activity_executions`")

            // 4. DEADLINES (With exactly one owner check)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `deadlines` (
                    `id` TEXT NOT NULL, 
                    `dueAt` INTEGER NOT NULL, 
                    `definitionId` TEXT, 
                    `backlogId` TEXT, 
                    `instanceId` TEXT, 
                    PRIMARY KEY(`id`), 
                    FOREIGN KEY(`definitionId`) REFERENCES `activity_definitions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, 
                    FOREIGN KEY(`backlogId`) REFERENCES `backlog_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, 
                    FOREIGN KEY(`instanceId`) REFERENCES `daily_instances`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    CHECK ((definitionId IS NOT NULL) + (backlogId IS NOT NULL) + (instanceId IS NOT NULL) == 1)
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_deadlines_definitionId` ON `deadlines` (`definitionId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_deadlines_backlogId` ON `deadlines` (`backlogId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_deadlines_instanceId` ON `deadlines` (`instanceId`)")

            // 5. NOTES (Global or max one owner check)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `notes` (
                    `id` TEXT NOT NULL, 
                    `content` TEXT NOT NULL, 
                    `definitionId` TEXT, 
                    `backlogId` TEXT, 
                    `instanceId` TEXT, 
                    PRIMARY KEY(`id`), 
                    FOREIGN KEY(`definitionId`) REFERENCES `activity_definitions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, 
                    FOREIGN KEY(`backlogId`) REFERENCES `backlog_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, 
                    FOREIGN KEY(`instanceId`) REFERENCES `daily_instances`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    CHECK ((definitionId IS NOT NULL) + (backlogId IS NOT NULL) + (instanceId IS NOT NULL) <= 1)
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_definitionId` ON `notes` (`definitionId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_backlogId` ON `notes` (`backlogId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_instanceId` ON `notes` (`instanceId`)")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RoutineOSDatabase {
        return Room.databaseBuilder(
            context,
            RoutineOSDatabase::class.java,
            "routine_db"
        )
            .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideActivityDefinitionDao(db: RoutineOSDatabase): ActivityDefinitionDao = db.activityDefinitionDao()

    @Provides
    @Singleton
    fun provideActivityNodeDao(db: RoutineOSDatabase): ActivityNodeDao = db.activityNodeDao()

    @Provides
    @Singleton
    fun provideActivityExecutionDao(db: RoutineOSDatabase): ActivityExecutionDao = db.activityExecutionDao()

    @Provides
    @Singleton
    fun provideScheduleRuleDao(db: RoutineOSDatabase): ScheduleRuleDao = db.scheduleRuleDao()

    @Provides
    @Singleton
    fun provideScheduleExceptionDao(db: RoutineOSDatabase): ScheduleExceptionDao = db.scheduleExceptionDao()

    @Provides
    @Singleton
    fun provideDailyInstanceDao(db: RoutineOSDatabase): DailyInstanceDao = db.dailyInstanceDao()

    @Provides
    @Singleton
    fun provideMetadataSchemaDao(db: RoutineOSDatabase): MetadataSchemaDao = db.metadataSchemaDao()

    @Provides
    @Singleton
    fun provideSystemDao(db: RoutineOSDatabase): SystemDao = db.systemDao()

    @Provides
    @Singleton
    fun provideBacklogItemDao(db: RoutineOSDatabase): BacklogItemDao = db.backlogItemDao()

    @Provides
    @Singleton
    fun provideDeadlineDao(db: RoutineOSDatabase): DeadlineDao = db.deadlineDao()

    @Provides
    @Singleton
    fun provideNoteDao(db: RoutineOSDatabase): NoteDao = db.noteDao()
}
