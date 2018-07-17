package com.wearock.pmppractice.controllers.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wearock.pmppractice.R;
import com.wearock.pmppractice.controllers.DBHelper;
import com.wearock.pmppractice.controllers.JSONHelper;
import com.wearock.pmppractice.models.Answer;
import com.wearock.pmppractice.models.PracticeConfiguration;
import com.wearock.pmppractice.models.PracticeHistory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class PracticeDAO {

    private Context curContext;
    private DBHelper dbHelper;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public PracticeDAO(Context context, DBHelper dbHelper) {
        this.curContext = context;
        this.dbHelper = dbHelper;
    }

    public int createPractice(PracticeConfiguration config) throws DBHelper.DBAccessException {
        int newPracticeId = -1;
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getReadableDatabase();
            db.execSQL("insert into practice_history(configuration,creation) values (?,?)",
                    new String[] { PracticeConfiguration.toJsonString(config), sdf.format(Calendar.getInstance().getTime()) });

            Cursor cursor = db.rawQuery("select id from practice_history order by id desc", new String[] {});
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                newPracticeId = cursor.getInt(0);
            }
            cursor.close();
            db.close();
        } catch (Exception e) {
            throw new DBHelper.DBAccessException(
                    curContext.getResources().getString(R.string.err_dao_level_failure), e);
        } finally {
            if (db != null)
                db.close();
        }

        return newPracticeId;
    }

    public void updateResults(PracticeHistory result) throws DBHelper.DBAccessException {
        result.setCompletionTime(sdf.format(Calendar.getInstance().getTime()));
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getReadableDatabase();
            db.execSQL("update practice_history set score=?,results=?,completion=? where id=?",
                    new String[] { String.valueOf(result.getTotalScore()),
                            JSONHelper.toJSON(result.getAnswers()),
                            result.getCompletionTime(),
                            String.valueOf(result.getId()) });
        } catch (Exception e) {
            throw new DBHelper.DBAccessException(
                    curContext.getResources().getString(R.string.err_dao_level_failure), e);
        } finally {
            if (db != null)
                db.close();
        }
    }

    public ArrayList<PracticeHistory> getHistoryRecords() throws DBHelper.DBAccessException {
        ArrayList<PracticeHistory> lstRecords = new ArrayList<>();
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("select id,score,configuration,results,creation,completion from practice_history order by id desc",
                    new String[] { });
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                do {
                    PracticeHistory history = new PracticeHistory();
                    history.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    history.setTotalScore(cursor.getInt(cursor.getColumnIndex("score")));
                    history.setConfiguration(JSONHelper.parseObject(cursor.getString(cursor.getColumnIndex("configuration")),
                            PracticeConfiguration.class));
                    history.setAnswers(JSONHelper.parseArray(cursor.getString(cursor.getColumnIndex("results")), Answer.class));
                    history.setCreationTime(cursor.getString(cursor.getColumnIndex("creation")));
                    history.setCompletionTime(cursor.getString(cursor.getColumnIndex("completion")));

                    lstRecords.add(history);
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

        return lstRecords;
    }

    public PracticeHistory getHistoryById(int pid) throws DBHelper.DBAccessException {
        PracticeHistory history = null;
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("select score,configuration,results,creation,completion from practice_history where id=?",
                    new String[] { String.valueOf(pid) });
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                history = new PracticeHistory();
                history.setId(pid);
                history.setTotalScore(cursor.getInt(cursor.getColumnIndex("score")));
                history.setConfiguration(JSONHelper.parseObject(cursor.getString(cursor.getColumnIndex("configuration")),
                        PracticeConfiguration.class));
                history.setAnswers(JSONHelper.parseArray(cursor.getString(cursor.getColumnIndex("results")), Answer.class));
                history.setCreationTime(cursor.getString(cursor.getColumnIndex("creation")));
                history.setCompletionTime(cursor.getString(cursor.getColumnIndex("completion")));
            }
            cursor.close();
        } catch (Exception e) {
            throw new DBHelper.DBAccessException(
                    curContext.getResources().getString(R.string.err_dao_level_failure), e);
        } finally {
            if (db != null)
                db.close();
        }

        return history;
    }

}
