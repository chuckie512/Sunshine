package com.example.android.sunshine.app;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
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

        ArrayAdapter forecastAdapter = new ArrayAdapter(getActivity(),R.layout.list_item_forcast, R.id.list_item_forcast_textview, weatherData);

        ListView weatherListView = (ListView) rootView.findViewById(R.id.listViewForcast);

        weatherListView.setAdapter(forecastAdapter);


        return rootView;
    }
}
