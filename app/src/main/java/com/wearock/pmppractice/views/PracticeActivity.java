package com.wearock.pmppractice.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.wearock.pmppractice.Application;
import com.wearock.pmppractice.R;
import com.wearock.pmppractice.controllers.DBHelper;
import com.wearock.pmppractice.controllers.dao.PracticeDAO;
import com.wearock.pmppractice.controllers.dao.QuestionDAO;
import com.wearock.pmppractice.models.Answer;
import com.wearock.pmppractice.models.PracticeHistory;
import com.wearock.pmppractice.models.Question;
import com.wearock.pmppractice.models.QuestionBody;
import com.wearock.pmppractice.models.QuestionStatus;
import com.wearock.pmppractice.views.adapters.PracticeQuestionAdapter;
import com.wearock.pmppractice.views.adapters.QuestionStatusAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class PracticeActivity extends AppCompatActivity {

    private ViewPager vpBodyPager;
    private SeekBar sbProgress;
    private TextView tvPracticeStatus;
    private TextView tvTimeLeft;
    private AlertDialog dlgStatus;

    private Timer timer;
    private TimerTask task;
    private static Handler handler;
    private int secondsLeft;

    private PracticeHistory score;
    private Question[] lstQuestions;
    private boolean editable;
    private QuestionBody.Language curLanguage;
    private PracticeQuestionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        PracticeDAO practiceDAO = new PracticeDAO(PracticeActivity.this,
                Application.getInstance().getDBHelper(PracticeActivity.this));
        QuestionDAO questionDAO = new QuestionDAO(PracticeActivity.this,
                Application.getInstance().getDBHelper(PracticeActivity.this));
        try {
            Intent intent = getIntent();
            score = (PracticeHistory) intent.getSerializableExtra("score");
            if (score == null) {
                score = practiceDAO.getHistoryById(intent.getIntExtra("pid", 0));
                editable = true;
                secondsLeft = score.getConfiguration().getTimeLimit() * 60;
                lstQuestions = questionDAO.getQuestionsByConfig(score.getConfiguration());
            } else {
                editable = false;
                secondsLeft = 0;

                ArrayList<Integer> lstIds = new ArrayList<>();
                for (Answer answer : score.getAnswers()) {
                    lstIds.add(answer.getQuestionId());
                }
                lstQuestions = questionDAO.getQuestionsByIdList(lstIds);
            }
        } catch (DBHelper.DBAccessException dbe) {
            Toast.makeText(PracticeActivity.this, dbe.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        initControls();

        curLanguage = QuestionBody.Language.Chinese;

        task = new TimerTask() {
            @Override
            public void run() {
                secondsLeft -= 1;
                handler.sendEmptyMessage(secondsLeft);
            }
        };
        handler = new Handler() {
            public void handleMessage(Message msg) {
                tvTimeLeft.setText(getTimeLeftString());
                if (secondsLeft == 0) {
                    timer.cancel();

                    AlertDialog.Builder builder = new AlertDialog.Builder(PracticeActivity.this);
                    TextView tvContent = new TextView(PracticeActivity.this);
                    tvContent.setText(R.string.practice_timed_out);

                    builder.setView(tvContent);
                    builder.setPositiveButton(R.string.dialog_positive_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    submitResults(true);

                                    Intent iScore = new Intent();
                                    iScore.setClass(PracticeActivity.this, ScoreActivity.class);
                                    iScore.putExtra("score", score);
                                    startActivity(iScore);

                                    finish();
                                    dialogInterface.dismiss();
                                }
                            });
                    builder.create().show();
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (secondsLeft > 0) {
            timer = new Timer();
            timer.schedule(task, 0, 1000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_practice, menu);
        for (int i=0; i<menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            switch (item.getItemId()) {
                case R.id.menuLeavePractice:
                    item.setTitle(editable ? R.string.practice_menu_submit : R.string.practice_menu_exit);
                    break;
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuLeavePractice:
                if (editable && submitResults(false)) {
                    Intent iScore = new Intent();
                    iScore.setClass(PracticeActivity.this, ScoreActivity.class);
                    iScore.putExtra("score", score);
                    startActivity(iScore);
                }
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public QuestionBody.Language getCurLanguage() {
        return curLanguage;
    }

    public void receiveAnswer(Answer answer) {
        score.attachAnswer(answer);

        int curProgress = sbProgress.getProgress();
        sbProgress.setProgress(curProgress + 1);

        tvPracticeStatus.setText(getStatusString());
    }

    private void initControls() {
        vpBodyPager = findViewById(R.id.vpBodyPager);
        sbProgress = findViewById(R.id.sbProgress);
        tvPracticeStatus = findViewById(R.id.tvPracticeStatus);
        tvTimeLeft = findViewById(R.id.tvTimeLeft);
        Switch swhLanguage = findViewById(R.id.swhLanguage);
        LinearLayout pPracticeStatus = findViewById(R.id.pPracticeStatus);

        swhLanguage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                curLanguage = checked ? QuestionBody.Language.Chinese : QuestionBody.Language.English;

                PracticeQuestionFragment frag = (PracticeQuestionFragment) adapter.getItem(sbProgress.getProgress());
                frag.updateQuestionBody();
            }
        });

        tvPracticeStatus.setText(getStatusString());
        pPracticeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<QuestionStatus> lstStatuses = new ArrayList<>();
                for (int i=0; i<lstQuestions.length; i++) {
                    QuestionStatus newStatus = new QuestionStatus();
                    newStatus.setIndex(i + 1);

                    Answer curAnswer = score.getAnswerByQuestionId(lstQuestions[i].getId());
                    if (curAnswer == null) {
                        newStatus.setAnswered(false);
                        newStatus.setCorrect(false);
                    } else {
                        newStatus.setAnswered(true);
                        newStatus.setCorrect(curAnswer.getAnswer() == lstQuestions[i].getAnswer());
                    }

                    lstStatuses.add(newStatus);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(PracticeActivity.this);

                View dlgRoot = LayoutInflater.from(PracticeActivity.this).inflate(
                        R.layout.layout_question_status_dialog, null);
                GridView gvQuestionStatuses = dlgRoot.findViewById(R.id.gvQuestionStatuses);
                gvQuestionStatuses.setAdapter(new QuestionStatusAdapter(lstStatuses,
                        editable ? QuestionStatusAdapter.StatusType.ANSWERED : QuestionStatusAdapter.StatusType.CORRECTNESS,
                        PracticeActivity.this));
                gvQuestionStatuses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        sbProgress.setProgress(i + 1);
                        dlgStatus.dismiss();
                    }
                });

                builder.setView(dlgRoot);
                builder.setNegativeButton(R.string.dialog_negative_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

                dlgStatus = builder.create();
                dlgStatus.show();
            }
        });

        sbProgress.setMax(lstQuestions.length);
        sbProgress.setProgress(1);
        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                try {
                    if (i == 0) {
                        sbProgress.setProgress(1);
                    } else {
                        vpBodyPager.setCurrentItem(i - 1);
                    }
                }catch (IllegalStateException e) {
                    // FragmentManager is already executing transactions
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        List<Fragment> fragments = new ArrayList<>();
        for (Question question : lstQuestions) {
            if (question == null) {
                continue;
            }
            Answer exitAnswer = score.getAnswerByQuestionId(question.getId());
            fragments.add(PracticeQuestionFragment.newInstance(question,
                    (exitAnswer == null) ? -1 : exitAnswer.getAnswer(), editable));
        }

        adapter = new PracticeQuestionAdapter(getSupportFragmentManager(), fragments);
        vpBodyPager.setAdapter(adapter);
        vpBodyPager.addOnPageChangeListener(new PracticePagerChangeListener());
        vpBodyPager.setCurrentItem(0);
        if (fragments.size() > 1) {
            vpBodyPager.setOffscreenPageLimit(fragments.size() - 1);
        }
    }

    private String getTimeLeftString() {
        String minute = String.valueOf(secondsLeft / 60);
        String seconds = String.valueOf(secondsLeft % 60);
        if (minute.length() < 2)
            minute = "0" + minute;
        if (seconds.length() < 2)
            seconds = "0" + seconds;
        return minute + ":" + seconds;
    }

    private String getStatusString() {
        int answered = (null == score.getAnswers()) ? 0 : score.getAnswers().length;
        return String.format(Locale.US, "%d/%d", answered, lstQuestions.length);
    }

    private boolean submitResults(boolean force) {
        if ((null != score.getAnswers() && score.getAnswers().length == lstQuestions.length) || force) {
            collectAndUpdateResults();
            return true;
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(PracticeActivity.this);

            TextView tvContent = new TextView(PracticeActivity.this);
            tvContent.setText(R.string.practice_unfinished);

            builder.setView(tvContent);
            builder.setPositiveButton(R.string.dialog_positive_button,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            collectAndUpdateResults();
                            dialogInterface.dismiss();
                        }
                    });
            builder.setNegativeButton(R.string.dialog_negative_button,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            builder.create().show();
            return false;
        }
    }

    private void collectAndUpdateResults() {
        int totalScore = 0;
        for (Question q : lstQuestions) {
            Answer curAnswer = score.getAnswerByQuestionId(q.getId());
            if (curAnswer == null) {
                curAnswer = new Answer();
                curAnswer.setQuestionId(q.getId());
                curAnswer.setAnswer(-1);
                score.attachAnswer(curAnswer);
            } else if (curAnswer.getAnswer() == q.getAnswer()) {
                totalScore++;
            }
        }

        score.setTotalScore(totalScore);

        PracticeDAO practiceDAO = new PracticeDAO(PracticeActivity.this,
                Application.getInstance().getDBHelper(PracticeActivity.this));
        try {
            practiceDAO.updateResults(score);
        } catch (DBHelper.DBAccessException dbe) {
            Toast.makeText(PracticeActivity.this, dbe.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public class PracticePagerChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
            try {
                sbProgress.setProgress(arg0 + 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
