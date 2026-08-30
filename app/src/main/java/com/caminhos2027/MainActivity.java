package com.caminhos2027;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Notification;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.webkit.WebViewAssetLoader;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity implements SensorEventListener {
    private static final int LOCATION_REQ = 1001;
    private static final int FILE_PICKER_REQ = 2001;
    private static final int NOTIFICATION_REQ = 3001;
    private static final String WATCH_CHANNEL = "peregrino_walk";

    private WebView webView;
    private TextToSpeech tts;
    private ValueCallback<Uri[]> pendingFileCallback;
    private WebViewAssetLoader assetLoader;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private boolean compassRunning = false;
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_REQ);
        }
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("pt", "PT"));
                tts.setSpeechRate(0.96f);
            }
        });
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        webView = new WebView(this);
        setContentView(webView);
        configureWebView();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQ);
        }
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html");
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        assetLoader = new WebViewAssetLoader.Builder().addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
        webView.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) { return assetLoader.shouldInterceptRequest(request.getUrl()); }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) { callback.invoke(origin, true, false); }
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (pendingFileCallback != null) pendingFileCallback.onReceiveValue(null);
                pendingFileCallback = callback;
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                try { startActivityForResult(intent, FILE_PICKER_REQ); return true; }
                catch (Exception e) { pendingFileCallback = null; return false; }
            }
        });
        webView.addJavascriptInterface(new AndroidBridge(), "Android");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(WATCH_CHANNEL, "Caminhada", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Alertas de navegação dos Caminhos do Peregrino");
            channel.enableVibration(true);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    public class AndroidBridge {
        @android.webkit.JavascriptInterface public void speak(String text) {
            if (tts == null || text == null || text.trim().isEmpty()) return;
            runOnUiThread(() -> { tts.stop(); tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "peregrino"); });
        }
        @android.webkit.JavascriptInterface public void notifyWatch(String title, String text) {
            final String t = (title == null || title.trim().isEmpty()) ? "Caminhos do Peregrino" : title;
            final String body = text == null ? "" : text;
            runOnUiThread(() -> {
                NotificationManager manager = getSystemService(NotificationManager.class);
                if (manager == null) return;
                if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
                Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(MainActivity.this, WATCH_CHANNEL) : new Notification.Builder(MainActivity.this);
                b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(t).setContentText(body).setStyle(new Notification.BigTextStyle().bigText(body)).setAutoCancel(true).setVibrate(new long[]{0,180,100,180});
                manager.notify((int)(System.currentTimeMillis() & 0x7fffffff), b.build());
            });
        }
        @android.webkit.JavascriptInterface public void downloadRoute(String url, String routeName) {
            if (url == null || routeName == null) return;
            if (!(url.startsWith("https://caminhosdefatima.org/") || url.startsWith("https://caminhosdefatima.com/"))) { routeError(routeName, "Fonte não autorizada."); return; }
            networkExecutor.execute(() -> {
                HttpURLConnection c = null;
                try {
                    c = (HttpURLConnection)new URL(url).openConnection(); c.setRequestMethod("GET"); c.setConnectTimeout(20000); c.setReadTimeout(30000); c.setInstanceFollowRedirects(true); c.setRequestProperty("User-Agent", "Caminhos-do-Peregrino/1.1");
                    int code=c.getResponseCode(); if(code<200||code>=300) throw new Exception("HTTP "+code);
                    try(InputStream in=c.getInputStream(); BufferedReader br=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))){StringBuilder out=new StringBuilder();char[] buf=new char[8192];int n;while((n=br.read(buf))!=-1)out.append(buf,0,n);String js="window.__routeDownloadDone("+JSONObject.quote(routeName)+","+JSONObject.quote(out.toString())+",null)";runOnUiThread(()->webView.evaluateJavascript(js,null));}
                } catch(Exception e){routeError(routeName,e.getMessage()==null?"Erro de rede":e.getMessage());} finally {if(c!=null)c.disconnect();}
            });
        }
        private void routeError(String routeName,String message){String js="window.__routeDownloadDone("+JSONObject.quote(routeName)+",null,"+JSONObject.quote(message)+")";runOnUiThread(()->webView.evaluateJavascript(js,null));}
        @android.webkit.JavascriptInterface public void setKeepScreenOn(boolean enabled){runOnUiThread(()->{if(enabled)getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);});}
        @android.webkit.JavascriptInterface public void startCompass(){if(sensorManager==null||rotationSensor==null||compassRunning)return;compassRunning=sensorManager.registerListener(MainActivity.this,rotationSensor,SensorManager.SENSOR_DELAY_GAME);}
        @android.webkit.JavascriptInterface public void stopCompass(){if(sensorManager!=null&&compassRunning)sensorManager.unregisterListener(MainActivity.this);compassRunning=false;}
    }

    @Override public void onSensorChanged(SensorEvent event){if(!compassRunning||event.sensor.getType()!=Sensor.TYPE_ROTATION_VECTOR||webView==null)return;float[] matrix=new float[9],orientation=new float[3];SensorManager.getRotationMatrixFromVector(matrix,event.values);SensorManager.getOrientation(matrix,orientation);double heading=Math.toDegrees(orientation[0]);if(heading<0)heading+=360.0;final double h=heading;runOnUiThread(()->webView.evaluateJavascript("window.setDeviceHeading && window.setDeviceHeading("+h+")",null));}
    @Override public void onAccuracyChanged(Sensor sensor,int accuracy){}
    @Override protected void onActivityResult(int requestCode,int resultCode,@Nullable Intent data){if(requestCode==FILE_PICKER_REQ&&pendingFileCallback!=null){Uri[] results=null;if(resultCode==RESULT_OK&&data!=null&&data.getData()!=null)results=new Uri[]{data.getData()};pendingFileCallback.onReceiveValue(results);pendingFileCallback=null;}super.onActivityResult(requestCode,resultCode,data);}
    @Override protected void onDestroy(){if(sensorManager!=null&&compassRunning)sensorManager.unregisterListener(this);if(tts!=null){tts.stop();tts.shutdown();}networkExecutor.shutdownNow();if(webView!=null)webView.destroy();super.onDestroy();}
}
