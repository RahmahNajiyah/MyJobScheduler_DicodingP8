# MyJobScheduler_DicodingP8
membuat sebuah proses terjadwal (scheduler task) untuk mengunduh data cuaca per 3 menit sekali, Aplikasi akan menampilkan notifikasi cuaca saat ini.

kode dari GetCurrentWeatherJobService secara keseluruhan. Ada dua fungsi utama ketika kelas ini dijalankan :

1. Melalukan koneksi data ke webservice openweathermap.org melalui koneksi Internet
Pada kesempatan ini, kita akan menggunakan library  AsyncHttpClient (LoopJ), Untuk memudahkan proses transaksi data dengan webservice
Kita menggunakan gradle untuk mengintegrasikan AsyncHttpClient ke aplikasi.

Pada metode dibawah ini kita menggunakan AsyncHttpClient. Metode diatas memiliki tanggung jawab untuk bertransaksi data dengan openweathermap.org. 

Untuk terhubung dengan openweathermap.org, kita hanya membutuhkan buat obyek dari kelas AsyncHttpClient dan menjalankan get() dengan inputan parameter yang dibutuhkan. Metode-metode yang ada AsyncHttpClient merepresentasikan metode HTTP seperti POST, GET, PUT, HEAD dan DELETE. 

Target dari sumber daya pada webservice yang diakses adalah "http://api.openweathermap.org/data/2.5/weather?q="+CITY+"&appid="+APP_ID; 

Pada bagian CITY , kita beri nilai Jakarta. Sementara pada bagian APP_ID, kita masukkan nilai API_KEY yang telah disediakan oleh openweathermap.org.

private void getCurrentWeather(final JobParameters job){
    Log.d(TAG, "Running");
    AsyncHttpClient client = new AsyncHttpClient();
    String url = "http://api.openweathermap.org/data/2.5/weather?q="+CITY+"&appid="+APP_ID;
    Log.e(TAG, "getCurrentWeather: "+url );
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
                String message = currentWeather +", "+description+" with "+temprature+" celcius";
                int notifId = 100;
 
                showNotification(getApplicationContext(), title, message, notifId);
 
                // ketika proses selesai, maka perlu dipanggil jobFinished dengan parameter false;
                jobFinished(job, false);
            }catch (Exception e){
                // ketika terjadi error, maka jobFinished diset dengan parameter true. Yang artinya job perlu di reschedule
                jobFinished(job, true);
                e.printStackTrace();
            }
        }
 
        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            // ketika proses gagal, maka jobFinished diset dengan parameter true. Yang artinya job perlu di reschedule
            jobFinished(job, true);
        }
    });
}

2. Menampilkan notifikasi ke pengguna tentang cuaca saat ini
double tempInCelcius = tempInKelvin - 273;
String temprature = new DecimalFormat("##.##").format(tempInCelcius);
String title = "Current Weather";
String message = currentWeather +", "+description+" with "+temprature+" celcius";
int notifId = 100;
showNotification(getApplicationContext(), title, message, notifId);

Pada kode di atas, kita harus mengubah nilai suhu dari satuan Kelvin menjadi satuan Celcius. Caranya adalah dengan mengurangi nilainya dengan 273.
new DecimalFormat("##.##").format(tempInCelcius) → digunakan untuk memformat tampilan agar hanya ada dua nilai dibelakang koma.
Ketika showNotification(getApplicationContext(), title, message, notifId); dijalankan, maka notifikasi akan tampil di panel notifikasi pengguna.

Terakhir, pada android Manifest kita menambahkan satu baris permission yang menandakan aplikasi ini dapat mengakses internet.

<uses-permission android:name="android.permission.INTERNET"/>
Dan
<service android:name=".GetCurrentWeatherJobService" android:enabled="true"
   android:exported="true"
   android:permission="android.permission.BIND_JOB_SERVICE"/>
   
Kita daftarkan kelas JobService agar dikenali oleh Android System. Bila belum didaftarkan, maka job tersebut tidak akan dijalankan oleh Android.
Konsekuensi lain adalah kegagalan di sistem, karena job tersebut tidak dapat ditemukan.
