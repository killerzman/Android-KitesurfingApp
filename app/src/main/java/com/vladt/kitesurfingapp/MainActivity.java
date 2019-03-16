package com.vladt.kitesurfingapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ArrayList<HashMap<String,String>> arrList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[][] TextAndInfo ={
                {"Country 1", "Info 1"},
                {"Country 2", "Info 2"},
                {"Country 3", "Info 3"},
                {"Country 4", "Info 4"},
        };

        HashMap<String,String> item;
        for (int i=0; i< TextAndInfo.length; i++) {
            item = new HashMap<>();
            item.put("line1", TextAndInfo[i][0]);
            item.put("line2", TextAndInfo[i][1]);
            arrList.add(item);
        }

        SimpleAdapter sa = new SimpleAdapter(this, arrList, R.layout.twolinesonebutton,
                new String[]{"line1", "line2"},
                new int[]{R.id.line_a, R.id.line_b});

        ((ListView)findViewById(R.id.list)).setAdapter(sa);


    }
}
