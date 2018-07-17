package com.wearock.pmppractice.controllers.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wearock.pmppractice.R;
import com.wearock.pmppractice.controllers.DBHelper;
import com.wearock.pmppractice.models.PracticeConfiguration;

import java.util.HashMap;
import java.util.Locale;

public class DomainDAO {

    private Context curContext;
    private DBHelper dbHelper;

    public DomainDAO(Context context, DBHelper dbHelper) {
        this.curContext = context;
        this.dbHelper = dbHelper;
    }

    public HashMap<String, Integer> getDomainQuestionCounts(PracticeConfiguration.SourceEnum source) throws DBHelper.DBAccessException {
        HashMap<String, Integer> dictCounts = new HashMap<>();
        String sourceFilter = "";
        if (source != PracticeConfiguration.SourceEnum.ALL) {
            sourceFilter = String.format(Locale.US, " where q.source=%d", source.getValue());
        }

        SQLiteDatabase db = null;
        try {
            db = this.dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("select distinct sp.domain as domain, count(*) as qcount from question q left join subproc sp on sp.id=q.subproc_id"
                    + sourceFilter + " group by sp.domain", new String[] {});
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    dictCounts.put(cursor.getString(cursor.getColumnIndex("domain")), cursor.getInt(cursor.getColumnIndex("qcount")));
                } while (cursor.moveToNext());
            }
            cursor.close();
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
