package com.example.basakhuji;

import static com.android.volley.VolleyLog.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AboutUsActivity extends AppCompatActivity {

    private String stringJavaScript = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "  <body>\n" +
            "    <!-- 1. The <iframe> (and video player) will replace this <div> tag. -->\n" +
            "    <div id=\"player\"></div>\n" +
            "\n" +
            "    <script>\n" +
            "      // 2. This code loads the IFrame Player API code asynchronously.\n" +
            "      var tag = document.createElement('script');\n" +
            "\n" +
            "      tag.src = \"https://www.youtube.com/iframe_api\";\n" +
            "      var firstScriptTag = document.getElementsByTagName('script')[0];\n" +


            "      firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);\n" +
            "\n" +
            "      // 3. This function creates an <iframe> (and YouTube player)\n" +
            "      //    after the API code downloads.\n" +
            "      var player;\n" +
            "      function onYouTubeIframeAPIReady() {\n" +
            "        player = new YT.Player('player', {\n" +
            "          height: '195',\n" +
            "          width: '350',\n" +
            "          videoId: 'Vey-jmGJVNU',\n" +
            "          playerVars: {\n" +
            "            'playsinline': 1\n" +
            "          },\n" +
            "          events: {\n" +
            "            'onReady': onPlayerReady,\n" +
            "            'onStateChange': onPlayerStateChange\n" +
            "          }\n" +
            "        });\n" +
            "      }\n" +
            "\n" +
            "      // 4. The API will call this function when the video player is ready.\n" +
            "      function onPlayerReady(event) {\n" +
            "        event.target.playVideo();\n" +
            "      }\n" +
            "\n" +
            "      // 5. The API calls this function when the player's state changes.\n" +
            "      //    The function indicates that when playing a video (state=1),\n" +


            "      //    the player should play for six seconds and then stop.\n" +
            "      var done = false;\n" +
            "      function onPlayerStateChange(event) {\n" +
            "        if (event.data == YT.PlayerState.PLAYING && !done) {\n" +
            "          setTimeout(stopVideo, 1000000);\n" +
            "          done = true;\n" +
            "        }\n" +
            "      }\n" +
            "      function stopVideo() {\n" +
            "        player.stopVideo();\n" +
            "      }\n" +
            "    </script>\n" +
            "  </body>\n" +
            "</html>";


    private WebView webView;
    ImageView backButton;
    private TextView weatherMarqueeTextView;
    private Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        backButton = findViewById(R.id.backButton);
        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Remove default toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Initialize Retrofit instance
        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Initialize weather marquee text view
        weatherMarqueeTextView = findViewById(R.id.weatherMarqueeTextView);
        weatherMarqueeTextView.setSelected(true); // Enable marquee effect

        // Fetch and display weather information
        fetchWeatherData();
    }

    private void fetchWeatherData() {
        // Create API service interface using Retrofit
        WeatherApiService apiService = retrofit.create(WeatherApiService.class);

        // Make API call to fetch weather data
        Call<WeatherResponse> call = apiService.getWeather("Chittagong", "15a7403ff94164b39a76008df5594957");
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    WeatherResponse weatherResponse = response.body();

                    // Extract weather information from response
                    double temperatureKelvin = weatherResponse.getMain().getTemp();
                    double temperatureCelsius = temperatureKelvin - 273.15;
                    double humidity = weatherResponse.getMain().getHumidity();
                    String weatherCondition = "";
                    if (weatherResponse != null && weatherResponse.getWeather() != null && !weatherResponse.getWeather().isEmpty()) {
                        weatherCondition = weatherResponse.getWeather().get(0).getDescription();
                    }

                    // Construct weather information string
                    String weatherInfo = "Weather Information: Temperature: " + String.format("%.2f", temperatureCelsius) + "°C, Humidity: " + humidity + "%, Condition: " + weatherCondition;

                    // Update marquee TextView with weather information
                    weatherMarqueeTextView.setText(weatherInfo);
                    weatherMarqueeTextView.setSelected(true); // Enable marquee effect
                } else {
                    // Handle unsuccessful API response
                    Toast.makeText(AboutUsActivity.this, "Failed to fetch weather data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e(TAG, "Error fetching weather data", t);
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        webView.loadData(stringJavaScript, "text/html", "utf-8");
    }

}
