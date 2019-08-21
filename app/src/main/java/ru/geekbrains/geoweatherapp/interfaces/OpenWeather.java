package ru.geekbrains.geoweatherapp.interfaces;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import ru.geekbrains.geoweatherapp.models.WeatherRequest;


public interface OpenWeather {
    @GET("data/2.5/weather")
    Call<WeatherRequest> loadWeather( @Query("lon") float longitude, @Query("lat") float latitude, @Query("appid") String keyApi);
}
