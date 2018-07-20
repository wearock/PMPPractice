package com.wearock.pmppractice.controllers.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseIntArray;

import com.wearock.pmppractice.R;
import com.wearock.pmppractice.controllers.DBHelper;
import com.wearock.pmppractice.models.PracticeConfiguration;
import com.wearock.pmppractice.models.Question;
import com.wearock.pmppractice.models.QuestionBody;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class QuestionDAO {

    private Context curContext;
    private DBHelper dbHelper;

    public QuestionDAO(Context context, DBHelper dbHelper) {
        this.curContext = context;
        this.dbHelper = dbHelper;
    }

    public int getQuestionCount() throws DBHelper.DBAccessException {
        int count = 0;
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getReadableDatabase();

            Cursor cursor = db.rawQuery("select count(*) from question", new String[] {});
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
            cursor.close();
        } catch (Exception e) {
            throw new DBHelper.DBAccessException(
                    curContext.getResources().getString(R.string.err_dao_level_failure), e);
        } finally {
            if (db != null)
                db.close();
        }

        return count;
    }

    public Question[] getQuestionsByConfig(PracticeConfiguration config) throws DBHelper.DBAccessException {
        // Assemble query
        boolean bApplyQuery = false;
        StringBuilder builder = new StringBuilder();
        builder.append(" where");
        if (config.getSelectedDomains().size() > 0) {
            for (String domain : config.getSelectedDomains()) {
                builder.append(" sp.domain='");
                builder.append(domain);
                builder.append("' or");
            }
            builder.delete(builder.length() - 3, builder.length());
            bApplyQuery = true;
        }
        if (config.getQuestionSource() != PracticeConfiguration.SourceEnum.ALL) {
            if (bApplyQuery) {
                builder.append(" and");
            }
            builder.append(String.format(Locale.US, " q.source=%d", config.getQuestionSource().getValue()));
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getReadableDatabase();
            return queryQuestions(db, bApplyQuery ? builder.toString() : null, config.getQuestionCount(), true);
        } catch (Exception e) {
            throw new DBHelper.DBAccessException(
                    curContext.getResources().getString(R.string.err_dao_level_failure), e);
        } finally {
            if (db != null)
                db.close();
        }
    }

    public Question[] getQuestionsByIdList(ArrayList<Integer> lstIds) throws DBHelper.DBAccessException {
        // Assemble query
        StringBuilder builder = new StringBuilder();
        if (lstIds.size() > 0) {
            builder.append(" where");
            for (Integer qid : lstIds) {
                builder.append(" q.id=");
                builder.append(qid);
                builder.append(" or");
            }
            builder.delete(builder.length() - 3, builder.length());
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getReadableDatabase();
            return queryQuestions(db, builder.toString(), lstIds.size(), false);
        } catch (Exception e) {
            throw new DBHelper.DBAccessException(
                    curContext.getResources().getString(R.string.err_dao_level_failure), e);
        } finally {
            if (db != null)
                db.close();
        }
    }

    public Question getQuestionById(int qid) throws DBHelper.DBAccessException {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getReadableDatabase();
            Question[] lstQuestion = queryQuestions(db, " where q.id=" + qid, 1, false);
            return (lstQuestion.length == 0) ? null : lstQuestion[0];
        } catch (Exception e) {
            throw new DBHelper.DBAccessException(
                    curContext.getResources().getString(R.string.err_dao_level_failure), e);
        } finally {
            if (db != null)
                db.close();
        }
    }

    private Question[] queryQuestions(SQLiteDatabase db, String condition, int count, boolean random) {
        Question[] lstQuestions = new Question[count];
        String baseQuestionQuery = "select q.id,q.image,q.answer,sp.name as subproc,sp.process,sp.domain,q.knowledge_point,q.explanation " +
                "from question q join subproc sp on sp.id=q.subproc_id";
        SparseIntArray indexes;
        int curIndex = 0;

        Cursor cursor = db.rawQuery(baseQuestionQuery + condition, new String[] {});
        if (cursor.getCount() > 0) {

            if (random)
                indexes = randomIndexes(cursor.getCount(), count);
            else {
                indexes = new SparseIntArray();
                for (int i=0; i<count; i++) {
                    indexes.put(i, i);
                }
            }

            cursor.moveToFirst();
            do {
                if (indexes.indexOfKey(curIndex) >= 0) {
                    Question question = new Question();
                    question.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    question.setImage(cursor.getString(cursor.getColumnIndex("image")));
                    question.setAnswer(cursor.getInt(cursor.getColumnIndex("answer")));
                    question.setSubProcess(cursor.getString(cursor.getColumnIndex("subproc")));
                    question.setProcess(cursor.getString(cursor.getColumnIndex("process")));
                    question.setDomain(cursor.getString(cursor.getColumnIndex("domain")));
                    question.setKnowledgePoint(cursor.getString(cursor.getColumnIndex("knowledge_point")));
                    question.setExplanation(cursor.getString(cursor.getColumnIndex("explanation")));

                    queryBodies(db, question);

                    lstQuestions[indexes.get(curIndex)] = question;
                }

                curIndex++;
            } while (cursor.moveToNext());
        }
        cursor.close();

        return lstQuestions;
    }

    private void queryBodies(SQLiteDatabase db, Question question) {
        String bodyQuery = "select language,description,choice_a,choice_b,choice_c,choice_d from question_body where question_id=?";
        Cursor cursor = db.rawQuery(bodyQuery, new String[] { String.valueOf(question.getId()) });
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                QuestionBody body = new QuestionBody();
                body.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                body.setChoiceA(cursor.getString(cursor.getColumnIndex("choice_a")));
                body.setChoiceB(cursor.getString(cursor.getColumnIndex("choice_b")));
                body.setChoiceC(cursor.getString(cursor.getColumnIndex("choice_c")));
                body.setChoiceD(cursor.getString(cursor.getColumnIndex("choice_d")));

                question.putBody(QuestionBody.Language.values()[cursor.getInt(cursor.getColumnIndex("language"))],
                        body);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private SparseIntArray randomIndexes(int total, int count) {
        Random r = new Random();
        ArrayList<Integer> scope = new ArrayList<>();
        for (int i=0; i<total; i++) {
            scope.add(i);
        }

        SparseIntArray indexes = new SparseIntArray();
        for (int j=0; j<count; j++) {
            int index = r.nextInt(scope.size());
            indexes.put(scope.get(index), j);
            scope.remove(index);
        }

        return indexes;
    }
}
