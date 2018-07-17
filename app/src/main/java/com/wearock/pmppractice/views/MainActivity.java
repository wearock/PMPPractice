package com.wearock.pmppractice.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wearock.pmppractice.Application;
import com.wearock.pmppractice.R;
import com.wearock.pmppractice.controllers.ActivityController;
import com.wearock.pmppractice.controllers.DBHelper;
import com.wearock.pmppractice.controllers.ViewHelper;
import com.wearock.pmppractice.controllers.dao.PracticeDAO;
import com.wearock.pmppractice.controllers.dao.QuestionDAO;
import com.wearock.pmppractice.models.PracticeHistory;
import com.wearock.pmppractice.views.adapters.PracticeHistoryAdapter;
import com.wearock.pmppractice.views.base.ManagedActivity;

import java.util.ArrayList;

public class MainActivity extends ManagedActivity {

    public static String ACTION_EXIT_APP = "com.wearock.pmppractice.broadcast.EXITAPP";

    private ImageView ivMainHeader;
    private TextView tvQuestionCount;
    private TextView tvPracticeCount;
    private TextView tvHighestRate;
    private ListView lvPracticeHistory;
    private Button btnNewPractice;

    private ArrayList<PracticeHistory> lstHistoryRecords;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ActivityController.finishAll();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(ACTION_EXIT_APP);
        registerReceiver(receiver, iFilter);

        Intent iSplush = new Intent();
        iSplush.setClass(MainActivity.this, SplashActivity.class);
        startActivity(iSplush);

        ivMainHeader = findViewById(R.id.ivMainHeader);
        ivMainHeader.setImageBitmap(ViewHelper.readBitmap(MainActivity.this, R.drawable.main_header));

        tvQuestionCount = findViewById(R.id.tvTotalQuestionCount);
        tvPracticeCount = findViewById(R.id.tvTotalPracticeCount);
        tvHighestRate = findViewById(R.id.tvHighestRate);

        lvPracticeHistory = findViewById(R.id.lvPracticeHistory);
        lvPracticeHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent redirect = new Intent();
                redirect.setClass(MainActivity.this, ScoreActivity.class);
                redirect.putExtra("score", lstHistoryRecords.get(i));
                startActivity(redirect);
            }
        });

        btnNewPractice = findViewById(R.id.btnNewPractice);
        btnNewPractice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iConfig = new Intent();
                iConfig.setClass(MainActivity.this, ConfigActivity.class);
                startActivity(iConfig);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        DBHelper dbHelper = Application.getInstance().getDBHelper(MainActivity.this);
        if (dbHelper == null)
            return;

        try {
            QuestionDAO questionDAO = new QuestionDAO(MainActivity.this, dbHelper);
            tvQuestionCount.setText(String.valueOf(questionDAO.getQuestionCount()));

            PracticeDAO practiceDAO = new PracticeDAO(MainActivity.this, dbHelper);
            lstHistoryRecords = practiceDAO.getHistoryRecords();
            tvPracticeCount.setText(String.valueOf(lstHistoryRecords.size()));
            lvPracticeHistory.setAdapter(new PracticeHistoryAdapter(lstHistoryRecords, MainActivity.this));

            double highestRate = 0.0;
            for (PracticeHistory history : lstHistoryRecords) {
                if (history.getCorrectRate() >= highestRate) {
                    highestRate = history.getCorrectRate();
                    tvHighestRate.setText(history.getCorrectRateString());
                }
            }
        }
        catch (DBHelper.DBAccessException e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
