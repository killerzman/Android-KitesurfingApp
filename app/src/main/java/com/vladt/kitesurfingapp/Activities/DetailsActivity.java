package com.vladt.kitesurfingapp.Activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
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

import org.json.JSONException;
import org.json.JSONObject;

public class DetailsActivity extends AppCompatActivity {

    JSONObject urlBody;
    KitesurfingSpot ks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ks = (KitesurfingSpot) getIntent().getSerializableExtra("serializedSpot");

        urlBody = new JSONObject();
        try {
            urlBody.put("spotId", ks.getID());
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
                    JSONObject _jo = (JSONObject) jo.get("result");
                    ks.setLongitude(_jo.getString("longitude"));
                    ks.setLatitude(_jo.getString("latitude"));
                    ks.setWindProbability(_jo.getString("windProbability"));
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

                Button mapsLink = findViewById(R.id.mapsLink);

                countryInfoDetails.setText(ks.getCountry());

                latitudeInfoDetails.setText(ks.getLatitude());

                longitudeInfoDetails.setText(ks.getLongitude());

                windInfoDetails.setText(ks.getWindProbability());

                whenToGoInfoDetails.setText(ks.getWhenToGo().substring(0, 1).toUpperCase()
                        + ks.getWhenToGo().substring(1).toLowerCase());

                Toolbar toolbar = findViewById(R.id.app_bar_details);
                setSupportActionBar(toolbar);
                assert getSupportActionBar() != null;
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setTitle(ks.getName());

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

        if (InternetConnection.check()) {
            prj.execute(new String[]{APIEndpoints.getSpotDetails, urlBody.toString()},
                    APIHeaders.get());
        } else {
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
        if (ks.getIsFavorite()) {
            menu.findItem(R.id.action_star).setIcon(R.drawable.white_star_on_button);
        } else {
            menu.findItem(R.id.action_star).setIcon(R.drawable.white_star_off_button);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                updatePreviousActivity();
                return true;
            case R.id.action_star:
                setFavoriteSpot(item);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        updatePreviousActivity();
    }

    private void setFavoriteSpot(final MenuItem item) {
        JSONObject spotID = new JSONObject();
        try {
            spotID.put("spotId", ks.getID());
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        if (InternetConnection.check()) {
            if (!ks.getIsFavorite()) {
                favoriteSpot.execute(new String[]{APIEndpoints.addFavoriteSpot, spotID.toString()},
                        APIHeaders.get());
            } else {
                favoriteSpot.execute(new String[]{APIEndpoints.removeFavoriteSpot, spotID.toString()},
                        APIHeaders.get());
            }
        } else {
            Toast.makeText(DetailsActivity.this, "Can't favorite while offline", Toast.LENGTH_LONG).show();
        }
    }

    private void updatePreviousActivity() {
        Intent intent = getIntent();
        intent.putExtra("serializedSpot", ks);
        setResult(ResponseCodes.Codes.RESULT_OK.ordinal(), intent);
        finish();
    }
}
