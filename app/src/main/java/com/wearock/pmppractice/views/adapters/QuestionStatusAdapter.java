package com.wearock.pmppractice.views.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.wearock.pmppractice.R;
import com.wearock.pmppractice.models.QuestionStatus;
import com.wearock.pmppractice.views.customize.CircleView;

import java.util.ArrayList;

public class QuestionStatusAdapter extends BaseAdapter {

    private ArrayList<QuestionStatus> list;
    private StatusType showType;
    private Context context;
    private LayoutInflater inflater = null;

    public QuestionStatusAdapter(ArrayList<QuestionStatus> list, StatusType type, Context context) {
        this.context = context;
        this.showType = type;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            QuestionStatusAdapter.StatusViewHolder holder;
            if (convertView == null) {
                holder = new QuestionStatusAdapter.StatusViewHolder();
                convertView = inflater.inflate(R.layout.layout_question_status_item, null);
                holder.cv = convertView.findViewById(R.id.cvQuestionStatus);
                convertView.setTag(holder);
            } else {
                holder = (QuestionStatusAdapter.StatusViewHolder) convertView.getTag();
            }

            holder.cv.setText(String.valueOf(list.get(position).getIndex()));
            switch (showType) {
                case ANSWERED:
                    holder.cv.setBgColor(list.get(position).isAnswered() ? Color.GREEN : Color.GRAY);
                    break;
                case CORRECTNESS:
                    holder.cv.setBgColor(list.get(position).isCorrect() ? Color.GREEN : Color.RED);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    public enum StatusType {
        ANSWERED,
        CORRECTNESS
    }

    public static class StatusViewHolder {
        public CircleView cv;
    }

}
