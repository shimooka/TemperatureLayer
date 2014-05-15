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

package jp.doyouphp.android.temperaturelayer;

import java.util.List;

import jp.doyouphp.android.temperaturelayer.R;
import jp.doyouphp.android.temperaturelayer.service.TemperatureLayerService;
import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * the main Activity class of TemperatureLayer
 */
public class TemperatureLayerActivity extends Activity {
    public static final String TAG = "TemperatureLayer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String versionName = "0.0.0";
        PackageManager packageManager = getPackageManager();
        try {
            versionName = packageManager.getPackageInfo(getPackageName(),
                    PackageManager.GET_ACTIVITIES).versionName;
        } catch (NameNotFoundException e) {
            Log.w(TAG, "failed to get versionName");
        }
        setTitle(getString(R.string.app_name));

        setContentView(R.layout.activity_temperature_layer);

        Button btn = (Button) findViewById(R.id.StartButton);
        btn.setOnClickListener(btnListener);
        btn = (Button) findViewById(R.id.StopButton);
        btn.setOnClickListener(btnListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.temperature_layer, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_settings:
            Intent intent = new Intent();
            intent.setClassName(this.getPackageName(),
                    SettingActivity.class.getName());
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        flipButton();
    }

    private OnClickListener btnListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.StartButton:
                startService(new Intent(TemperatureLayerActivity.this,
                        TemperatureLayerService.class));
                break;
            case R.id.StopButton:
                stopService(new Intent(TemperatureLayerActivity.this,
                        TemperatureLayerService.class));
                break;
            default:
                return;
            }
            flipButton();
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void flipButton() {
        boolean running = isServiceRunning(this);
        Button btn = (Button) findViewById(R.id.StartButton);
        btn.setEnabled(!running);
        btn = (Button) findViewById(R.id.StopButton);
        btn.setEnabled(running);
    }

    /**
     * Returns if TemperatureLayer service is running or not
     *
     * @param Context context Context object
     * @return boolean true if service is running or false
     */
    public static boolean isServiceRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> services = activityManager
                .getRunningServices(Integer.MAX_VALUE);
        final String mServiceName = TemperatureLayerService.class
                .getCanonicalName();

        for (RunningServiceInfo info : services) {
            if (mServiceName.equals(info.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static RunningServiceInfo getRunningService(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> services = activityManager
                .getRunningServices(Integer.MAX_VALUE);
        final String mServiceName = TemperatureLayerService.class
                .getCanonicalName();

        for (RunningServiceInfo info : services) {
            if (mServiceName.equals(info.service.getClassName())) {
                return info;
            }
        }
        return null;
    }
}
