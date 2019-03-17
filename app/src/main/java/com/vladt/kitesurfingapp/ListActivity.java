package com.vladt.kitesurfingapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    ArrayList<ArrayList<String>> spotsAndCountries;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);

        spotsAndCountries = new ArrayList<>();

        final JSONPostRequest jpr = new JSONPostRequest("https://internship-2019.herokuapp.com/api-spot-get-all"
                , new String[]{"Content-Type", "token"}, new String[]{"application/json", "OxrBHp1ReG"});

        final String[] s = {null};
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    s[0] = jpr.doRequest();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JSONObject jo;
        try {
            jo = new JSONObject(s[0]);
            JSONArray ja = (JSONArray) jo.get("result");
            for (int i = 0; i < ja.length(); i++) {
                ArrayList<String> _arrL = new ArrayList<>();
                JSONObject _jo = ja.getJSONObject(i);
                _arrL.add(_jo.get("name").toString());
                _arrL.add(_jo.get("country").toString());
                spotsAndCountries.add(_arrL);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        listView = findViewById(R.id.list);

        listView.setAdapter(new CustomAdapter());
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return spotsAndCountries.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.list_activity_view, null);

            TextView spotName = view.findViewById(R.id.spotname);
            final ImageButton favButton = view.findViewById(R.id.favbutton);
            TextView countryName = view.findViewById(R.id.countryname);

            spotName.setText(spotsAndCountries.get(i).get(0));
            countryName.setText(spotsAndCountries.get(i).get(1));

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int idx, long id) {
                    Intent intent = new Intent(ListActivity.this, DetailsActivity.class);
                    startActivity(intent);
                }
            });

            favButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //favButton.setPressed(!favButton.isPressed());
                    Intent intent = new Intent(ListActivity.this, FiltersActivity.class);
                    startActivity(intent);
                }
            });

            return view;
        }
    }
}

