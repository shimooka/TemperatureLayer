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

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * the main service class of {@link TemperatureLayerActivity}
 *
 * This service will register a BroadcastReceiver which receives Intent.ACTION_BATTERY_CHANGED
 * and update battery temperature string on inflated layer.
 */
public class TemperatureLayerService extends Service {
    View mView;
    WindowManager mWindowManager;
    SharedPreferences mSharedPreferences;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TemperatureLayerActivity.TAG, "onStartCommand");
        startForeground(1, new Notification());

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        mView = layoutInflater.inflate(R.layout.overlay, null);
        mSharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(mView.getContext());

        TextView text = (TextView)mView.findViewById(R.id.currentTemperature);
        text.setTextSize(mSharedPreferences.getInt(
                "key_text_size",
                getResources().getInteger(R.integer.default_text_size)
        ));
        int color = mSharedPreferences.getInt(
                "key_color",
                getResources().getColor(R.color.default_color)
        );
        text.setTextColor(Color.argb(
                Color.alpha(color),
                Color.red(color),
                Color.green(color),
                Color.blue(color)
        ));

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        params.gravity = mSharedPreferences.getInt(
                "key_layout",
                getResources().getInteger(R.integer.default_layout)
        );

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(mView, params);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(broadcastReceiver, filter);

        Log.v(TemperatureLayerActivity.TAG,
                "Service started : id " + startId + " with " + intent);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
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
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            Log.v(TemperatureLayerActivity.TAG, "action : " + action);
            if (action != null && action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int temperature = intent.getIntExtra("temperature", 0);
                String temperatureString = getTemperatureAsString(context, temperature);

                TextView text = (TextView)mView.findViewById(R.id.currentTemperature);
                text.setText(temperatureString);
                Log.v(TemperatureLayerActivity.TAG, "current : " + temperatureString);
            }
        }

        private String getTemperatureAsString(Context context, int temperature) {
            final String unitCelsius =
                    context.getString(R.string.default_temperature_unit);
            final String unitCurrent =
                    mSharedPreferences.getString("key_temperature_unit", unitCelsius);
            boolean useCelsius = unitCurrent.equals(unitCelsius);

            return context.getString(
                    R.string.string_degree,
                    Double.toString(Math.floor(
                            useCelsius ? temperature : temperature * 9f / 5f + 320
                    ) / 10),
                    unitCurrent
            );
        }
    };
}
