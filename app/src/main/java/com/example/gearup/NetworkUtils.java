package com.example.gearup;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtils {

    public static String fetchData(String urlString) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (reader != null) reader.close();
                if (urlConnection != null) urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}