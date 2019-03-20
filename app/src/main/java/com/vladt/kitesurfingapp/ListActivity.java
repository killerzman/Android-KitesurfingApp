package com.vladt.kitesurfingapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    ArrayList<ArrayList<String>> spotsAndCountries;
    ArrayList<String> idSpots;
    ListView listView;
    String url;
    JSONObject urlBody;
    String urlBodyString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);

        spotsAndCountries = new ArrayList<>();
        idSpots = new ArrayList<>();
        url = "https://internship-2019.herokuapp.com/api-spot-get-all";

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            urlBody = new JSONObject();
            try {
                urlBody.put("country", extras.getString("Country"));
                String windProbNumber = extras.getString("Wind Probability");
                assert windProbNumber != null;
                if (windProbNumber.equals("0")) {
                    windProbNumber = "";
                }
                urlBody.put("windProbability", windProbNumber);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            urlBody = null;
        }

        if (urlBody != null) {
            urlBodyString = urlBody.toString();
        } else {
            urlBodyString = "";
        }

        PostRequestJSON prj = new PostRequestJSON(new PostRequestJSON.AsyncResponse() {
            final String[] result = new String[1];

            @Override
            public void processFinish(String output) {
                result[0] = output;
                parseJSONData(result[0]);
                setCustomAdapter();
            }

            private void parseJSONData(String res) {
                JSONObject jo;
                try {
                    jo = new JSONObject(res);
                    JSONArray ja = (JSONArray) jo.get("result");
                    for (int i = 0; i < ja.length(); i++) {
                        ArrayList<String> _arrL = new ArrayList<>();
                        JSONObject _jo = ja.getJSONObject(i);
                        _arrL.add(_jo.get("name").toString());
                        _arrL.add(_jo.get("country").toString());
                        idSpots.add(_jo.get("id").toString());
                        spotsAndCountries.add(_arrL);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            private void setCustomAdapter() {
                listView = findViewById(R.id.list);

                listView.setAdapter(new CustomAdapter());
            }

        });

        prj.execute(url, "Content-Type", "application/json", "token", "OxrBHp1ReG", urlBodyString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                startActivity(new Intent(ListActivity.this, FiltersActivity.class));
        }
        return super.onOptionsItemSelected(item);
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

            Toolbar toolbar = findViewById(R.id.app_bar_list);
            setSupportActionBar(toolbar);

            TextView spotName = view.findViewById(R.id.spotname);
            final ImageButton favButton = view.findViewById(R.id.favbutton);
            TextView countryName = view.findViewById(R.id.countryname);

            spotName.setText(spotsAndCountries.get(i).get(0));
            countryName.setText(spotsAndCountries.get(i).get(1));

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int idx, long id) {
                    Intent intent = new Intent(ListActivity.this, DetailsActivity.class);
                    intent.putExtra("id", idSpots.get(idx));
                    startActivity(intent);
                }
            });

            favButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*PostRequestJSON addFav = new PostRequestJSON(new PostRequestJSON.AsyncResponse() {
                        @Override
                        public void processFinish(String output) {

                        }
                    });
                    addFav.execute(url, "Content-Type", "application/json", "token", "OxrBHp1ReG", urlBodyString);*/
                    favButton.setSelected(!favButton.isSelected());
                }
            });

            return view;
        }
    }
}

