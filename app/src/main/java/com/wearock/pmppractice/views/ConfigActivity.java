package com.wearock.pmppractice.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConfigActivity extends ManagedActivity {

    private TextView tvQuestionSource;
    private TextView tvQuestionCount;
    private TextView tvTimeLimit;
    private CheckBox chkAllDomains;
    private ListView lvPracticeDomains;

    private PracticeDomainAdapter adapter;
    private HashMap<String, Integer> mapDomainCounts;
    private ArrayList<String> lstDomainDisplays;
    private AlertDialog dialog;
    private int maxQuestionCount;
    private int maxTimeLimit;
    private boolean bBlockEvent = false;

    private DomainDAO domainDAO;
    private PracticeConfiguration config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        initControls();

        domainDAO = new DomainDAO(ConfigActivity.this,
                Application.getInstance().getDBHelper(ConfigActivity.this));
        tvQuestionSource.setText(PracticeConfiguration.SourceEnum.ALL.getName());
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
        tvQuestionSource = findViewById(R.id.tvQuestionSource);
        tvQuestionCount = findViewById(R.id.tvQuestionCount);
        tvTimeLimit = findViewById(R.id.tvTimeLimit);
        chkAllDomains = findViewById(R.id.chkAllDomains);
        lvPracticeDomains = findViewById(R.id.lvPracticeDomains);

        tvQuestionSource.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                PracticeConfiguration.SourceEnum source = PracticeConfiguration.SourceEnum.fromName(charSequence.toString());
                try {
                    mapDomainCounts = domainDAO.getDomainQuestionCounts(source);
                } catch (DBHelper.DBAccessException dbe) {
                    Toast.makeText(ConfigActivity.this, dbe.getMessage(), Toast.LENGTH_LONG).show();
                    mapDomainCounts = new HashMap<>();
                }

                lstDomainDisplays = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : mapDomainCounts.entrySet()) {
                    lstDomainDisplays.add(String.format(Locale.US, "%s (%d)", entry.getKey(), entry.getValue()));
                }

                adapter = new PracticeDomainAdapter(lstDomainDisplays, ConfigActivity.this);
                lvPracticeDomains.setAdapter(adapter);

                maxQuestionCount = calculateTotalQuestionCount();
                tvQuestionCount.setText(String.valueOf(maxQuestionCount));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        tvQuestionCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int qCount = Integer.valueOf(charSequence.toString());
                maxTimeLimit = qCount + qCount / 2;
                tvTimeLimit.setText(String.valueOf(maxTimeLimit));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        findViewById(R.id.pQuestionSource).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ConfigActivity.this);
                builder.setTitle(R.string.config_question_source);

                ListView lvSource = new ListView(ConfigActivity.this);
                final List<String> sourceList = new ArrayList<>();
                for (PracticeConfiguration.SourceEnum se : PracticeConfiguration.SourceEnum.values()) {
                    sourceList.add(se.getName());
                }
                lvSource.setAdapter(new ArrayAdapter<>(ConfigActivity.this,
                        android.R.layout.simple_list_item_1, sourceList));
                lvSource.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        tvQuestionSource.setText(sourceList.get(i));
                        dialog.dismiss();
                    }
                });

                builder.setView(lvSource);
                builder.setNegativeButton(R.string.dialog_negative_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                dialog = builder.create();
                dialog.show();
            }
        });

        findViewById(R.id.pQuestionCount).setOnClickListener(new View.OnClickListener() {
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

        findViewById(R.id.pTimeLimit).setOnClickListener(new View.OnClickListener() {
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
                for (Map.Entry<String, Boolean> entry : isSelected.entrySet()) {
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
        for (Map.Entry<String, Boolean> entry : adapter.getIsSelected().entrySet()) {
            String displayName = entry.getKey();
            String domainName = displayName.substring(0, displayName.indexOf("(") - 1);
            if (entry.getValue())
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
        for (Map.Entry<String, Boolean> entry : isSelected.entrySet()) {
            if (entry.getValue()) {
                String displayName = entry.getKey();
                lstSelectedDomains.add(displayName.substring(0, displayName.indexOf("(") - 1));
            }
        }

        config = new PracticeConfiguration();
        config.setQuestionSource(PracticeConfiguration.SourceEnum.fromName(tvQuestionSource.getText().toString()));
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
