package com.caminhos2027;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.content.Intent;
import android.provider.Settings;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
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

import java.util.Locale;

public class MainActivity extends Activity implements SensorEventListener {
    private static final int LOCATION_REQ = 1001;
    private static final int FILE_PICKER_REQ = 2001;
    private static final int NOTIFICATION_REQ = 3001;

    private WebView webView;
    private TextToSpeech tts;
    private ValueCallback<Uri[]> pendingFileCallback;
    private WebViewAssetLoader assetLoader;

    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private boolean compassRunning = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("pt", "PT"));
                tts.setSpeechRate(0.96f);
            }
        });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }

        webView = new WebView(this);
        setContentView(webView);
        configureWebView();

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_REQ);
        }

        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html");
        createNotificationChannel();
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                syncNotificationUi();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (pendingFileCallback != null) pendingFileCallback.onReceiveValue(null);
                pendingFileCallback = callback;
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                        "application/gpx+xml", "application/gpx",
                        "application/vnd.google-earth.kml+xml",
                        "application/xml", "text/xml", "text/plain"
                });
                try {
                    startActivityForResult(intent, FILE_PICKER_REQ);
                    return true;
                } catch (Exception e) {
                    pendingFileCallback = null;
                    return false;
                }
            }
        });

        webView.addJavascriptInterface(new AndroidBridge(), "Android");
    }

    public class AndroidBridge {
        @android.webkit.JavascriptInterface
        public void speak(String text) {
            if (tts == null || text == null || text.trim().isEmpty()) return;
            runOnUiThread(() -> {
                tts.stop();
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "peregrino");
            });
        }

        @android.webkit.JavascriptInterface
        public void setKeepScreenOn(boolean enabled) {
            runOnUiThread(() -> {
                if (enabled) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            });
        }

        @android.webkit.JavascriptInterface
        public void startCompass() {
            if (sensorManager == null || rotationSensor == null || compassRunning) return;
            compassRunning = sensorManager.registerListener(
                    MainActivity.this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
        }

        @android.webkit.JavascriptInterface
        public void stopCompass() {
            if (sensorManager != null && compassRunning) {
                sensorManager.unregisterListener(MainActivity.this);
            }
            compassRunning = false;
        }

        @android.webkit.JavascriptInterface
        public void requestNotificationPermission() {
            if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_REQ);
            } else {
                syncNotificationUi();
            }
        }

        @android.webkit.JavascriptInterface
        public boolean notificationsGranted() {
            return Build.VERSION.SDK_INT < 33 || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }

        @android.webkit.JavascriptInterface
        public void openNotificationSettings() {
            try {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
            } catch (Exception ignored) {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())));
            }
        }

        @android.webkit.JavascriptInterface
        public void notifyUser(String title, String text) {
            if (Build.VERSION.SDK_INT >= 33 && !notificationsGranted()) return;
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm == null) return;
            android.app.Notification.Builder b = Build.VERSION.SDK_INT >= 26
                    ? new android.app.Notification.Builder(MainActivity.this, "peregrino")
                    : new android.app.Notification.Builder(MainActivity.this);
            b.setSmallIcon(com.caminhos2027.R.mipmap.ic_launcher)
                    .setContentTitle(title == null ? "Caminhos do Peregrino" : title)
                    .setContentText(text == null ? "" : text)
                    .setAutoCancel(true)
                    .setPriority(android.app.Notification.PRIORITY_DEFAULT);
            nm.notify((int)(System.currentTimeMillis() & 0x7fffffff), b.build());
        }
    }

    private void syncNotificationUi() {
        if (webView == null) return;
        boolean granted = Build.VERSION.SDK_INT < 33 || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        final String js = "(function(){var b=document.getElementById('notificationBtn');if(!b)return;var g=" + granted + ";b.style.background=g?'#eef7ef':'';b.style.borderColor=g?'#cfe3d2':'';b.style.color=g?'#0e4e37':'';b.textContent=g?'✅ Ativos':'🔔 Ativar';b.title=g?'Abrir definições de notificações':'Ativar alertas da caminhada';b.onclick=function(){if(g&&window.Android&&Android.openNotificationSettings){Android.openNotificationSettings();}else if(window.Android&&Android.requestNotificationPermission){Android.requestNotificationPermission();}};})()";
        runOnUiThread(() -> webView.evaluateJavascript(js, null));
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null) {
                NotificationChannel ch = new NotificationChannel("peregrino", "Caminhada", NotificationManager.IMPORTANCE_DEFAULT);
                ch.setDescription("Alertas de navegação, pausas, apoios e condições da caminhada");
                nm.createNotificationChannel(ch);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_REQ) syncNotificationUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncNotificationUi();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!compassRunning || event.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR || webView == null) return;

        float[] matrix = new float[9];
        float[] orientation = new float[3];
        SensorManager.getRotationMatrixFromVector(matrix, event.values);
        SensorManager.getOrientation(matrix, orientation);

        double heading = Math.toDegrees(orientation[0]);
        if (heading < 0) heading += 360.0;
        final double h = heading;

        runOnUiThread(() -> webView.evaluateJavascript(
                "window.setDeviceHeading && window.setDeviceHeading(" + h + ")", null));
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == FILE_PICKER_REQ && pendingFileCallback != null) {
            Uri[] results = null;
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                results = new Uri[]{data.getData()};
            }
            pendingFileCallback.onReceiveValue(results);
            pendingFileCallback = null;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        if (sensorManager != null && compassRunning) sensorManager.unregisterListener(this);
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
