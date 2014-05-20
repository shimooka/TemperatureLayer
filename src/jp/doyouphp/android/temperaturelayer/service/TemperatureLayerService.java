/**
 * Copyright 2013,2014 Hideyuki SHIMOOKA <shimooka@doyouphp.jp>
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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

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
    WindowManager.LayoutParams mLayoutParams;
    TextView mCurrentTemperatureText;
    TemperatureLayerConfig mConfig;

    public static boolean isTest = false;

    public static final String KEY_EDIT_MODE = "EDIT_MODE";


    @Override
    public void onCreate() {
        super.onCreate();

        mConfig = new TemperatureLayerConfig(getApplicationContext());

        mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        mView = LayoutInflater.from(this).inflate(R.layout.overlay, null);
        mView.setOnTouchListener(movingListener);

        mCurrentTemperatureText = (TextView)mView.findViewById(R.id.currentTemperature);
        mCurrentTemperatureText.setTextSize(mConfig.getTextSize());
        if (mConfig.getFontPath() != null) {
            mCurrentTemperatureText.setTypeface(Typeface.createFromFile(mConfig.getFontPath()));
        }
        int color = mConfig.getColor();
        mCurrentTemperatureText.setTextColor(Color.argb(Color.alpha(color), Color.red(color),
                Color.green(color), Color.blue(color)));

    }

    final OnTouchListener movingListener = new View.OnTouchListener() {
        int currentX;
        int currentY;
        int previousX;
        int previousY;

        int clickCount = 0;
        long startTime;
        long duration;
        static final int MAX_DURATION = 750;
        static final int REQUIRED_TAP_COUNT = 2;
	    static final int MOVE_DELTA = 2;

        @SuppressWarnings("deprecation")
        @TargetApi(Build.VERSION_CODES.FROYO)
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            boolean consumed = false;
            WindowManager.LayoutParams mLayoutParams = getLayoutParams(true);

            switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                currentX = (int)event.getRawX();
                currentY = (int)event.getRawY();
                previousX = currentX;
                previousY = currentY;

                startTime = System.currentTimeMillis();
                clickCount++;

                consumed = true;
                break;
            case MotionEvent.ACTION_UP:
                duration=  duration + System.currentTimeMillis() - startTime;
                if (clickCount >= REQUIRED_TAP_COUNT) {     // double tap
                    if (duration <= MAX_DURATION) {
                        exitEditMode();
                    }
                    clickCount = 0;
                    duration = 0;
                    consumed = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int dX = (int)event.getRawX() - currentX;
                int dY = (int)event.getRawY() - currentY;
                currentX = (int)event.getRawX();
                currentY = (int)event.getRawY();
                if ((Math.abs(currentX - previousX) >= MOVE_DELTA || Math.abs(currentY - previousY) >= MOVE_DELTA) &&
                    event.getPointerCount() == 1) {
                        mLayoutParams.x += dX;
                        mLayoutParams.y += dY;
                        Log.v(TemperatureLayerActivity.TAG,
                            String.format("dX:%d dY:%d x:%d y:%d", dX, dY, mLayoutParams.x, mLayoutParams.y));

                        mConfig.setX(mLayoutParams.x);
                        mConfig.setY(mLayoutParams.y);

                        try {
                            mWindowManager.updateViewLayout(v, mLayoutParams);
                        } catch (IllegalArgumentException e) {
                            Log.w(TemperatureLayerActivity.TAG, e.getMessage());
                            mWindowManager.addView(v, mLayoutParams);
                        }
                        consumed = true;
                }
                break;
            case MotionEvent.ACTION_POINTER_1_UP:       // ACTION_POINTER_UP
                if (event.getPointerCount() == 2) {     // two-finger touch
                    exitEditMode();
                    consumed = true;
                }
                break;
            default:
                break;
            }

            return consumed;
        }
    };

    private void exitEditMode() {
        Intent intent = new Intent(mConfig.getContext(), TemperatureLayerService.class);
        if (TemperatureLayerActivity.isServiceRunning(mConfig.getContext())) {
            stopService(intent);
        }
        startService(intent);
        Toast.makeText(mConfig.getContext(), R.string.exit_edit_mode, Toast.LENGTH_SHORT).show();
    }

    protected WindowManager.LayoutParams getLayoutParams() {
        return getLayoutParams(false);
    }

    protected WindowManager.LayoutParams getLayoutParams(boolean editMode) {
        if (mLayoutParams == null) {
            mLayoutParams = new WindowManager.LayoutParams();
            mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            mLayoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            mLayoutParams.x = mConfig.getX();
            mLayoutParams.y = mConfig.getY();
            mLayoutParams.format = PixelFormat.TRANSLUCENT;
            mLayoutParams.gravity = Gravity.NO_GRAVITY;
        }

        if (editMode) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
            mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        }

        return mLayoutParams;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (!isTest) {
            Notification notification = new Notification();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notification = buildNotification(
                    mConfig.getContext().getString(R.string.notification_ticker),
                    mConfig.getContext().getString(R.string.notification_content));
            }

            // require API Level 5
            startForeground(R.string.app_name, notification);
        }

        boolean editMode = false;
        if (intent != null && intent.getExtras() != null) {
            editMode = intent.getExtras().getBoolean(KEY_EDIT_MODE);
        }

        if (editMode) {
            int edit_color = mConfig.getContext().getResources().getInteger(R.color.edit_color);
            int color = Color.argb(Color.alpha(edit_color), Color.red(edit_color),
                Color.green(edit_color), Color.blue(edit_color));
            mCurrentTemperatureText.setTextColor(Color.argb(Color.alpha(color), Color.red(color),
                    Color.green(color), Color.blue(color)));
        }

        mWindowManager.addView(mView, getLayoutParams(editMode));

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(broadcastReceiver, filter);

        Log.i(TemperatureLayerActivity.TAG,
                "TemperatureLayerService started : id=" + startId + " with "
                        + intent);

        return START_STICKY;
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    protected Notification buildNotification(String tickerText, String contentText) {
        Notification notification = new Notification();
        Intent intent = new Intent(mConfig.getContext(), TemperatureLayerActivity.class);
        notification.icon = R.drawable.ic_stat_name;
        notification.tickerText = tickerText;
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.setLatestEventInfo(
            mConfig.getContext(),
            mConfig.getContext().getString(R.string.app_name),
            contentText,
            PendingIntent.getActivity(mConfig.getContext(), 0, intent, 0));

        Drawable largeIconDrawable = getResources().getDrawable(R.drawable.ic_launcher);
        Bitmap largeIconBitmap = ((BitmapDrawable)largeIconDrawable).getBitmap();

        if (!mConfig.withIcon()) {
            // API 11<=
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                notification.largeIcon = largeIconBitmap;
            }
            // API 16<=
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notification.priority = Notification.PRIORITY_MIN;
            }
        }

        return notification;
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

        super.onDestroy();

        Log.i(TemperatureLayerActivity.TAG, "Service stopped");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v(TemperatureLayerActivity.TAG, "action : " + action);

            if (action != null && action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int temperature = intent.getIntExtra("temperature", 0);
                String temperatureString = getTemperatureAsString(mConfig,
                        temperature);

                if (mCurrentTemperatureText != null &&
                        mCurrentTemperatureText.getText().equals(temperatureString)) {
                    return;
                }
                mCurrentTemperatureText.setText(temperatureString);
                Log.v(TemperatureLayerActivity.TAG, "current : "
                        + temperatureString);

                if (mConfig.isNotify()) {
                    NotificationManager notificationManager =
                        (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                    Notification notification = buildNotification(
                        mConfig.getContext().getString(R.string.notification_message, temperatureString),
                        temperatureString);

                    notificationManager.notify(R.string.app_name, notification);
                }

                if (temperature >= mConfig.getTemperatureThreshold()) {
                    if (mConfig.withVibration()) {
                        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(1000);
                    }

                    if (mConfig.withSound()) {
                        Ringtone ringtone = RingtoneManager.getRingtone(mConfig.getContext(), Uri.parse(mConfig.getAlertSound()));
                        ringtone.play();
/*
                        MediaPlayer mMediaPlayer = new MediaPlayer();
                        try {
                            mMediaPlayer.setDataSource(mConfig.getContext(), Uri.parse(mConfig.getRingtone()));
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
