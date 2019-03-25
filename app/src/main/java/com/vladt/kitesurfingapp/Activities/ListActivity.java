package com.vladt.kitesurfingapp.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.vladt.kitesurfingapp.Models.KitesurfingSpot;
import com.vladt.kitesurfingapp.Network.APIEndpoints;
import com.vladt.kitesurfingapp.Network.APIHeaders;
import com.vladt.kitesurfingapp.Network.InternetConnection;
import com.vladt.kitesurfingapp.Network.PostRequestJSON;
import com.vladt.kitesurfingapp.R;
import com.vladt.kitesurfingapp.Response.ResponseCodes;
import com.vladt.kitesurfingapp.Utils.TintedDrawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

//MAIN ACTIVITY
public class ListActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private CustomAdapter ca;
    private ListView listView;

    //array of spots
    private ArrayList<KitesurfingSpot> spots;

    //body request for url
    private JSONObject urlBody;

    //used for titling the app bar
    private String activityTitle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //get SharedPreferences for dark mode
        prefs = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        //get value for dark mode
        //if there is no value, set it as -1
        //-1 means uninitialised
        int isDarkMode = prefs.getInt("darkMode", -1);

        //if dark mode is uninitialised
        if (isDarkMode == -1) {
            //set dark mode off
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("darkMode", 0);
            editor.apply();
            setTheme(R.style.AppTheme);
        }

        //if dark mode is disabled
        else if (isDarkMode == 0) {
            setTheme(R.style.AppTheme);
        }

        //if dark mode is enabled
        else if (isDarkMode == 1) {
            setTheme(R.style.AppTheme_Dark);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        //create custom adapter for list view
        ca = new CustomAdapter();

        //if there is an internet connection
        //start the program logic
        if (InternetConnection.check()) {

            //for the filtered countries screen
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                urlBody = new JSONObject();
                try {

                    //get country from bundle extras
                    //country is used for body request
                    String country = extras.getString("Country");
                    if (country == null) {
                        country = "";
                    }

                    //set activity title for filtering screen
                    else if (country.equals("<All countries>")) {
                        country = "";
                        activityTitle = "All countries";
                    } else {
                        activityTitle = country;
                    }
                    urlBody.put("country", country);

                    //get wind probability from bundle extras
                    //wind probability is used for body request
                    int windProbNumber = extras.getInt("Wind Probability");
                    urlBody.put("windProbability", windProbNumber);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                urlBody = null;
            }

            //set body request to empty if null for proper request
            //else toString() will suffice
            String urlBodyString;
            if (urlBody != null) {
                urlBodyString = urlBody.toString();
            } else {
                urlBodyString = "";
            }

            //request for updating array of spots
            PostRequestJSON prj = new PostRequestJSON(new PostRequestJSON.AsyncResponse() {

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

                        //if there are no results for the filter screen
                        //notify user through toast
                        //and go back to the previous activity
                        if (ja == null || ja.length() == 0) {
                            Toast.makeText(ListActivity.this,
                                    "No results found for filter",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            for (int i = 0; i < ja.length(); i++) {

                                //populate class with json results
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

                    //set listview adapter
                    listView = findViewById(R.id.list);
                    listView.setAdapter(ca);
                }

            });

            //execute the request for getting spots
            prj.execute(new String[]{APIEndpoints.getAllSpots, urlBodyString},
                    APIHeaders.get());
        } else {

            //if there is no connection to the server
            //notify user through toast
            Toast.makeText(ListActivity.this, "Unable to connect to server", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //condition used for filter/list screen
        //if the activity title hasn't been overridden
        //then app is on the list screen
        //therefore show app name
        //else show the name of the filtered country
        if (activityTitle.equals("")) {
            getMenuInflater().inflate(R.menu.menu_list, menu);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        //condition used for filter/list screen
        //if the activity title hasn't been overridden
        //then app is on the list screen
        //therefore show the filter button and tint it
        if (activityTitle.equals("")) {
            menu.findItem(R.id.action_filter).setIcon(TintedDrawable.get(getApplicationContext(),R.drawable.white_filter_button,R.color.colorWhite));

            //get dark mode settings
            int isDarkMode = prefs.getInt("darkMode", -1);
            if (isDarkMode == 1) {
                menu.findItem(R.id.action_darkmode).setIcon(R.drawable.darkmode_on);
            } else if (isDarkMode == 0) {
                menu.findItem(R.id.action_darkmode).setIcon(R.drawable.darkmode_off);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter: {

                //startActivityForResult used for when
                //we favorite a spot through the filtered screen
                //and update the info on the list screen
                Intent intent = new Intent(ListActivity.this, FiltersActivity.class);
                startActivityForResult(intent,
                        ResponseCodes.Codes.LIST_TO_FILTER_CODE.ordinal());
                break;
            }
            case R.id.action_darkmode:

                //condition used for filter/list screen
                //if the activity title hasn't been overridden
                //then app is on the list screen
                //therefore update dark mode accordingly
                if (activityTitle.equals("")) {
                    updateDarkMode();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //if we're getting back to list from details activity
        if (requestCode == ResponseCodes.Codes.LIST_TO_DETAILS_CODE.ordinal() &&
                resultCode == ResponseCodes.Codes.LIST_TO_DETAILS_OK.ordinal()) {

            //set new info retrieved for spots
            KitesurfingSpot ks = (KitesurfingSpot) data.getSerializableExtra("serializedSpot");
            for (int i = 0; i < spots.size(); i++) {
                if (ks.getID().equals(spots.get(i).getID())) {
                    spots.set(i, ks);
                    break;
                }
            }
            ca.notifyDataSetChanged();
        }

        //if we're getting back to list from filter activity
        else if (requestCode == ResponseCodes.Codes.LIST_TO_FILTER_CODE.ordinal() &&
                resultCode == ResponseCodes.Codes.LIST_TO_FILTER_OK.ordinal()) {
            //recreate view
            recreate();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void updateDarkMode() {

        int isDarkMode = prefs.getInt("darkMode", -1);
        SharedPreferences.Editor editor = prefs.edit();

        //set dark mode on
        if (isDarkMode == 0) {
            editor.putInt("darkMode", 1);
            editor.apply();
            setTheme(R.style.AppTheme_Dark);
            recreate();
        }

        //set dark mode off
        else if (isDarkMode == 1) {
            editor.putInt("darkMode", 0);
            editor.commit();
            setTheme(R.style.AppTheme);
            recreate();
        }

        ca.notifyDataSetChanged();
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

        @SuppressLint("ViewHolder")
        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            //inflate layout for how many spots there are
            view = getLayoutInflater().inflate(R.layout.activity_list_view, viewGroup, false);

            //set app bar
            Toolbar toolbar = findViewById(R.id.app_bar_list);
            setSupportActionBar(toolbar);

            //condition used for filter/list screen
            //if the activity title has been overridden
            //then app is on the filter screen
            //therefore update title and subtitle
            //according to the filter settings
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

            //set spot and country name for every element
            spotName.setText(spots.get(i).getName());
            countryName.setText(spots.get(i).getCountry());

            //set fav button state for every element
            if (spots.get(i).getIsFavorite()) {
                favButton.setSelected(true);
            } else {
                favButton.setSelected(false);
            }

            //get spot details
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int idx, long id) {
                    Intent intent = new Intent(ListActivity.this, DetailsActivity.class);
                    intent.putExtra("serializedSpot", spots.get(idx));
                    startActivityForResult(intent, ResponseCodes.Codes.LIST_TO_DETAILS_CODE.ordinal());
                }
            });

            //fav button listener
            favButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject spotID = new JSONObject();
                    try {

                        //get id from spot
                        spotID.put("spotId", spots.get(i).getID());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //request for favoriting spots
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

                    //if there is an internet connection
                    if (InternetConnection.check()) {

                        //favorite spot accordingly
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

