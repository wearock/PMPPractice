package com.wearock.pmppractice.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.wearock.pmppractice.Application;
import com.wearock.pmppractice.R;
import com.wearock.pmppractice.controllers.DBHelper;
import com.wearock.pmppractice.controllers.dao.DomainDAO;
import com.wearock.pmppractice.controllers.dao.PracticeDAO;
import com.wearock.pmppractice.models.PracticeConfiguration;
import com.wearock.pmppractice.views.adapters.PracticeDomainAdapter;
import com.wearock.pmppractice.views.base.ManagedActivity;

import java.security.DomainCombiner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ConfigActivity extends ManagedActivity {

    private LinearLayout pQuestionCount;
    private LinearLayout pTimeLimit;
    private TextView tvQuestionCount;
    private TextView tvTimeLimit;
    private CheckBox chkAllDomains;
    private ListView lvPracticeDomains;

    private PracticeDomainAdapter adapter;
    private HashMap<String, Integer> mapDomainCounts;
    private ArrayList<String> lstDomainDisplays;
    private int maxQuestionCount;
    private int maxTimeLimit;
    private boolean bBlockEvent = false;

    private PracticeConfiguration config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        initControls();

        try {
            DomainDAO domainDAO = new DomainDAO(ConfigActivity.this,
                    Application.getInstance().getDBHelper(ConfigActivity.this));
            mapDomainCounts = domainDAO.getDomainQuestionCounts();
        } catch (DBHelper.DBAccessException dbe) {
            Toast.makeText(ConfigActivity.this, dbe.getMessage(), Toast.LENGTH_LONG).show();
            mapDomainCounts = new HashMap<>();
        }

        lstDomainDisplays = new ArrayList<>();
        Iterator iter = mapDomainCounts.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            lstDomainDisplays.add(String.format("%s (%d)", entry.getKey(), entry.getValue()));
        }

        adapter = new PracticeDomainAdapter(lstDomainDisplays, ConfigActivity.this);
        lvPracticeDomains.setAdapter(adapter);

        maxQuestionCount = calculateTotalQuestionCount();
        maxTimeLimit = maxQuestionCount + maxQuestionCount / 2;
        tvQuestionCount.setText(String.valueOf(maxQuestionCount));
        tvTimeLimit.setText(String.valueOf(maxTimeLimit));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_config, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuStartPractice:
                collectConfiguration();
                int newPracticeId = createPractice();
                if (-1 != newPracticeId) {
                    Intent iPractice = new Intent();
                    iPractice.setClass(ConfigActivity.this, PracticeActivity.class);
                    iPractice.putExtra("pid", newPracticeId);
                    startActivity(iPractice);
                    finish();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initControls() {
        pQuestionCount = findViewById(R.id.pQuestionCount);
        pTimeLimit = findViewById(R.id.pTimeLimit);
        tvQuestionCount = findViewById(R.id.tvQuestionCount);
        tvTimeLimit = findViewById(R.id.tvTimeLimit);
        chkAllDomains = findViewById(R.id.chkAllDomains);
        lvPracticeDomains = findViewById(R.id.lvPracticeDomains);

        pQuestionCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ConfigActivity.this);
                builder.setTitle(R.string.config_question_count);

                final NumberPicker npQuestionCount = new NumberPicker(ConfigActivity.this);
                npQuestionCount.setMinValue(1);
                npQuestionCount.setMaxValue(maxQuestionCount);
                npQuestionCount.setValue(Integer.valueOf(tvQuestionCount.getText().toString()));

                builder.setView(npQuestionCount);
                builder.setPositiveButton(R.string.dialog_positive_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int qCount = npQuestionCount.getValue();
                                tvQuestionCount.setText(String.valueOf(qCount));
                                maxTimeLimit = qCount + qCount / 2;
                                if (maxTimeLimit < Integer.valueOf(tvTimeLimit.getText().toString())) {
                                    tvTimeLimit.setText(String.valueOf(maxTimeLimit));
                                }

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
            }
        });

        pTimeLimit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ConfigActivity.this);
                builder.setTitle(R.string.config_time_limit);

                final NumberPicker npTimeLimit = new NumberPicker(ConfigActivity.this);
                npTimeLimit.setMinValue(0);
                npTimeLimit.setMaxValue(maxTimeLimit);
                npTimeLimit.setValue(Integer.valueOf(tvTimeLimit.getText().toString()));

                builder.setView(npTimeLimit);
                builder.setPositiveButton(R.string.dialog_positive_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                tvTimeLimit.setText(String.valueOf(npTimeLimit.getValue()));
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
            }
        });

        chkAllDomains.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (bBlockEvent) {
                    bBlockEvent = false;
                    return;
                }

                HashMap<String, Boolean> isSelected = adapter.getIsSelected();
                Iterator iter = isSelected.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry)iter.next();
                    isSelected.put(String.valueOf(entry.getKey()), checked);
                }
                adapter.setIsSelected(isSelected);
            }
        });

        lvPracticeDomains.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PracticeDomainAdapter.DomainSelectionViewHolder holder = (PracticeDomainAdapter.DomainSelectionViewHolder) view.getTag();
                holder.cb.toggle();
                adapter.getIsSelected().put(lstDomainDisplays.get(i), holder.cb.isChecked());

                maxQuestionCount = calculateTotalQuestionCount();
                int curCount = Integer.valueOf(tvQuestionCount.getText().toString());
                if (curCount > maxQuestionCount)
                    tvQuestionCount.setText(String.valueOf(maxQuestionCount));

                updateCheckAllStatus();
            }
        });
    }

    private int calculateTotalQuestionCount() {
        int total = 0;
        Iterator iter = adapter.getIsSelected().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            String displayName = entry.getKey().toString();
            String domainName = displayName.substring(0, displayName.indexOf("(") - 1);
            if ((boolean)entry.getValue())
                total += mapDomainCounts.get(domainName);
        }

        return total;
    }

    private void updateCheckAllStatus() {
        boolean isAllChecked = true;
        Iterator iter = adapter.getIsSelected().entrySet().iterator();
        while (iter.hasNext() && isAllChecked) {
            Map.Entry entry = (Map.Entry)iter.next();
            isAllChecked = (boolean)entry.getValue();
        }

        if (chkAllDomains.isChecked() != isAllChecked) {
            bBlockEvent = true;
            chkAllDomains.setChecked(isAllChecked);
        }
    }

    private void collectConfiguration() {
        ArrayList<String> lstSelectedDomains = new ArrayList<>();
        HashMap<String, Boolean> isSelected = adapter.getIsSelected();
        Iterator iter = isSelected.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            if ((Boolean) entry.getValue()) {
                String displayName = entry.getKey().toString();
                lstSelectedDomains.add(displayName.substring(0, displayName.indexOf("(") - 1));
            }
        }

        config = new PracticeConfiguration();
        config.setSelectedDomains(lstSelectedDomains);
        config.setQuestionCount(Integer.valueOf(tvQuestionCount.getText().toString()));
        config.setTimeLimit(Integer.valueOf(tvTimeLimit.getText().toString()));
    }

    private int createPractice() {
        int newPracticeId = -1;
        try {
            PracticeDAO practiceDAO = new PracticeDAO(ConfigActivity.this,
                    Application.getInstance().getDBHelper(ConfigActivity.this));
            newPracticeId = practiceDAO.createPractice(config);
        } catch (DBHelper.DBAccessException dbe) {
            Toast.makeText(ConfigActivity.this, dbe.getMessage(), Toast.LENGTH_LONG).show();
        }

        return newPracticeId;
    }
}
