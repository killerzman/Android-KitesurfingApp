package com.vladt.kitesurfingapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JSONPostRequest {

    String mUrl;
    String[] mKeys;
    String[] mValues;

    public JSONPostRequest(String url, String[] keys, String[] values) {
        mUrl = url;
        mKeys = keys;
        mValues = values;
    }

    public String doRequest() throws IOException {

        try {
            URL obj = new URL(mUrl);
            HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
            postConnection.setRequestMethod("POST");
            for (int i = 0; i < mKeys.length; i++) {
                postConnection.setRequestProperty(mKeys[i], mValues[i]);
            }
            postConnection.setDoOutput(true);
            OutputStream os = postConnection.getOutputStream();
            os.flush();
            int responseCode = postConnection.getResponseCode();
            System.out.println("POST Response Code :  " + responseCode);
            System.out.println("POST Response Message : " + postConnection.getResponseMessage());
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuffer response;
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        postConnection.getInputStream()));
                String inputLine;
                response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                System.out.println(response.toString());
                return response.toString();
            } else {
                System.out.println("POST NOT WORKED");
                return "POST NOT WORKED";
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(JSONPostRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "INVALID URL";
    }
}
