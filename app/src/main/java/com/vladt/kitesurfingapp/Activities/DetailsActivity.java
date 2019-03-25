package com.vladt.kitesurfingapp.Activities;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

import org.json.JSONException;
import org.json.JSONObject;

public class DetailsActivity extends AppCompatActivity {

    //spot object for details
    private KitesurfingSpot ks;

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
        setContentView(R.layout.activity_details);

        //get some spot details from list activity
        ks = (KitesurfingSpot) getIntent().getSerializableExtra("serializedSpot");

        //set body request
        JSONObject urlBody = new JSONObject();
        try {
            urlBody.put("spotId", ks.getID());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //request for updating spot details
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

                    //get more spot details
                    JSONObject _jo = (JSONObject) jo.get("result");
                    ks.setLongitude(_jo.getString("longitude"));
                    ks.setLatitude(_jo.getString("latitude"));
                    ks.setWindProbability(_jo.getString("windProbability"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @SuppressLint("SetTextI18n")
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

                Button mapsLink = findViewById(R.id.mapsLink);

                countryInfoDetails.setText(ks.getCountry());

                latitudeInfoDetails.setText(ks.getLatitude());

                longitudeInfoDetails.setText(ks.getLongitude());

                windInfoDetails.setText(ks.getWindProbability());

                whenToGoInfoDetails.setText(ks.getWhenToGo().substring(0, 1).toUpperCase()
                        + ks.getWhenToGo().substring(1).toLowerCase());

                //set appbar
                Toolbar toolbar = findViewById(R.id.app_bar_details);
                setSupportActionBar(toolbar);
                assert getSupportActionBar() != null;
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setTitle(ks.getName());

                //button for seeing spot on Google Maps
                //or another application if GMaps isn't available
                mapsLink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String uri = "https://www.google.com/maps/place/" + ks.getLatitude() +
                                "," + ks.getLongitude();
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        intent.setPackage("com.google.android.apps.maps");
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException ex) {
                            try {
                                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                startActivity(unrestrictedIntent);
                            } catch (ActivityNotFoundException innerEx) {
                                Toast.makeText(DetailsActivity.this, "Please install a maps application", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }

        });

        //execute request for getting spot details
        //if there is internet connection
        if (InternetConnection.check()) {
            prj.execute(new String[]{APIEndpoints.getSpotDetails, urlBody.toString()},
                    APIHeaders.get());
        }
        //else notify user through toast
        else {
            Toast.makeText(DetailsActivity.this, "Can't get details while offline", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        //set favorite button for spot accordingly
        //and tint it
        if (ks.getIsFavorite()) {
            menu.findItem(R.id.action_star).setIcon(TintedDrawable.get(getApplicationContext(),R.drawable.white_star_on_button,R.color.colorWhite));
        } else {
            menu.findItem(R.id.action_star).setIcon(TintedDrawable.get(getApplicationContext(),R.drawable.white_star_off_button,R.color.colorWhite));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                updatePreviousList();
                return true;
            case R.id.action_star:
                setFavoriteSpot(item);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        updatePreviousList();
    }

    private void setFavoriteSpot(final MenuItem item) {
        JSONObject spotID = new JSONObject();
        try {
            spotID.put("spotId", ks.getID());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //request for favorite spot inside details
        PostRequestJSON favoriteSpot = new PostRequestJSON(new PostRequestJSON.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                ks.setIsFavorite(!ks.getIsFavorite());
                if (ks.getIsFavorite()) {
                    item.setIcon(R.drawable.white_star_on_button);
                } else {
                    item.setIcon(R.drawable.white_star_off_button);
                }
            }
        });
        //if there is an internet connection
        //update favorite state for spot accordingly
        if (InternetConnection.check()) {
            if (!ks.getIsFavorite()) {
                favoriteSpot.execute(new String[]{APIEndpoints.addFavoriteSpot, spotID.toString()},
                        APIHeaders.get());
            } else {
                favoriteSpot.execute(new String[]{APIEndpoints.removeFavoriteSpot, spotID.toString()},
                        APIHeaders.get());
            }
        }
        //else notify user through toast
        else {
            Toast.makeText(DetailsActivity.this, "Can't favorite while offline", Toast.LENGTH_LONG).show();
        }
    }

    private void updatePreviousList() {
        //get back updated details about spot
        //into the list activity
        Intent intent = getIntent();
        intent.putExtra("serializedSpot", ks);
        setResult(ResponseCodes.Codes.LIST_TO_DETAILS_OK.ordinal(), intent);
        finish();
    }
}
