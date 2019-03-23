package com.vladt.kitesurfingapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vladt.kitesurfingapp.Models.KitesurfingSpot;
import com.vladt.kitesurfingapp.Network.APIEndpoints;
import com.vladt.kitesurfingapp.Network.APIHeaders;
import com.vladt.kitesurfingapp.Network.InternetConnection;
import com.vladt.kitesurfingapp.Network.PostRequestJSON;
import com.vladt.kitesurfingapp.R;
import com.vladt.kitesurfingapp.Response.ResponseCodes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    ArrayList<KitesurfingSpot> spots;
    ListView listView;
    JSONObject urlBody;
    String urlBodyString;
    PostRequestJSON prj;
    String activityTitle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Log.i("conn", InternetConnection.check().toString());

        if (InternetConnection.check()) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                urlBody = new JSONObject();
                try {
                    String country = extras.getString("Country");
                    if (country == null) {
                        country = "";
                    } else if (country.equals("<All countries>")) {
                        country = "";
                        activityTitle = "All countries";
                    } else {
                        activityTitle = country;
                    }
                    urlBody.put("country", country);

                    int windProbNumber = extras.getInt("Wind Probability");
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

            prj = new PostRequestJSON(new PostRequestJSON.AsyncResponse() {

                @Override
                public void processFinish(String output) {
                    parseJSONData(output);
                    setCustomAdapter();
                }

                private void parseJSONData(String res) {
                    JSONObject jo;
                    try {
                        jo = new JSONObject(res);
                        JSONArray ja = (JSONArray) jo.get("result");
                        spots = new ArrayList<>();
                        if (ja == null || ja.length() == 0) {
                            Toast.makeText(ListActivity.this,
                                    "No results found for filter",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            for (int i = 0; i < ja.length(); i++) {
                                JSONObject _jo = ja.getJSONObject(i);
                                KitesurfingSpot ks = new KitesurfingSpot();
                                ks.setID(_jo.get("id").toString());
                                ks.setName(_jo.get("name").toString());
                                ks.setCountry(_jo.get("country").toString());
                                ks.setWhenToGo(_jo.get("whenToGo").toString());
                                ks.setIsFavorite(_jo.get("isFavorite").toString().equals("true"));
                                spots.add(ks);
                            }
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

            prj.execute(new String[]{APIEndpoints.getAllSpots, urlBodyString},
                    APIHeaders.get());
        } else {
            Toast.makeText(ListActivity.this, "Unable to connect to server", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ResponseCodes.Codes.REQUEST_CODE.ordinal() &&
                resultCode == ResponseCodes.Codes.RESULT_OK.ordinal()) {

            KitesurfingSpot ks = (KitesurfingSpot) data.getSerializableExtra("serializedSpot");
            for (int i = 0; i < spots.size(); i++) {
                if (ks.getID().equals(spots.get(i).getID())) {
                    spots.set(i, ks);
                    break;
                }
            }
            listView.setAdapter(new CustomAdapter());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return spots.size();
        }

        @Override
        public Object getItem(int position) {
            return spots.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.activity_list_view, null);

            final SwipeRefreshLayout srl = findViewById(R.id.pullToRefresh);

            Toolbar toolbar = findViewById(R.id.app_bar_list);
            setSupportActionBar(toolbar);
            if (!activityTitle.equals("")) {
                getSupportActionBar().setTitle(activityTitle);
                try {
                    int windProb = urlBody.getInt("windProbability");
                    String subtitle = "Wind Probability: " + String.valueOf(windProb) + "%";
                    if (windProb != 100) {
                        subtitle += " - 100%";
                    }
                    getSupportActionBar().setSubtitle(subtitle);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            TextView spotName = view.findViewById(R.id.spotname);
            final ImageButton favButton = view.findViewById(R.id.favbutton);
            TextView countryName = view.findViewById(R.id.countryname);

            spotName.setText(spots.get(i).getName());
            countryName.setText(spots.get(i).getCountry());

            if (spots.get(i).getIsFavorite()) {
                favButton.setSelected(true);
            } else {
                favButton.setSelected(false);
            }

            srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Log.i("srl", "REFRESHING");
                    PostRequestJSON prj2 = new PostRequestJSON(new PostRequestJSON.AsyncResponse() {

                        @Override
                        public void processFinish(String output) {
                            parseJSONData(output);
                            setCustomAdapter();
                        }

                        private void parseJSONData(String res) {
                            JSONObject jo;
                            try {
                                jo = new JSONObject(res);
                                JSONArray ja = (JSONArray) jo.get("result");
                                spots = new ArrayList<>();
                                if (ja == null || ja.length() == 0) {
                                    Toast.makeText(ListActivity.this,
                                            "No results found for filter",
                                            Toast.LENGTH_LONG).show();
                                    finish();
                                } else {
                                    for (int i = 0; i < ja.length(); i++) {
                                        JSONObject _jo = ja.getJSONObject(i);
                                        KitesurfingSpot ks = new KitesurfingSpot();
                                        ks.setID(_jo.get("id").toString());
                                        ks.setName(_jo.get("name").toString());
                                        ks.setCountry(_jo.get("country").toString());
                                        ks.setWhenToGo(_jo.get("whenToGo").toString());
                                        ks.setIsFavorite(_jo.get("isFavorite").toString().equals("true"));
                                        spots.add(ks);
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        private void setCustomAdapter() {
                            listView = findViewById(R.id.list);

                            listView.setAdapter(new CustomAdapter());

                            srl.setRefreshing(false);
                        }

                    });
                    prj2.execute(new String[]{APIEndpoints.getAllSpots, urlBodyString},
                            APIHeaders.get());
                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int idx, long id) {
                    Intent intent = new Intent(ListActivity.this, DetailsActivity.class);
                    intent.putExtra("serializedSpot", spots.get(idx));
                    startActivityForResult(intent, ResponseCodes.Codes.REQUEST_CODE.ordinal());
                }
            });

            favButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject spotID = new JSONObject();
                    try {
                        spotID.put("spotId", spots.get(i).getID());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    PostRequestJSON favoriteSpot = new PostRequestJSON(new PostRequestJSON.AsyncResponse() {
                        @Override
                        public void processFinish(String output) {
                            spots.get(i).setIsFavorite(!spots.get(i).getIsFavorite());
                            if (spots.get(i).getIsFavorite()) {
                                favButton.setSelected(true);
                            } else {
                                favButton.setSelected(false);
                            }
                        }
                    });
                    if (InternetConnection.check()) {
                        if (!spots.get(i).getIsFavorite()) {
                            favoriteSpot.execute(new String[]{APIEndpoints.addFavoriteSpot, spotID.toString()},
                                    APIHeaders.get());
                        } else {
                            favoriteSpot.execute(new String[]{APIEndpoints.removeFavoriteSpot, spotID.toString()},
                                    APIHeaders.get());
                        }
                    } else {
                        Toast.makeText(ListActivity.this, "Can't favorite while offline", Toast.LENGTH_LONG).show();
                    }
                    //Toast.makeText(ListActivity.this,"Pressed " + i, Toast.LENGTH_LONG).show();
                }
            });

            return view;
        }
    }
}

