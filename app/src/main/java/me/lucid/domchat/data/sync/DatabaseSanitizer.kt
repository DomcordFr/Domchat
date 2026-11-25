package me.lucid.domchat.data.sync

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import me.lucid.domchat.utils.LogUtil
import androidx.room.Room
import me.lucid.domchat.data.db.AppDatabase
import java.io.File

object DatabaseSanitizer {
    private const val TAG = "DatabaseSanitizer"

    /**
     * User-data tables copied into the clean Room database during restore.
     *
     * These are SQLite table names, which are not always the Kotlin entity names. Keep this
     * ordered like AppDatabase.entities so parent records are restored before dependent rows.
     * The backup round-trip test verifies this list against Room's current entity tables.
     */
    internal val PORTABLE_TABLES = listOf(
        "ConversationEntity",
        "MemoryEntity",
        "GenMediaEntity",
        "ChatEpisodeEntity",
        "embedding_cache",
        "daily_activity",
        "usage_stats",
        "chat_attachment",
        "conversation_attachment_ref",
        "workspaces",
        "memory_claim",
        "memory_claim_fts",
        "memory_episode_v3",
        "memory_episode_v3_fts",
        "memory_source_v3",
        "memory_source_v3_fts",
        "memory_ingest_state",
        "memory_projection",
    )

    val CHAT_TABLES = setOf(
        "ConversationEntity",
        "MemoryEntity",
        "GenMediaEntity",
        "ChatEpisodeEntity",
        "embedding_cache",
        "daily_activity",
        "usage_stats",
        "chat_attachment",
        "conversation_attachment_ref",
        "memory_claim",
        "memory_claim_fts",
        "memory_episode_v3",
        "memory_episode_v3_fts",
        "memory_source_v3",
        "memory_source_v3_fts",
        "memory_ingest_state",
        "memory_projection",
    )

    val WORKSPACE_TABLES = setOf(
        "workspaces",
    )

    val FTS_TABLES = setOf(
        "memory_claim_fts",
        "memory_episode_v3_fts",
        "memory_source_v3_fts",
    )

    data class SanitizationResult(
        val totalRows: Int = 0,
        val skippedRows: Int = 0,
        val skippedBytes: Long = 0,
        val issuesFixed: Int = 0,
        val details: String = ""
    ) {
        operator fun plus(other: SanitizationResult) = SanitizationResult(
            totalRows = this.totalRows + other.totalRows,
            skippedRows = this.skippedRows + other.skippedRows,
            skippedBytes = this.skippedBytes + other.skippedBytes,
            issuesFixed = this.issuesFixed + other.issuesFixed,
            details = (this.details + "\n" + other.details).trim()
        )
    }

    /**
     * Sanitizes the given source database file by copying valid data to a new database.
     * Returns the path to the sanitized database file.
     */
    fun sanitize(
        context: Context,
        sourceDbFile: File,
        targetDbName: String = "rikka_hub_sanitized",
        allowedTables: Set<String>? = null,
    ): Pair<File, SanitizationResult> {
        val targetDbFile = context.getDatabasePath(targetDbName)
        
        // Ensure clean state for target
        if (targetDbFile.exists()) {
            context.deleteDatabase(targetDbName)
        }

        // Migrate older backup database to current schema version before copying
        val migrationDbName = "backup_migration_${System.currentTimeMillis()}"
        val migrationDbFile = context.getDatabasePath(migrationDbName)
        if (migrationDbFile.exists()) {
            context.deleteDatabase(migrationDbName)
        }
        migrationDbFile.parentFile?.mkdirs()
        sourceDbFile.copyTo(migrationDbFile, overwrite = true)
        val sourceWal = File(sourceDbFile.parentFile, sourceDbFile.name + "-wal")
        if (sourceWal.exists()) {
            sourceWal.copyTo(File(migrationDbFile.parentFile, "$migrationDbName-wal"), overwrite = true)
        }
        val sourceShm = File(sourceDbFile.parentFile, sourceDbFile.name + "-shm")
        if (sourceShm.exists()) {
            sourceShm.copyTo(File(migrationDbFile.parentFile, "$migrationDbName-shm"), overwrite = true)
        }

        try {
            val migrationRoomDb = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                migrationDbName,
            )
                .addMigrations(*AppDatabase.ALL_MIGRATIONS)
                .allowMainThreadQueries()
                .build()
            try {
                val writable = migrationRoomDb.openHelper.writableDatabase
                writable.query("PRAGMA wal_checkpoint(TRUNCATE)").use { cursor ->
                    if (cursor.moveToFirst() && cursor.getInt(0) != 0) {
                        throw java.io.IOException("Sanitized DB WAL checkpoint busy")
                    }
                }
            } finally {
                migrationRoomDb.close()
            }
        } catch (e: Exception) {
            LogUtil.w(TAG, "Room migration on backup db failed or skipped: ${e.message}")
            context.deleteDatabase(migrationDbName)
        }

