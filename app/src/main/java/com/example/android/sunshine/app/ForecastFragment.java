package com.example.android.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import android.text.format.Time;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {


    String zipC = "15213";
    ArrayAdapter forecastAdapter;




    public ForecastFragment() {
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id;
        id = item.getItemId();
        if(id == R.id.action_refresh){
            FetchWeatherTask weatherTask = new FetchWeatherTask();
            weatherTask.execute(zipC);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forcastfragment, menu);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);



        String[] weatherData = {
                "Today - Sunny - 88/74",
                "Tomorrow - Partly Cloudy - 80/70",
                "Weds - cloudy - 75/68",
                "Thurs - rainy - 60/52",
                "Fri - Flaming - 700/451",
                "Sat - Death - 0/0",
                "sun - Partly Cloudy - 78/69",
        };
        ArrayList fakeData = new ArrayList();

        for(String s : weatherData){
            fakeData.add(s);
        }


        forecastAdapter = new ArrayAdapter(getActivity(),R.layout.list_item_forcast, R.id.list_item_forcast_textview, fakeData);

        ListView weatherListView = (ListView) rootView.findViewById(R.id.listViewForcast);

        weatherListView.setAdapter(forecastAdapter);


        FetchWeatherTask weatherTask = new FetchWeatherTask();
        weatherTask.execute(zipC);



        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]>{

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
      @Override
      public String[] doInBackground(String... zipCode) {

          HttpURLConnection urlConnection = null;
          BufferedReader reader = null;

          // Will contain the raw JSON response as a string.
          String forecastJsonStr = null;

          int numDays = 7;

          try {
              // Construct the URL for the OpenWeatherMap query
              // Possible parameters are avaiable at OWM's forecast API page, at
              // http://openweathermap.org/API#forecast

              Uri.Builder builder = new Uri.Builder();
              builder.scheme("http");
              builder.authority("api.openweathermap.org/data/2.5/forecast/daily");
              builder.appendQueryParameter("q", zipCode[0]);
              builder.appendQueryParameter("mode","json");
              builder.appendQueryParameter("units","metric");
              builder.appendQueryParameter("cnt", Integer.toString(numDays));

              //Log.v(LOG_TAG, "Weather api URL: " +Uri.decode( builder.build().toString()));

              URL url = new URL(Uri.decode( builder.build().toString()));

              // Create the request to OpenWeatherMap, and open the connection
              urlConnection = (HttpURLConnection) url.openConnection();
              urlConnection.setRequestMethod("GET");
              urlConnection.connect();

              // Read the input stream into a String
              InputStream inputStream = urlConnection.getInputStream();
              StringBuffer buffer = new StringBuffer();
              if (inputStream == null) {
                  // Nothing to do.
                  return  null;
              }
              reader = new BufferedReader(new InputStreamReader(inputStream));

              String line;
              while ((line = reader.readLine()) != null) {
                  // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                  // But it does make debugging a *lot* easier if you print out the completed
                  // buffer for debugging.
                  buffer.append(line + "\n");
              }

              if (buffer.length() == 0) {
                  // Stream was empty.  No point in parsing.
                  return null;
              }
              forecastJsonStr = buffer.toString();
          } catch (IOException e) {
              Log.e(LOG_TAG, "Error ", e);
              // If the code didn't successfully get the weather data, there's no point in attemping
              // to parse it.
              return null;
          } finally{
              if (urlConnection != null) {
                  urlConnection.disconnect();
              }
              if (reader != null) {
                  try {
                      reader.close();
                  } catch (final IOException e) {
                      Log.e(LOG_TAG, "Error closing stream", e);
                  }
              }
          }

            //Log.v(LOG_TAG, forecastJsonStr);
            //return  forecastJsonStr;

            String[] forecastArray;

            try {
                forecastArray = getWeatherDataFromJson(forecastJsonStr, numDays);
            }catch (Exception e){
                Log.e(LOG_TAG, "Error getting weather data from JSON ");
                return null;
            }

          return forecastArray;
      }


        @Override
        protected void onPostExecute(String[] strings) {

            if(strings!=null){
                forecastAdapter.clear();
                for(String s: strings){
                    forecastAdapter.add(s);
                }
            }

        }

        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            //for (String s : resultStrs) {
                //Log.v(LOG_TAG, "Forecast entry: " + s);
            //}
            return resultStrs;

        }




    }
}
