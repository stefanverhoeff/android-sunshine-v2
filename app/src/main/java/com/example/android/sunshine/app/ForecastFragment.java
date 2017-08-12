package com.example.android.sunshine.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ForecastFragment extends Fragment {
    static String LOG_TAG = ForecastFragment.class.getSimpleName();

    private static final Double LEMPALA_LAT = 61.31323;
    private static final Double LEMPALA_LON = 23.75497;
    private List<String> weatherData = new ArrayList<>();
    private ArrayAdapter<String> forecastAdaptor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);

        Context context = getActivity();

        weatherData.addAll(Arrays.asList("Hey", "Ho", "hey", "ho", "off", "to", "work", "we", "go"));
        forecastAdaptor = new ArrayAdapter<>(
                context,
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weatherData);

        ListView listViewForecast = rootView.findViewById(R.id.listview_forecast);
        listViewForecast.setAdapter(forecastAdaptor);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_refresh:
                fetchWeatherData();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fetchWeatherData() {
        WeatherFetchingTask weatherFetcher = new WeatherFetchingTask();
        weatherFetcher.execute(LEMPALA_LAT, LEMPALA_LON);
        String[] weatherForecast = null;
        try {
            weatherForecast = weatherFetcher.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        Log.i(LOG_TAG + ":forecasts", Arrays.toString(weatherForecast));
        weatherData.clear();
        if (weatherForecast != null)
            weatherData.addAll(Arrays.asList(weatherForecast));

        forecastAdaptor.notifyDataSetChanged();
    }
}
