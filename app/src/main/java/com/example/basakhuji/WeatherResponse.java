package com.example.basakhuji;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherResponse {

    @SerializedName("main")
    private MainData main;

    @SerializedName("weather")
    private List<WeatherData> weather;

    public MainData getMain() {
        return main;
    }

    public List<WeatherData> getWeather() {
        return weather;
    }

    // Nested class representing the main weather data
    public static class MainData {
        @SerializedName("temp")
        private double temp;

        @SerializedName("humidity")
        private double humidity;

        public double getTemp() {
            return temp;
        }

        public double getHumidity() {
            return humidity;
        }
    }

    // Nested class representing weather data
    public static class WeatherData {
        @SerializedName("main")
        private String main;

        @SerializedName("description")
        private String description;

        public String getMain() {
            return main;
        }

        public String getDescription() {
            return description;
        }
    }
}
