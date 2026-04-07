package com.lkrjangid.account_manager.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

private const val AUTHORITY = "com.lkrjangid.account_manager.provider"
private const val ACCOUNTS = 1
private const val SYNC_DATA = 2

/**
 * ContentProvider required by the Android SyncAdapter framework.
 * Provides structured access to the local SQLite sync database.
 */
class AppContentProvider : ContentProvider() {

    private lateinit var dbHelper: DatabaseHelper

    companion object {
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY")

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "accounts", ACCOUNTS)
            addURI(AUTHORITY, "sync_data", SYNC_DATA)
        }
    }

    override fun onCreate(): Boolean {
        dbHelper = DatabaseHelper(context!!)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor {
        val db = dbHelper.readableDatabase
        val table = when (uriMatcher.match(uri)) {
            ACCOUNTS -> "accounts"
            SYNC_DATA -> "sync_data"
            else -> return MatrixCursor(emptyArray())
        }
        return db.query(table, projection, selection, selectionArgs, null, null, sortOrder)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = dbHelper.writableDatabase
        val table = when (uriMatcher.match(uri)) {
            ACCOUNTS -> "accounts"
            SYNC_DATA -> "sync_data"
            else -> return null
        }
        db.insertOrThrow(table, null, values)
        context?.contentResolver?.notifyChange(uri, null)
        return uri
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int {
        val db = dbHelper.writableDatabase
        val table = when (uriMatcher.match(uri)) {
            ACCOUNTS -> "accounts"
            SYNC_DATA -> "sync_data"
            else -> return 0
        }
        val count = db.update(table, values, selection, selectionArgs)
        context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int {
        val db = dbHelper.writableDatabase
        val table = when (uriMatcher.match(uri)) {
            ACCOUNTS -> "accounts"
            SYNC_DATA -> "sync_data"
            else -> return 0
        }
        val count = db.delete(table, selection, selectionArgs)
        context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun getType(uri: Uri): String = when (uriMatcher.match(uri)) {
        ACCOUNTS -> "vnd.android.cursor.dir/vnd.$AUTHORITY.accounts"
        SYNC_DATA -> "vnd.android.cursor.dir/vnd.$AUTHORITY.sync_data"
        else -> throw IllegalArgumentException("Unknown URI: $uri")
    }
}
