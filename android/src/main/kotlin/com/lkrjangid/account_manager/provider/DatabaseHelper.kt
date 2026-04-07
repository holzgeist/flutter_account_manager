package com.lkrjangid.account_manager.provider

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DB_NAME = "account_manager.db"
private const val DB_VERSION = 1

/** SQLiteOpenHelper that creates all tables required by the sync framework. */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE accounts (
                id TEXT PRIMARY KEY,
                username TEXT NOT NULL,
                account_type TEXT NOT NULL,
                display_name TEXT,
                user_data TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                last_sync_at INTEGER,
                sync_enabled INTEGER DEFAULT 1,
                UNIQUE(username, account_type)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE sync_records (
                id TEXT PRIMARY KEY,
                account_id TEXT NOT NULL,
                started_at INTEGER NOT NULL,
                completed_at INTEGER,
                status TEXT NOT NULL,
                items_uploaded INTEGER DEFAULT 0,
                items_downloaded INTEGER DEFAULT 0,
                conflicts INTEGER DEFAULT 0,
                error_code INTEGER,
                error_message TEXT,
                FOREIGN KEY(account_id) REFERENCES accounts(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE sync_data (
                id TEXT PRIMARY KEY,
                account_id TEXT NOT NULL,
                entity_type TEXT NOT NULL,
                entity_id TEXT NOT NULL,
                data TEXT NOT NULL,
                version INTEGER DEFAULT 1,
                is_dirty INTEGER DEFAULT 0,
                is_deleted INTEGER DEFAULT 0,
                local_modified_at INTEGER NOT NULL,
                server_modified_at INTEGER,
                FOREIGN KEY(account_id) REFERENCES accounts(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX idx_accounts_type ON accounts(account_type)")
        db.execSQL("CREATE INDEX idx_sync_data_dirty ON sync_data(account_id, is_dirty)")
        db.execSQL("CREATE INDEX idx_sync_records_account ON sync_records(account_id, started_at DESC)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS sync_data")
        db.execSQL("DROP TABLE IF EXISTS sync_records")
        db.execSQL("DROP TABLE IF EXISTS accounts")
        onCreate(db)
    }
}
