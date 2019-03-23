package com.vladt.kitesurfingapp.Activities;

import android.content.Intent;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FiltersActivity extends AppCompatActivity {

    ArrayList<String> countries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);

        countries = new ArrayList<>();
        countries.add("<All countries>");

        Toolbar toolbar = findViewById(R.id.app_bar_filters);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.filter);


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
                    for (int i = 0; i < ja.length(); i++) {
                        countries.add(ja.get(i).toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            private void setCustomAdapter() {
                final String[] selectedCountry = {null};
                final int[] selectedWindProb = {0};

                Spinner spinner = findViewById(R.id.countrySpinner);
                final TextView windProbText = findViewById(R.id.windProbabilityTextView);
                SeekBar seekbar = findViewById(R.id.seekbarWind);
                Button button = findViewById(R.id.applyFilter);

                ArrayAdapter<String> countriesAdapter =
                        new ArrayAdapter<String>(FiltersActivity.this, android.R.layout.simple_spinner_item, countries);

                countriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(countriesAdapter);
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
                seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

        if (InternetConnection.check()) {
            prj.execute(new String[]{APIEndpoints.getSpotCountries, ""},
                    APIHeaders.get());
        } else {
            Toast.makeText(FiltersActivity.this, "Can't get countries while offline", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}