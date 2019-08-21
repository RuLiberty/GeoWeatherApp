package ru.geekbrains.geoweatherapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.geekbrains.geoweatherapp.interfaces.OpenWeather;
import ru.geekbrains.geoweatherapp.models.WeatherRequest;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSION_REQUEST_CODE = 10;
    private static final String KEY_API_WEATHER = "3d1ebc018f306cf73036dd285969216c";
    private Button btnGetGeo;
    private TextView textCity;
    private TextView textTemp;
    private OpenWeather openWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initRetorfit();
        initOnClickEvent();
    }

    private void initOnClickEvent() {
        btnGetGeo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation(); // получаем координаты
            }
        });
    }

    private void initView(){
        btnGetGeo = findViewById(R.id.btn_request);
        textCity = findViewById(R.id.text_city);
        textTemp = findViewById(R.id.text_temp);

        textTemp.setText(String.valueOf(""));
        textCity.setText(String.valueOf(""));
    }

    /*
    Работа с
    JSON и API
     */

    private void requestWeather(float mlongitude, float mlatitude) {
            requestRetrofit(mlongitude, mlatitude);
    }

    private void initRetorfit() {
        Retrofit retrofit;
        retrofit = new Retrofit.Builder()
// Базовая часть адреса
                .baseUrl("http://api.openweathermap.org/")
// Конвертер, необходимый для преобразования JSON в объекты
                .addConverterFactory(GsonConverterFactory.create())
                .build();
// Создаем объект, при помощи которого будем выполнять запросы
        openWeather = retrofit.create(OpenWeather.class);
    }

    private void requestRetrofit(final float longitude, final float latitude){
        openWeather.loadWeather(longitude, latitude, KEY_API_WEATHER)
                .enqueue(new Callback<WeatherRequest>() {
                    @Override
                    public void onResponse(@NonNull Call<WeatherRequest> call, @NonNull Response<WeatherRequest> response) {
                        if (response.body() != null) {
                            float faring = response.body().getMain().getTemp();
                            int cels = (int) faring - 272;

                            String result = String.valueOf(cels) + " °С";
                            String city = response.body().getName();

                            textTemp.setText(String.valueOf("Температура "+ result));
                            textCity.setText(String.valueOf("Текущее положение: "+ city));
                        }else {
                            Log.d("MAIN_TAG","Error body");
                            Log.d("MAIN_TAG",openWeather.loadWeather(longitude, latitude, KEY_API_WEATHER).request().toString());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<WeatherRequest> call, @NonNull Throwable t) {
                        Log.d("MAIN_TAG","Wrong city!");
                    }
                });

    }

    /*
    Работа с
    Гео локацией
     */

    public void getLocation(){
        // Проверим на пермиссии, и если их нет, запросим у пользователя
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
// Запросим координаты
            requestLocation();
        } else {
// Пермиссии нет, будем запрашивать у пользователя
            requestLocationPermissions();
        }
    }

    // запрашиваем текущие координаты
    private void requestLocation() {
// Если пермиссии все-таки нет - просто выйдем, приложение не имеет смысла
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);

// Получим наиболее подходящий провайдер геолокации по критериям
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider != null) {
// Получим координаты и закроем листенер
            LocationListener locationListener = getLocationListener();
            locationManager.requestLocationUpdates(provider, 10000, 10, locationListener);
        }
    }

    // создаем локейшен Листенер
    public LocationListener getLocationListener(){
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
// Широта
                float latitude = (float) location.getLatitude();
// Долгота
                float longitude = (float) location.getLongitude();

               requestWeather(latitude, longitude); // по координатам запрашиваем погоду
            }
            @Override
            public void onStatusChanged(String provider1, int status, Bundle extras) {
            }
            @Override
            public void onProviderEnabled(String provider1) {
            }
            @Override
            public void onProviderDisabled(String provider1) {
            }
        };
    }

    // Запрос пермиссии для геолокации
    private void requestLocationPermissions() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
// Запросим эти две пермиссии у пользователя
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    PERMISSION_REQUEST_CODE);
        }
    }

    // Это результат запроса у пользователя пермиссии
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
    // проверка пермиссии
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length == 2 &&
                    (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                // Пермиссия дана
                requestLocation();
            }
        }
    }

}

