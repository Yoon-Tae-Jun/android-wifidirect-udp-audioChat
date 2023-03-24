package com.example.myapplication1;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

class MyAdapter extends BaseAdapter {
    private ArrayList<MyItem> items = new ArrayList<>();
    private Context c;

    public  MyAdapter(Context c){
        this.c = c;
    }
    public void addItem(MyItem item){
        items.add(item);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public MyItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        MyItemView view = new MyItemView(c);

        MyItem item = items.get(position);
        view.setDeviceName(item.getDeviceName());

        return view;
    }
}
