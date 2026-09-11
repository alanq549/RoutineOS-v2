package com.alan.routineos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alan.routineos.data.local.dao.*
import com.alan.routineos.data.local.entities.*

@Database(
    entities = [
        ActivityDefinitionEntity::class,
        ActivityNodeEntity::class,
        ActivityExecutionEntity::class,
        ScheduleRuleEntity::class,
        ScheduleExceptionEntity::class,
        DailyInstanceEntity::class,
        MetadataSchemaEntity::class,
        SystemEntity::class,
        BacklogItemEntity::class,
        DeadlineEntity::class,
        NoteEntity::class,
    ],
    version = 9,
    exportSchema = true
)
abstract class RoutineOSDatabase : RoomDatabase() {
    abstract fun activityDefinitionDao(): ActivityDefinitionDao
    abstract fun activityNodeDao(): ActivityNodeDao
    abstract fun activityExecutionDao(): ActivityExecutionDao
    abstract fun scheduleRuleDao(): ScheduleRuleDao
    abstract fun scheduleExceptionDao(): ScheduleExceptionDao
    abstract fun dailyInstanceDao(): DailyInstanceDao
    abstract fun metadataSchemaDao(): MetadataSchemaDao
    abstract fun systemDao(): SystemDao
    abstract fun backlogItemDao(): BacklogItemDao
    abstract fun deadlineDao(): DeadlineDao
    abstract fun noteDao(): NoteDao
}

