package com.vladt.kitesurfingapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class DetailsActivity extends AppCompatActivity {

    JSONObject urlBody;
    String url;
    String spotName;
    String longitude;
    String latitude;
    String windProbability;
    String country;
    String whenToGo;
    Boolean isFav = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);

        url = "https://internship-2019.herokuapp.com/api-spot-get-details";

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            urlBody = new JSONObject();
            try {
                urlBody.put("spotId", extras.getString("id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
                    JSONObject _jo = (JSONObject) jo.get("result");
                    country = _jo.getString("country");
                    latitude = _jo.getString("latitude");
                    longitude = _jo.getString("longitude");
                    windProbability = _jo.getString("windProbability");
                    whenToGo = _jo.getString("whenToGo");
                    spotName = _jo.getString("name");


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            private void setCustomAdapter() {
                //TextView countryDetails = findViewById(R.id.countryDetails);
                TextView countryInfoDetails = findViewById(R.id.countryInfoDetails);
                //TextView latitudeDetails = findViewById(R.id.latitudeDetails);
                TextView latitudeInfoDetails = findViewById(R.id.latitudeInfoDetails);
                //TextView longitudeDetails = findViewById(R.id.longitudeDetails);
                TextView longitudeInfoDetails = findViewById(R.id.longitudeInfoDetails);
                //TextView windDetails = findViewById(R.id.windDetails);
                TextView windInfoDetails = findViewById(R.id.windInfoDetails);
                //TextView whenToGoDetails = findViewById(R.id.whenToGoDetails);
                TextView whenToGoInfoDetails = findViewById(R.id.whenToGoInfoDetails);
                countryInfoDetails.setText(country);
                latitudeInfoDetails.setText(latitude);
                longitudeInfoDetails.setText(longitude);
                windInfoDetails.setText(windProbability);
                whenToGo = whenToGo.substring(0,1).toUpperCase() + whenToGo.substring(1).toLowerCase();
                whenToGoInfoDetails.setText(whenToGo);

                Toolbar toolbar = findViewById(R.id.app_bar_details);
                setSupportActionBar(toolbar);
                assert getSupportActionBar() != null;
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setTitle(spotName);
            }

        });

        prj.execute(url, "Content-Type", "application/json", "token", "OxrBHp1ReG", urlBody.toString());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.action_star:
                favSpot(item);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void favSpot(MenuItem item){
        if(!isFav){
            item.setIcon(R.drawable.white_star_on_button);
            isFav = true;
        }
        else{
            item.setIcon(R.drawable.white_star_off_button);
            isFav = false;
        }
    }
}
