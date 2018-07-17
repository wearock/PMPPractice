package com.wearock.pmppractice.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wearock.pmppractice.R;
import com.wearock.pmppractice.models.ScoreDomain;

import java.util.ArrayList;

public class DomainScoreAdapter extends BaseAdapter {

    private ArrayList<ScoreDomain> lstScoreDomains;
    private Context context;
    private LayoutInflater inflater;

    public DomainScoreAdapter(ArrayList<ScoreDomain> list, Context context) {
        this.lstScoreDomains = list;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return lstScoreDomains.size();
    }

    @Override
    public Object getItem(int position) {
        return lstScoreDomains.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DomainScoreViewHolder holder;
        if (convertView == null) {
            holder = new DomainScoreViewHolder();
            convertView = inflater.inflate(R.layout.layout_score_domain_item, null);
            holder.tvDomain = convertView.findViewById(R.id.tvColumnDomain);
            holder.tvTotalCount = convertView.findViewById(R.id.tvColumnTotal);
            holder.tvCorrectCount = convertView.findViewById(R.id.tvColumnCorrect);
            holder.tvCorrectRate = convertView.findViewById(R.id.tvColumnCorrectness);
            convertView.setTag(holder);
        } else {
            holder = (DomainScoreViewHolder) convertView.getTag();
        }

        holder.tvDomain.setText(lstScoreDomains.get(position).getDomain());
        holder.tvTotalCount.setText(String.valueOf(lstScoreDomains.get(position).getTotalCount()));
        holder.tvCorrectCount.setText(String.valueOf(lstScoreDomains.get(position).getCorrectCount()));
        holder.tvCorrectRate.setText(lstScoreDomains.get(position).getCorrectRateString());
        return convertView;
    }

    public class DomainScoreViewHolder {
        public TextView tvDomain;
        public TextView tvTotalCount;
        public TextView tvCorrectCount;
        public TextView tvCorrectRate;
    }
}