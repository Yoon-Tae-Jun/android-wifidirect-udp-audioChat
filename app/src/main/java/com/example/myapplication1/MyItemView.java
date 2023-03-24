package com.example.myapplication1;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class MyItemView extends LinearLayout {
    TextView textView, textView2;

    public MyItemView(Context context) {
        super(context);
        init(context);
    }

    public MyItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.my_item, this, true);

        textView = findViewById(R.id.tv_id);
        textView2 = findViewById(R.id.tv_phone);
    }

    public void setDeviceName(String device_name){
        textView.setText(device_name);
    }
}
