package com.wearock.pmppractice.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.wearock.pmppractice.Application;
import com.wearock.pmppractice.R;
import com.wearock.pmppractice.controllers.DBHelper;
import com.wearock.pmppractice.controllers.dao.QuestionDAO;
import com.wearock.pmppractice.models.Answer;
import com.wearock.pmppractice.models.PracticeHistory;
import com.wearock.pmppractice.models.Question;
import com.wearock.pmppractice.models.ScoreDomain;
import com.wearock.pmppractice.views.adapters.DomainScoreAdapter;
import com.wearock.pmppractice.views.base.ManagedActivity;

import java.util.ArrayList;

public class ScoreActivity extends ManagedActivity {

    private PracticeHistory score;

    private TextView tvTotalScore;
    private TextView tvCreation;
    private TextView tvCompletion;
    private ListView lvScoreDomains;
    private Button btnDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        Intent intent = getIntent();
        score = (PracticeHistory) intent.getSerializableExtra("score");

        tvTotalScore = findViewById(R.id.tvScoreTotal);
        tvTotalScore.setText(String.valueOf(score.getTotalScore()));

        tvCreation = findViewById(R.id.tvScoreCreation);
        tvCreation.setText(String.valueOf(score.getCreationTime()));

        tvCompletion = findViewById(R.id.tvScoreCompletion);
        tvCompletion.setText(String.valueOf(score.getCompletionTime()));

        View header = getLayoutInflater().inflate(R.layout.layout_score_domain_item, null);
        ((TextView) header.findViewById(R.id.tvColumnDomain)).setText("知识领域");
        ((TextView) header.findViewById(R.id.tvColumnTotal)).setText("总题数");
        ((TextView) header.findViewById(R.id.tvColumnCorrect)).setText("正确题数");
        ((TextView) header.findViewById(R.id.tvColumnCorrectness)).setText("正确率");

        lvScoreDomains = findViewById(R.id.lvScoreDomains);
        lvScoreDomains.setAdapter(new DomainScoreAdapter(getScoreDomains(), ScoreActivity.this));
        lvScoreDomains.addHeaderView(header);

        btnDetails = findViewById(R.id.btnQuestionDetails);
        btnDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent redirect = new Intent();
                redirect.setClass(ScoreActivity.this, PracticeActivity.class);
                redirect.putExtra("score", score);
                startActivity(redirect);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_score, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuExitScore:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ArrayList<ScoreDomain> getScoreDomains() {
        QuestionDAO questionsDAO = new QuestionDAO(ScoreActivity.this,
                Application.getInstance().getDBHelper(ScoreActivity.this));

        ArrayList<ScoreDomain> lstScoreDomains = new ArrayList<>();
        ScoreDomain totalDomain = new ScoreDomain();
        totalDomain.setDomain("总计");

        try {
            for (Answer answer : score.getAnswers()) {
                Question question = questionsDAO.getQuestionById(answer.getQuestionId());
                ScoreDomain scoreDomain = null;
                for (ScoreDomain sd : lstScoreDomains) {
                    if (sd.getDomain().equalsIgnoreCase(question.getDomain())) {
                        scoreDomain = sd;
                        break;
                    }
                }

                if (scoreDomain == null) {
                    scoreDomain = new ScoreDomain();
                    scoreDomain.setDomain(question.getDomain());
                    lstScoreDomains.add(scoreDomain);
                }

                scoreDomain.addTotalCount(1);
                totalDomain.addTotalCount(1);
                if (answer.getAnswer() == question.getAnswer()) {
                    scoreDomain.addCorrectCount(1);
                    totalDomain.addCorrectCount(1);
                }

            }
        } catch (DBHelper.DBAccessException dbe) {
            // eat the exception here
            dbe.printStackTrace();
        }

        lstScoreDomains.add(totalDomain);
        return lstScoreDomains;
    }
}
