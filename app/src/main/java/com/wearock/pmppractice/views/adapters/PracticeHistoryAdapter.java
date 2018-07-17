package com.wearock.pmppractice.views.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wearock.pmppractice.R;
import com.wearock.pmppractice.models.PracticeHistory;
import com.wearock.pmppractice.views.customize.CircleView;

import java.util.ArrayList;

public class PracticeHistoryAdapter extends BaseAdapter {

    private ArrayList<PracticeHistory> lstHistory;
    private Context context;
    private LayoutInflater inflater;

    public PracticeHistoryAdapter(ArrayList<PracticeHistory> list, Context context) {
        this.lstHistory = list;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return lstHistory.size();
    }

    @Override
    public Object getItem(int position) {
        return lstHistory.get(position);
    }

    @Override
    public long getItemId(int position) {
        return lstHistory.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HistoryViewHolder holder;
        if (convertView == null) {
            holder = new HistoryViewHolder();
            convertView = inflater.inflate(R.layout.layout_practice_history_item, null);
            holder.cvTotalScore = convertView.findViewById(R.id.cvTotalScore);
            holder.tvRate = convertView.findViewById(R.id.tvHistoryRate);
            holder.tvCreation = convertView.findViewById(R.id.tvCreation);
            convertView.setTag(holder);
        } else {
            holder = (HistoryViewHolder) convertView.getTag();
        }

        PracticeHistory curHistory = lstHistory.get(position);
        holder.cvTotalScore.setText(String.valueOf(curHistory.getTotalScore()));
        double correctRate = curHistory.getCorrectRate();
        if (correctRate >= 0.8) {
            holder.cvTotalScore.setBgColor(Color.GREEN);
        } else if (correctRate >= 0.6) {
            holder.cvTotalScore.setBgColor(Color.YELLOW);
        } else {
            holder.cvTotalScore.setBgColor(Color.RED);
        }
        holder.tvRate.setText(curHistory.getCorrectRateString());
        holder.tvCreation.setText(curHistory.getCreationTime());
        holder.practiceId = curHistory.getId();

        return convertView;
    }

    public static class HistoryViewHolder {
        public CircleView cvTotalScore;
        public TextView tvRate;
        public TextView tvCreation;
        public int practiceId;
    }

}