val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        // 1. Create new table with actionProtocol and CHECK constraint
        db.execSQL("""
            CREATE TABLE daily_instances_new (
                id TEXT NOT NULL PRIMARY KEY,
                targetId TEXT,
                targetType TEXT NOT NULL,
                scheduledDate INTEGER NOT NULL,
                titleSnapshot TEXT NOT NULL,
                descriptionSnapshot TEXT NOT NULL,
                plannedStartTime INTEGER,
                plannedEndTime INTEGER,
                plannedDurationMinutes INTEGER,
                status TEXT NOT NULL,
                mobility TEXT NOT NULL,
                sourceRuleId TEXT,
                parentInstanceId TEXT,
                backlogId TEXT,
                actionProtocol TEXT NOT NULL DEFAULT 'TIMER',
                reminderAbs INTEGER,
                reminderRel INTEGER,
                FOREIGN KEY(parentInstanceId) REFERENCES daily_instances_new(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(backlogId) REFERENCES backlog_items(id) ON UPDATE NO ACTION ON DELETE SET NULL,
                FOREIGN KEY(sourceRuleId) REFERENCES schedule_rules(id) ON UPDATE NO ACTION ON DELETE SET NULL,
                CHECK (reminderAbs IS NULL OR reminderRel IS NULL)
            )
        """)

        // 2. Transfer data
        db.execSQL("""
            INSERT INTO daily_instances_new (
                id, targetId, targetType, scheduledDate, titleSnapshot, descriptionSnapshot,
                plannedStartTime, plannedEndTime, plannedDurationMinutes, status, mobility,
                sourceRuleId, parentInstanceId, backlogId, actionProtocol
            )
            SELECT 
                id, targetId, targetType, scheduledDate, titleSnapshot, descriptionSnapshot,
                plannedStartTime, plannedEndTime, plannedDurationMinutes, status, mobility,
                sourceRuleId, parentInstanceId, backlogId, 'TIMER'
            FROM daily_instances
        """)

        // 3. Swap tables
        db.execSQL("DROP TABLE daily_instances")
        db.execSQL("ALTER TABLE daily_instances_new RENAME TO daily_instances")

        // 4. Recreate indices
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_daily_instances_sourceRuleId_scheduledDate ON daily_instances(sourceRuleId, scheduledDate)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_daily_instances_parentInstanceId ON daily_instances(parentInstanceId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_daily_instances_backlogId ON daily_instances(backlogId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_daily_instances_sourceRuleId ON daily_instances(sourceRuleId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_daily_instances_actionProtocol ON daily_instances(actionProtocol)")
    }
}

val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        // 1. Create new notes table with correct schema (Snapshots + SET NULL)
        db.execSQL("""
            CREATE TABLE notes_new (
                id TEXT NOT NULL PRIMARY KEY,
                content TEXT NOT NULL,
                definitionId TEXT,
                backlogId TEXT,
                instanceId TEXT,
                executionId TEXT,
                dateSnapshot INTEGER NOT NULL,
                targetTypeSnapshot TEXT,
                targetIdSnapshot TEXT,
                titleSnapshot TEXT,
                FOREIGN KEY(definitionId) REFERENCES activity_definitions(id) ON UPDATE NO ACTION ON DELETE SET NULL,
                FOREIGN KEY(backlogId) REFERENCES backlog_items(id) ON UPDATE NO ACTION ON DELETE SET NULL,
                FOREIGN KEY(instanceId) REFERENCES daily_instances(id) ON UPDATE NO ACTION ON DELETE SET NULL,
                FOREIGN KEY(executionId) REFERENCES activity_executions(id) ON UPDATE NO ACTION ON DELETE SET NULL,
                CHECK ((definitionId IS NOT NULL) + (backlogId IS NOT NULL) + (instanceId IS NOT NULL) + (executionId IS NOT NULL) <= 1)
            )
        """)

        // 2. Insert existing notes with complex backfill
        db.execSQL("""
            INSERT INTO notes_new (id, content, definitionId, backlogId, instanceId, executionId, dateSnapshot, targetTypeSnapshot, targetIdSnapshot, titleSnapshot)
            SELECT 
                n.id, 
                n.content, 
                n.definitionId, 
                n.backlogId, 
                n.instanceId, 
                NULL as executionId,
                COALESCE(di.scheduledDate, ad_date.today, bi_date.today, 0) as dateSnapshot,
                CASE 
                    WHEN n.instanceId IS NOT NULL THEN 'INSTANCE'
                    WHEN n.definitionId IS NOT NULL THEN 'DEFINITION'
                    WHEN n.backlogId IS NOT NULL THEN 'BACKLOG'
                    ELSE 'GLOBAL'
                END as targetTypeSnapshot,
                COALESCE(n.instanceId, n.definitionId, n.backlogId) as targetIdSnapshot,
                COALESCE(di.titleSnapshot, ad.title, bi.title, 'Nota') as titleSnapshot
            FROM notes n
            LEFT JOIN daily_instances di ON n.instanceId = di.id
            LEFT JOIN activity_definitions ad ON n.definitionId = ad.id
            LEFT JOIN backlog_items bi ON n.backlogId = bi.id
            CROSS JOIN (SELECT ${java.time.LocalDate.now().toEpochDay()} as today) ad_date
            CROSS JOIN (SELECT ${java.time.LocalDate.now().toEpochDay()} as today) bi_date
        """)

        // 3. Final safety check for dateSnapshot (no 0 allowed)
        db.execSQL("UPDATE notes_new SET dateSnapshot = ${java.time.LocalDate.now().toEpochDay()} WHERE dateSnapshot = 0")

        // 4. Swap tables
        db.execSQL("DROP TABLE notes")
        db.execSQL("ALTER TABLE notes_new RENAME TO notes")

        // 5. Indices
        db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_definitionId ON notes(definitionId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_backlogId ON notes(backlogId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_instanceId ON notes(instanceId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_executionId ON notes(executionId)")
    }
}

val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        // 1. Create new table with associatedInstanceId and SET NULL
        db.execSQL("""
            CREATE TABLE daily_instances_new (
                id TEXT NOT NULL PRIMARY KEY,
                targetId TEXT,
                targetType TEXT NOT NULL,
                scheduledDate INTEGER NOT NULL,
                titleSnapshot TEXT NOT NULL,
                descriptionSnapshot TEXT NOT NULL,
                plannedStartTime INTEGER,
                plannedEndTime INTEGER,
                plannedDurationMinutes INTEGER,
                status TEXT NOT NULL,
                mobility TEXT NOT NULL,
                sourceRuleId TEXT,
                parentInstanceId TEXT,
                backlogId TEXT,
                actionProtocol TEXT NOT NULL DEFAULT 'TIMER',
                reminderAbs INTEGER,
                reminderRel INTEGER,
                associatedInstanceId TEXT,
                FOREIGN KEY(parentInstanceId) REFERENCES daily_instances_new(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(backlogId) REFERENCES backlog_items(id) ON UPDATE NO ACTION ON DELETE SET NULL,
                FOREIGN KEY(sourceRuleId) REFERENCES schedule_rules(id) ON UPDATE NO ACTION ON DELETE SET NULL,
                FOREIGN KEY(associatedInstanceId) REFERENCES daily_instances_new(id) ON UPDATE NO ACTION ON DELETE SET NULL,
                CHECK (reminderAbs IS NULL OR reminderRel IS NULL)
            )
        """)

        // 2. Transfer data
        db.execSQL("""
            INSERT INTO daily_instances_new (
                id, targetId, targetType, scheduledDate, titleSnapshot, descriptionSnapshot,
                plannedStartTime, plannedEndTime, plannedDurationMinutes, status, mobility,
                sourceRuleId, parentInstanceId, backlogId, actionProtocol, reminderAbs, reminderRel
            )
            SELECT 
                id, targetId, targetType, scheduledDate, titleSnapshot, descriptionSnapshot,
                plannedStartTime, plannedEndTime, plannedDurationMinutes, status, mobility,
                sourceRuleId, parentInstanceId, backlogId, actionProtocol, reminderAbs, reminderRel
            FROM daily_instances
        """)

        // 3. Swap
        db.execSQL("DROP TABLE daily_instances")
        db.execSQL("ALTER TABLE daily_instances_new RENAME TO daily_instances")

        // 4. Indices
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_daily_instances_sourceRuleId_scheduledDate ON daily_instances(sourceRuleId, scheduledDate)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_daily_instances_parentInstanceId ON daily_instances(parentInstanceId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_daily_instances_backlogId ON daily_instances(backlogId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_daily_instances_sourceRuleId ON daily_instances(sourceRuleId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_daily_instances_actionProtocol ON daily_instances(actionProtocol)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_daily_instances_associatedInstanceId ON daily_instances(associatedInstanceId)")
    }
}
