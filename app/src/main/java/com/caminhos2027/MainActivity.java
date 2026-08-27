package com.caminhos2027;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.webkit.GeolocationPermissions;
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

    private WebView webView;
    private TextToSpeech tts;
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
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void configureWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setGeolocationEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);

        assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return assetLoader.shouldInterceptRequest(android.net.Uri.parse(url));
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
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
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "centenario");
            });
        }

        @android.webkit.JavascriptInterface
        public void setKeepScreenOn(boolean enabled) {
            runOnUiThread(() -> {
                if (enabled) {
                    getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            });
        }

        @android.webkit.JavascriptInterface
        public void startCompass() {
            if (sensorManager == null || rotationSensor == null || compassRunning) return;
            compassRunning = sensorManager.registerListener(
                    MainActivity.this,
                    rotationSensor,
                    SensorManager.SENSOR_DELAY_GAME
            );
        }

        @android.webkit.JavascriptInterface
        public void stopCompass() {
            stopCompassInternal();
        }
    }

    private void stopCompassInternal() {
        if (sensorManager != null && compassRunning) {
            sensorManager.unregisterListener(this);
        }
        compassRunning = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!compassRunning || event.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR || webView == null) return;

        float[] rotationMatrix = new float[9];
        float[] orientation = new float[3];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        SensorManager.getOrientation(rotationMatrix, orientation);

        double azimuth = Math.toDegrees(orientation[0]);
        if (azimuth < 0) azimuth += 360.0;
        final double heading = azimuth;

        runOnUiThread(() -> webView.evaluateJavascript(
                "window.setDeviceHeading && window.setDeviceHeading(" + heading + ")", null
        ));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No-op.
    }

    @Override
    protected void onDestroy() {
        stopCompassInternal();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
