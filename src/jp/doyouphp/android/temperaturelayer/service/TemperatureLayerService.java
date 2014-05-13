/**
 * Copyright 2013 Hideyuki SHIMOOKA <shimooka@doyouphp.jp>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.doyouphp.android.temperaturelayer.service;

import jp.doyouphp.android.temperaturelayer.R;
import jp.doyouphp.android.temperaturelayer.TemperatureLayerActivity;
import jp.doyouphp.android.temperaturelayer.config.TemperatureLayerConfig;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * the main service class of {@link TemperatureLayerActivity}
 *
 * This service will register a BroadcastReceiver which receives
 * Intent.ACTION_BATTERY_CHANGED and update battery temperature string on
 * inflated layer.
 */
public class TemperatureLayerService extends Service {
    View mView;
    WindowManager mWindowManager;
    TemperatureLayerConfig mConfig;
    public static boolean isTest = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mConfig = new TemperatureLayerConfig(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isTest) {
            // require API Level 5
            startForeground(1, new Notification());
        }

        mView = LayoutInflater.from(this).inflate(R.layout.overlay, null);
        TextView text = (TextView) mView.findViewById(R.id.currentTemperature);
        text.setTextSize(mConfig.getTextSize());
        if (mConfig.getFontPath() != null) {
            text.setTypeface(Typeface.createFromFile(mConfig.getFontPath()));
        }
        int color = mConfig.getColor();
        text.setTextColor(Color.argb(Color.alpha(color), Color.red(color),
                Color.green(color), Color.blue(color)));

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = mConfig.getLayout();

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(mView, params);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(broadcastReceiver, filter);

        Log.v(TemperatureLayerActivity.TAG,
                "TemperatureLayerService started : id=" + startId + " with "
                        + intent);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            if (!isTest) {
                Log.e(TemperatureLayerActivity.TAG, e.getMessage());
            }
        }

        if (mWindowManager != null) {
            mWindowManager.removeView(mView);
        }

        Log.v(TemperatureLayerActivity.TAG, "Service stopped");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @SuppressWarnings("deprecation")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v(TemperatureLayerActivity.TAG, "action : " + action);

            if (action != null && action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int temperature = intent.getIntExtra("temperature", 0);
                String temperatureString = getTemperatureAsString(mConfig,
                        temperature);

                TextView text = (TextView) mView
                        .findViewById(R.id.currentTemperature);
                text.setText(temperatureString);
                Log.v(TemperatureLayerActivity.TAG, "current : "
                        + temperatureString);

                if (mConfig.isNotify()) {
                    NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                    Notification notification = new Notification();
                    Intent i = new Intent(getApplicationContext(), TemperatureLayerActivity.class);
                    PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);
                    notification.icon = R.drawable.ic_stat_name;
                    notification.tickerText = "Current battery temperature is " + temperatureString;
                    notification.setLatestEventInfo(getApplicationContext(), "Temperature Layer", temperatureString, pi);

                    notificationManager.notify(1, notification);
                }

                if (temperature >= mConfig.getTemperatureThreshold()) {
                    if (mConfig.withVibration()) {
                        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(1000);
                    }

                    if (mConfig.withSound()) {
                        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(mConfig.getAlertSound()));
                        ringtone.play();
/*
                        MediaPlayer mMediaPlayer = new MediaPlayer();
                        try {
                            mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(mConfig.getRingtone()));
                            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
                            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    mediaPlayer.release();
                                }
                            });
                            mMediaPlayer.prepare();
                        } catch (Exception e) {
                            Log.e(TemperatureLayerActivity.TAG, e.getMessage());
                            mMediaPlayer.stop();
                            mMediaPlayer.release();
                            return;
                        }
                        mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                            @Override
                            public void onSeekComplete(MediaPlayer mediaPlayer) {
                                mediaPlayer.stop();
                            }
                        });
                        mMediaPlayer.start();
*/
                    }
                }
            }
        }

    };

    public static String getTemperatureAsString(TemperatureLayerConfig config, int temperature) {
        return config.getContext().getString(R.string.string_degree,
                calculateTemperature(temperature, config.useCelsius()),
                config.getTemperatureUnit());
    }

    public static double calculateTemperature(int temperature, boolean useCelsius) {
        return Math.floor(useCelsius ? temperature
                : temperature * 9f / 5f + 320) / 10;
    }
}
