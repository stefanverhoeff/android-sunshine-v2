package com.example.android.sunshine.app;

class WeatherSettings {
    private String location;
    private String temperatureUnit;

    WeatherSettings(String location, String temperatureUnit) {
        this.location = location;
        this.temperatureUnit = temperatureUnit;
    }

    String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    String getTemperatureUnit() {
        return temperatureUnit;
    }

    public void setTemperatureUnit(String temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }
}
