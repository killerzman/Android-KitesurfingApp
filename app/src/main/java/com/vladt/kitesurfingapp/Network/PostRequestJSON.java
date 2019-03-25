package com.vladt.kitesurfingapp.Network;

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

public class PostRequestJSON extends AsyncTask<String[], Integer, String> {

    private AsyncResponse delegate;

    public PostRequestJSON(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(String[]... p) {
        try {
            //urlInfo contains the url and url body for the request
            String[] urlInfo = p[0];

            //headersInfo contains the key and value items for headers
            String[] headersInfo = p[1];

            //urlInfo[0] is the url
            URL obj = new URL(urlInfo[0]);
            HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
            postConnection.setRequestMethod("POST");
            for (int i = 0; i < headersInfo.length; i += 2) {

                //headersInfo of even value is key
                //headersInfo of odd value is value of previous key
                postConnection.setRequestProperty(headersInfo[i], headersInfo[i + 1]);
            }
            postConnection.setDoInput(true);
            postConnection.setDoOutput(true);
            OutputStream os = postConnection.getOutputStream();

            //if necessary add in url body request
            if (!urlInfo[1].equals("")) {
                os.write(urlInfo[1].getBytes("UTF-8"));
                os.close();
            }
            postConnection.connect();
            int responseCode = postConnection.getResponseCode();

            //if the connection is ok
            if (responseCode == HttpURLConnection.HTTP_OK) {

                //get server response as string
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
                return "NO_POST";
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(PostRequestJSON.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "INVALID_URL";
    }

    @Override
    protected void onProgressUpdate(Integer... values) {

    }

    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }

    public interface AsyncResponse {
        void processFinish(String output);
    }
}
