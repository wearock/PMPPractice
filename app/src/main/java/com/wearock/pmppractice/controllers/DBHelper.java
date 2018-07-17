package com.wearock.pmppractice.controllers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.wearock.pmppractice.models.DBChangeLog;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "pmp_practice.db";
    private ArrayList<DBChangeLog> changeLogs;

    public DBHelper(Context context, int version, ArrayList<DBChangeLog> changeLogs) {
        super(context, DB_NAME, null, version);
        this.changeLogs = changeLogs;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table databasechangelog(id TEXT primary key, author TEXT)");
        for (DBChangeLog change : changeLogs ) {
            db.execSQL(String.format("insert into databasechangelog(id,author) values ('%s','%s')",
                    change.getId(), change.getAuthor()));
            db.execSQL(change.getStatement());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ArrayList<String> existingChangeIds = new ArrayList<>();
        Cursor cursor = db.rawQuery("select id from databasechangelog", new String[] {});
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                existingChangeIds.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        for (DBChangeLog change : changeLogs) {
            if (existingChangeIds.contains(change.getId()))
                continue;

            db.execSQL(String.format("insert into databasechangelog(id,author) values ('%s','%s')",
                    change.getId(), change.getAuthor()));
            db.execSQL(change.getStatement());
        }
    }

    public static class DBAccessException extends Exception {

        public DBAccessException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
