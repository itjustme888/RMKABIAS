package com.mirea.kae.httpurlconnection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import com.mirea.kae.httpurlconnection.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.button.setOnClickListener(v -> onClickLoad());
    }
    private void onClickLoad() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (cm != null) networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            new LoadIpAndWeatherTask().execute();
        } else {
            Toast.makeText(this, "Нет интернета", Toast.LENGTH_SHORT).show();
        }
    }
    private class LoadIpAndWeatherTask extends AsyncTask<Void, Void, ResultData> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            binding.tvIp.setText("IP: загружаем...");
        }

        @Override
        protected ResultData doInBackground(Void... voids) {
            ResultData out = new ResultData();

            try {
                String ipInfoJson = downloadText("https://ipinfo.io/json");

                JSONObject ipJson = new JSONObject(ipInfoJson);
                out.ip = ipJson.optString("ip", "-");
                out.city = ipJson.optString("city", "-");
                out.region = ipJson.optString("region", "-");
                out.country = ipJson.optString("country", "-");

                String loc = ipJson.optString("loc", "");
                String[] parts = loc.split(",");
                if (parts.length == 2) {
                    out.latitude = parts[0];
                    out.longitude = parts[1];

                    String weatherUrl =
                            "https://api.open-meteo.com/v1/forecast?latitude=" + out.latitude
                                    + "&longitude=" + out.longitude
                                    + "&current_weather=true";

                    String weatherJson = downloadText(weatherUrl);
                    JSONObject w = new JSONObject(weatherJson);
                    JSONObject current = w.getJSONObject("current_weather");

                    out.temperature = String.valueOf(current.optDouble("temperature"));
                    out.windspeed = String.valueOf(current.optDouble("windspeed"));
                }

            } catch (IOException | JSONException e) {
                out.error = e.toString();
            }

            return out;
        }

        @Override
        protected void onPostExecute(ResultData r) {
            super.onPostExecute(r);

            if (r.error != null) {
                binding.tvIp.setText("Ошибка: " + r.error);
                return;
            }

            binding.tvIp.setText("IP: " + r.ip);
            binding.tvCountry.setText("Страна: " + r.country);
            binding.tvRegion.setText("Регион: " + r.region);
            binding.tvCity.setText("Город: " + r.city);
            binding.tvLat.setText("Широта: " + (r.latitude == null ? "-" : r.latitude));
            binding.tvLon.setText("Долгота: " + (r.longitude == null ? "-" : r.longitude));
            binding.tvTemp.setText("Температура: " + (r.temperature == null ? "-" : r.temperature));
            binding.tvWind.setText("Ветер: " + (r.windspeed == null ? "-" : r.windspeed));
        }
    }

    private String downloadText(String address) throws IOException {
        InputStream inputStream = null;
        String data;

        URL url = new URL(address);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(100000);
        connection.setConnectTimeout(100000);
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(true);
        connection.setUseCaches(false);
        connection.setDoInput(true);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            inputStream = connection.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int read;
            while ((read = inputStream.read()) != -1) {
                bos.write(read);
            }
            bos.close();
            data = bos.toString();
        } else {
            data = connection.getResponseMessage() + ". Error Code: " + responseCode;
        }

        connection.disconnect();
        if (inputStream != null) inputStream.close();

        return data;
    }

    private static class ResultData {
        String ip, country, region, city;
        String latitude, longitude;
        String temperature, windspeed;
        String error;
    }
}