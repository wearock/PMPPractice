package com.wearock.pmppractice.controllers.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wearock.pmppractice.R;
import com.wearock.pmppractice.controllers.DBHelper;

import java.util.HashMap;

public class DomainDAO {

    private Context curContext;
    private DBHelper dbHelper;

    public DomainDAO(Context context, DBHelper dbHelper) {
        this.curContext = context;
        this.dbHelper = dbHelper;
    }

    public HashMap<String, Integer> getDomainQuestionCounts() throws DBHelper.DBAccessException {
        HashMap<String, Integer> dictCounts = new HashMap<>();

        SQLiteDatabase db = null;
        try {
            db = this.dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("select distinct sp.domain as domain, count(*) as qcount from question q left join subproc sp on sp.id=q.subproc_id group by sp.domain", new String[] {});
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    dictCounts.put(cursor.getString(cursor.getColumnIndex("domain")), cursor.getInt(cursor.getColumnIndex("qcount")));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            throw new DBHelper.DBAccessException(
                    curContext.getResources().getString(R.string.err_dao_level_failure), e);
        } finally {
            if (db != null)
                db.close();
        }

        return dictCounts;
    }
}
