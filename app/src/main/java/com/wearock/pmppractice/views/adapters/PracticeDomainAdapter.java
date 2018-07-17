package com.wearock.pmppractice.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.wearock.pmppractice.R;

import java.util.ArrayList;
import java.util.HashMap;

public class PracticeDomainAdapter extends BaseAdapter {

    private ArrayList<String> list;
    private HashMap<String, Boolean> isSelected;
    private Context context;
    private LayoutInflater inflater = null;

    public PracticeDomainAdapter(ArrayList<String> list, Context context) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
        isSelected = new HashMap<>();

        for (int i=0;i<list.size();i++) {
            getIsSelected().put(list.get(i), true);
        }
    }

    public HashMap<String, Boolean> getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(HashMap<String, Boolean> isSelected) {
        this.isSelected = isSelected;
        notifyDataSetChanged();
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
        DomainSelectionViewHolder holder;
        if (convertView == null) {
            holder = new DomainSelectionViewHolder();
            convertView = inflater.inflate(R.layout.layout_domain_selection_item, null);
            holder.tv = convertView.findViewById(R.id.item_text);
            holder.cb = convertView.findViewById(R.id.item_check);
            convertView.setTag(holder);
        } else {
            holder = (DomainSelectionViewHolder) convertView.getTag();
        }

        holder.tv.setText(list.get(position));
        holder.cb.setChecked(getIsSelected().get(list.get(position)));
        return convertView;
    }

    public static class DomainSelectionViewHolder {
        public TextView tv;
        public CheckBox cb;
    }

}
