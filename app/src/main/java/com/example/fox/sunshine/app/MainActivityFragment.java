package com.example.fox.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forcastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            FetchWeatherTask weatherTask = new FetchWeatherTask();
            weatherTask.execute("94043");
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private  final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        private String getReadableDateString(long time){
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        private  String formatHighLows(double high,double low){
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);
            String highLowStr = roundedHigh +"/" +roundedLow;
            return highLowStr;
        }

        private String[]getWeatherDataFromJson(String forecastJsonStr,int numDays)
            throws JSONException{
            final String OwM_LIST = "list";
            final String OwM_WEATHER = "weather";
            final String OwM_TEMPERATURE = "temp";
            final String OwM_MAX = "max";
            final String OwM_MIN = "min";
            final String OwM_DESCRIPTION = "main";
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OwM_LIST);
            Time daysTime = new Time();
            daysTime.setToNow();

            int julanStartDay = Time.getJulianDay(System.currentTimeMillis(),daysTime.gmtoff);

            daysTime = new Time();

            String[] resultStrs = new String[numDays];
            for (int i=0;i<weatherArray.length();i++)
            {
                String day;
                String description;
                String highAndLow;
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                long dateTime;

                dateTime = daysTime.setJulianDay(julanStartDay+i);
                day = getReadableDateString(dateTime);
                JSONObject weatherObject = dayForecast.getJSONArray(OwM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OwM_DESCRIPTION);

                JSONObject temperatureObject = dayForecast.getJSONObject(OwM_TEMPERATURE);
                double high = temperatureObject.getDouble(OwM_MAX);
                double low = temperatureObject.getDouble(OwM_MIN);

                highAndLow = formatHighLows(high,low);
                resultStrs[i]=day + "-" + description + "-" + highAndLow;
            }

            for (String s : resultStrs)
            {
                Log.v(LOG_TAG,"forecast entry: "+s );
            }

            return resultStrs;
        }
        @Override
        protected String[] doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;
            String format = "json";
            String units  = "metric";
            int numDays = 7;
            try {

                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";
                Uri bulidUri = null;
                URL url = null;
                bulidUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM,params[0])
                        .appendQueryParameter(FORMAT_PARAM,format)
                        .appendQueryParameter(UNITS_PARAM,units)
                        .appendQueryParameter(DAYS_PARAM,Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM,BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                        .build();

                url = new URL(bulidUri.toString());

                //String apiKey = "&APPID=" + BuildConfig.OPEN_WEATHER_MAP_API_KEY;
                //url = new URL(baseUrl.concat(apiKey));
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
                Log.v(LOG_TAG,"Forcast JSON String: "+forecastJsonStr);
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

            try{
                return getWeatherDataFromJson(forecastJsonStr,numDays);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
           if (result!=null){
               mForecastAdapter.clear();
               for(String dayForecastStr : result ){
                   mForecastAdapter.add(dayForecastStr);
               }
           }
        }
    }
}
