package com.vladt.kitesurfingapp;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostRequestJSON extends AsyncTask<String, Integer, String> {

    public AsyncResponse delegate = null;

    public PostRequestJSON(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            URL obj = new URL(params[0]);
            HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
            postConnection.setRequestMethod("POST");
            for (int i = 1; i < params.length - 1; i += 2) {
                postConnection.setRequestProperty(params[i], params[i + 1]);
            }
            postConnection.setDoInput(true);
            postConnection.setDoOutput(true);
            OutputStream os = postConnection.getOutputStream();
            if(!params[params.length - 1].equals("")) {
                os.write(params[params.length - 1].getBytes("UTF-8"));
                os.close();
            }
            postConnection.connect();
            int responseCode = postConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuffer response;
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        postConnection.getInputStream()));
                String inputLine;
                response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                return response.toString();
            } else {
                return "POST NOT WORKED";
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(PostRequestJSON.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "INVALID URL";
    }

    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }

    public interface AsyncResponse {
        void processFinish(String output);
    }
}
