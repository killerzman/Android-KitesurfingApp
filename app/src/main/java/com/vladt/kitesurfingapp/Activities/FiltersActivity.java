package com.vladt.kitesurfingapp.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

public class FiltersActivity extends AppCompatActivity {

    //arraylist of available countries for spots
    private ArrayList<String> countries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //get SharedPreferences for dark mode
        SharedPreferences prefs = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

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
        setContentView(R.layout.activity_filters);

        //set up arraylist for enumerating countries
        countries = new ArrayList<>();

        //add an all countries option
        countries.add("<All countries>");

        //set app bar
        Toolbar toolbar = findViewById(R.id.app_bar_filters);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.filter);

        //request for filtering spots
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
                    //get list of countries
                    //and add them to the arraylist
                    for (int i = 0; i < ja.length(); i++) {
                        countries.add(ja.get(i).toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            private void setCustomAdapter() {
                //selected country from spinner
                final String[] selectedCountry = {null};

                //selected wind probability from seekbar
                final int[] selectedWindProb = {0};

                Spinner spinner = findViewById(R.id.countrySpinner);
                final TextView windProbText = findViewById(R.id.windProbabilityTextView);
                SeekBar seekbar = findViewById(R.id.seekbarWind);
                Button button = findViewById(R.id.applyFilter);

                ArrayAdapter<String> countriesAdapter =
                        new ArrayAdapter<>(FiltersActivity.this, android.R.layout.simple_spinner_item, countries);

                countriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                //arraylist adapter for activity view
                spinner.setAdapter(countriesAdapter);

                //select element from spinner and remember it
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedCountry[0] = parent.getItemAtPosition(position).toString();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                seekbar.setMax(100);
                seekbar.setProgress(1);

                //get updated seekbar value and remember it
                seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        selectedWindProb[0] = progress;
                        windProbText.setText("Wind Probability : " + String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                //button for applying filters
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(FiltersActivity.this, ListActivity.class);
                        intent.putExtra("Country", selectedCountry[0]);
                        intent.putExtra("Wind Probability", selectedWindProb[0]);
                        startActivity(intent);
                    }
                });

            }
        });

        //execute request with internet connection
        //for getting spot countries
        if (InternetConnection.check()) {
            prj.execute(new String[]{APIEndpoints.getSpotCountries, ""},
                    APIHeaders.get());
        }
        //else notify user through toast
        else {
            Toast.makeText(FiltersActivity.this, "Can't get countries while offline", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                updatePreviousList();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        updatePreviousList();
    }

    void updatePreviousList() {
        //update spots details
        Intent intent = getIntent();
        setResult(ResponseCodes.Codes.LIST_TO_FILTER_OK.ordinal(), intent);
        finish();
    }
}