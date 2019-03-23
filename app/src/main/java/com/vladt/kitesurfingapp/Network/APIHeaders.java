package com.vladt.kitesurfingapp.Network;

import android.annotation.TargetApi;
import android.os.Build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class APIHeaders {
    private static Boolean addedInitialHeaders = false;
    private static HashMap<String, String> headers = new HashMap<>();

    private static void addInitialHeaders() {
        if (!addedInitialHeaders) {
            headers.put("Content-Type", "application/json");
            headers.put("token", "OxrBHp1ReG");
            addedInitialHeaders = true;
        }
    }

    public static String[] get() {
        addInitialHeaders();
        ArrayList<String> _ar = new ArrayList<>();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            _ar.add(entry.getKey());
            _ar.add(entry.getValue());
        }
        String[] returnedHeaders = new String[_ar.size()];
        for (int i = 0; i < _ar.size(); i++) {
            returnedHeaders[i] = _ar.get(i);
        }
        return returnedHeaders;
    }

    public static void add(String key, String value) {
        headers.put(key, value);
    }

    public static void remove(String key) {
        headers.remove(key);
    }

    public static void noHeaders() {
        headers = null;
    }

    public static void resetHeaders() {
        noHeaders();
        addedInitialHeaders = false;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static void remove(String key, String value) {
        headers.remove(key, value);
    }
}
