package com.hafidzqode.myjobscheduler_dicodingp8;

import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONObject;

import java.text.DecimalFormat;

import cz.msebera.android.httpclient.Header;

//todo 2 make a java class
//red > alt+enter > implements method > choose the both method
public class GetCurrentWeatherJobService extends JobService {

    //todo 5
    public static final String TAG = GetCurrentWeatherJobService.class.getSimpleName();

    final String APP_ID = "4d73c23a515c0a652fae489dfa47ce79"; //put your api key last time here

    //private final String CITY = "ISIKAN DENGAN NAMA KOTA KAMU";

    final String CITY = "JAKARTA";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob() Executed");
        getCurrentWeather(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob() Executed");
        return true;
    }

    private void getCurrentWeather(final JobParameters job){
        Log.d(TAG, "Running");
        AsyncHttpClient client = new AsyncHttpClient();
        String url = "http://api.openweathermap.org/data/2.5/weather?q="+CITY+"&appid="+APP_ID;
        Log.e(TAG, "getCurrentWeather:"+url);
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                Log.d(TAG, result);
                try {
                    JSONObject responseObject = new JSONObject(result);
                    String currentWeather = responseObject.getJSONArray("weather").getJSONObject(0).getString("main");
                    String description = responseObject.getJSONArray("weather").getJSONObject(0).getString("description");

                    double tempInKelvin = responseObject.getJSONObject("main").getDouble("temp");

                    double tempInCelcius = tempInKelvin - 273;

                    String temprature = new DecimalFormat("##.##").format(tempInCelcius);

                    String title = "Current Weather";
                    String message = currentWeather + ", "+description+"with"+temprature+" celcius";
                    int notifId = 100;

                    showNotification(getApplicationContext(), title, message, notifId);

                    jobFinished(job, false);
                }catch (Exception e){
                    //ketika terjadi error, maka jobfinished diset dengan parameter true. yang artinya job perlu di rescheduler
                    jobFinished(job, true);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                //ketika proses gagal, maka jobFinish ed di set dengan parameter true. yang artinya job perlu di reschedule
                jobFinished(job, true);
            }
        });
    }

    private void showNotification(Context context, String title, String message, int notifId) {
        NotificationManager notificationManagerCompat = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_replay_black_24dp)
                .setContentText(message)
                .setColor(ContextCompat.getColor(context, android.R.color.black))
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(alarmSound);

        notificationManagerCompat.notify(notifId, builder.build());

    }
}

//Kelas GetCurrentWeatherJobService memilki dua fungsi  ketika dijalankan :
//Melakukan koneksi HTTP ke openweathermap.org. Data akan diakses menggunakan endpoint berikut : "http://api.openweathermap.org/data/2.5/weather?q="+CITY+"&appid="+APP_ID; Pada contoh ini, kita akan meminta data cuaca untuk kota Jakarta.
//Menampilkan notifikasi di panel notifikasi pada perangkat pengguna.