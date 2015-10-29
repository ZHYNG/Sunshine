package com.example.fox.sunshine.app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdapter;
    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        String[] forecastArray={
                "Today - Sunny - 88/63",
                "Tomorrow - Foggy - 70/40",
                "Weds - Cloudy - 72/63",
                "Fri - Heavy Rain - 65/56",
                "Sat - HELP TRAPPED IN WEATHERSTATION - 68/51",
                "Sun - Sunny - 80/68"
        };

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));
        mForecastAdapter = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,weekForecast);


        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);



        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;
            try {
                URL url = null;
                String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?" +
                        "q=94043&mode=json&units=metric&cnt=7";
                String apiKey = "&APPID=" + BuildConfig.OPEN_WEATHER_MAP_API_KEY;
                url = new URL(baseUrl.concat(apiKey));
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(inputStream==null)
                {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if(buffer.length()==0)
                {
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (ProtocolException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }finally {
                if(urlConnection!=null)
                {
                    urlConnection.disconnect();
                }
                if(reader!=null)
                {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("Fox", "Error closing stream", e);
                    }
                }
            }

            return null;
        }
    }
}