        // Initialize Target DB using Room to ensure schema creation
        val targetRoomDb = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            targetDbName
        )
            .allowMainThreadQueries() // Only for migration utility
            .build()
        
        // Force Open to create tables
        val targetDbInfo = targetRoomDb.openHelper.writableDatabase
        
        var totalResult = SanitizationResult()
        var sourceDb: SQLiteDatabase? = null

        try {
            val sourceFileToRead = if (migrationDbFile.exists()) migrationDbFile else sourceDbFile
            val db = SQLiteDatabase.openDatabase(
                sourceFileToRead.path,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
            sourceDb = db

            val tablesToCopy = if (allowedTables != null) {
                PORTABLE_TABLES.filter { it in allowedTables }
            } else {
                PORTABLE_TABLES
            }

            targetDbInfo.beginTransaction()
            try {
                for (table in tablesToCopy) {
                    if (table in FTS_TABLES) continue
                    // Check if table exists in source
                    try {
                        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", arrayOf(table))
                        val exists = cursor.count > 0
                        cursor.close()
                        
                        if (exists) {
                            val result = copyTable(db, targetDbInfo, table)
                            totalResult += result
                            LogUtil.i(TAG, "Sanitized table $table: $result")
                        } else {
                            LogUtil.w(TAG, "Table $table not found in source database, skipping")
                        }
                    } catch (e: Exception) {
                        LogUtil.e(TAG, "Failed to check/process table $table", e)
                    }
                }
                rebuildFtsTables(targetDbInfo)
                targetDbInfo.setTransactionSuccessful()
            } finally {
                targetDbInfo.endTransaction()
            }

        } catch (e: Exception) {
            LogUtil.e(TAG, "Critical error during sanitization", e)
            throw e
        } finally {
            sourceDb?.close()
            targetRoomDb.close()
            context.deleteDatabase(migrationDbName)
        }

        return targetDbFile to totalResult
    }

    fun rebuildFtsTables(target: androidx.sqlite.db.SupportSQLiteDatabase) {
        runCatching {
            target.execSQL("DELETE FROM memory_claim_fts")
            target.execSQL(
                "INSERT INTO memory_claim_fts(rowid, statement, subject, predicate, objectValue) " +
                "SELECT id, statement, subject, predicate, coalesce(object_value, '') FROM memory_claim"
            )
        }.onFailure { LogUtil.w(TAG, "Failed to rebuild memory_claim_fts", it) }

        runCatching {
            target.execSQL("DELETE FROM memory_episode_v3_fts")
            target.execSQL(
                "INSERT INTO memory_episode_v3_fts(rowid, title, summary) " +
                "SELECT id, title, summary FROM memory_episode_v3"
            )
        }.onFailure { LogUtil.w(TAG, "Failed to rebuild memory_episode_v3_fts", it) }

        runCatching {
            target.execSQL("DELETE FROM memory_source_v3_fts")
            target.execSQL(
                "INSERT INTO memory_source_v3_fts(rowid, excerpt) " +
                "SELECT id, excerpt FROM memory_source_v3"
            )
        }.onFailure { LogUtil.w(TAG, "Failed to rebuild memory_source_v3_fts", it) }
    }

    private fun copyTable(
        source: SQLiteDatabase,
        target: androidx.sqlite.db.SupportSQLiteDatabase,
        tableName: String
    ): SanitizationResult {
        var rows = 0
        var skipped = 0
        var skippedBytes = 0L
        
        var cursor: Cursor? = null
        try {
            cursor = source.query(tableName, null, null, null, null, null, null)
            
            // Get column names
            val columnNames = cursor.columnNames
            
            while (cursor.moveToNext()) {
                rows++
                try {
                    val values = ContentValues()
                    var rowBytes = 0L
                    
                    for (colName in columnNames) {
                        val index = cursor.getColumnIndex(colName)
                        when (cursor.getType(index)) {
                            Cursor.FIELD_TYPE_NULL -> values.putNull(colName)
                            Cursor.FIELD_TYPE_INTEGER -> values.put(colName, cursor.getLong(index))
                            Cursor.FIELD_TYPE_FLOAT -> values.put(colName, cursor.getDouble(index))
                            Cursor.FIELD_TYPE_STRING -> {
                                val str = cursor.getString(index)
                                values.put(colName, str)
                                rowBytes += str.length
                            }
                            Cursor.FIELD_TYPE_BLOB -> {
                                val blob = cursor.getBlob(index)
                                values.put(colName, blob)
                                rowBytes += blob.size
                            }
                        }
                    }
                    
                    target.insert(tableName, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE, values)
                    
                } catch (e: Exception) {
                    LogUtil.w(TAG, "Error copying row in $tableName", e)
                    skipped++
                    // We can't easily estimate bytes of a row specifically if we failed to read it, 
                    // but we can try to guess or just leave it.
                    // If the crash was in cursor.get...() then we missed it.
                }
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "Failed to query table $tableName", e)
            return SanitizationResult(totalRows = rows, skippedRows = rows, details = "Failed to read table $tableName: ${e.message}")
        } finally {
            cursor?.close()
        }
        
        return SanitizationResult(
            totalRows = rows, 
            skippedRows = skipped, 
            skippedBytes = skippedBytes,
            details = if (skipped > 0) "Skipped $skipped rows in $tableName" else ""
        )
    }

    /**
     * Creates a temporary sanitized copy of the live database containing only the specified tables.
     */
    fun createFilteredExportDatabase(
        context: Context,
        allowedTables: Set<String>,
    ): File {
        val liveDb = context.getDatabasePath(BackupArchiveFormat.DB_ENTRY)
        if (!liveDb.exists()) return liveDb
        val tempExportName = "export_db_${System.currentTimeMillis()}"
        val (file, _) = sanitize(
            context = context,
            sourceDbFile = liveDb,
            targetDbName = tempExportName,
            allowedTables = allowedTables,
        )
        return file
    }

    /**
     * Copies specific tables from one SQLite database file into another.
     * Used during partial restore to preserve non-restored live tables from being wiped.
     */
    fun copyTablesBetweenDatabases(
        sourceDbFile: File,
        targetDbFile: File,
        tables: List<String>,
    ) {
        if (!sourceDbFile.exists() || !targetDbFile.exists()) return
        var sourceDb: SQLiteDatabase? = null
        var targetDb: SQLiteDatabase? = null
        try {
            sourceDb = SQLiteDatabase.openDatabase(sourceDbFile.path, null, SQLiteDatabase.OPEN_READONLY)
            targetDb = SQLiteDatabase.openDatabase(targetDbFile.path, null, SQLiteDatabase.OPEN_READWRITE)
            targetDb.beginTransaction()
            try {
                for (table in tables) {
                    if (table in FTS_TABLES) continue
                    try {
                        val cursor = sourceDb.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", arrayOf(table))
                        val exists = cursor.count > 0
                        cursor.close()
                        if (exists) {
                            copyTableSqlite(sourceDb, targetDb, table)
                        }
                    } catch (e: Exception) {
                        LogUtil.e(TAG, "copyTablesBetweenDatabases: Failed for table $table", e)
                        throw e
                    }
                }
                rebuildFtsTablesSqlite(targetDb)
                targetDb.setTransactionSuccessful()
            } finally {
                targetDb.endTransaction()
            }
        } finally {
            sourceDb?.close()
            targetDb?.close()
        }
    }

    private fun rebuildFtsTablesSqlite(target: SQLiteDatabase) {
        runCatching {
            target.execSQL("DELETE FROM memory_claim_fts")
            target.execSQL(
                "INSERT INTO memory_claim_fts(rowid, statement, subject, predicate, objectValue) " +
                "SELECT id, statement, subject, predicate, coalesce(object_value, '') FROM memory_claim"
            )
        }.onFailure { LogUtil.w(TAG, "Failed to rebuild memory_claim_fts (sqlite)", it) }

        runCatching {
            target.execSQL("DELETE FROM memory_episode_v3_fts")
            target.execSQL(
                "INSERT INTO memory_episode_v3_fts(rowid, title, summary) " +
                "SELECT id, title, summary FROM memory_episode_v3"
            )
        }.onFailure { LogUtil.w(TAG, "Failed to rebuild memory_episode_v3_fts (sqlite)", it) }

        runCatching {
            target.execSQL("DELETE FROM memory_source_v3_fts")
            target.execSQL(
                "INSERT INTO memory_source_v3_fts(rowid, excerpt) " +
                "SELECT id, excerpt FROM memory_source_v3"
            )
        }.onFailure { LogUtil.w(TAG, "Failed to rebuild memory_source_v3_fts (sqlite)", it) }
    }

    private fun copyTableSqlite(
        source: SQLiteDatabase,
        target: SQLiteDatabase,
        tableName: String,
    ) {
        var cursor: Cursor? = null
        try {
            cursor = source.query(tableName, null, null, null, null, null, null)
            val columnNames = cursor.columnNames
            while (cursor.moveToNext()) {
                val values = ContentValues()
                for (colName in columnNames) {
                    val index = cursor.getColumnIndex(colName)
                    when (cursor.getType(index)) {
                        Cursor.FIELD_TYPE_NULL -> values.putNull(colName)
                        Cursor.FIELD_TYPE_INTEGER -> values.put(colName, cursor.getLong(index))
                        Cursor.FIELD_TYPE_FLOAT -> values.put(colName, cursor.getDouble(index))
                        Cursor.FIELD_TYPE_STRING -> values.put(colName, cursor.getString(index))
                        Cursor.FIELD_TYPE_BLOB -> values.put(colName, cursor.getBlob(index))
                    }
                }
                target.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
        } finally {
            cursor?.close()
        }
    }
}
