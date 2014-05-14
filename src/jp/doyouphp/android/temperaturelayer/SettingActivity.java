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

import java.util.HashMap;
import java.util.Map;

import com.ulduzsoft.font.FontManager;

import jp.doyouphp.android.temperaturelayer.config.TemperatureLayerConfig;
import jp.doyouphp.android.temperaturelayer.service.TemperatureLayerService;

import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.view.WindowManager;

/**
 * an Activity class for setting screen
 */
public class SettingActivity extends PreferenceActivity {
    private static final String[] PREFERENCE_KEYS = {
            TemperatureLayerConfig.KEY_START_ON_BOOT,
            TemperatureLayerConfig.KEY_NOTIFICATION,
            TemperatureLayerConfig.KEY_TEMPERATURE_UNIT,
            TemperatureLayerConfig.KEY_LAYOUT,
            TemperatureLayerConfig.KEY_TEXT_SIZE,
            TemperatureLayerConfig.KEY_FONT,
            TemperatureLayerConfig.KEY_COLOR,
            TemperatureLayerConfig.KEY_ALERT,
            TemperatureLayerConfig.KEY_TEMPERATURE_THRESHOLD,
            TemperatureLayerConfig.KEY_SOUND,
            TemperatureLayerConfig.KEY_ALERT_SOUND,
            TemperatureLayerConfig.KEY_VIBRATION };
    private Map<String, String> mLayouts = new HashMap<String, String>();
    private Map<String, String> mTemperatureThresholds = new HashMap<String, String>();

    private static final String KEY_RESTART_SERVICE = "RESTART_SERVICE";

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);

        String[] layout_entries = getResources()
                .getStringArray(R.array.entries_layout);
        String[] layout_values = getResources().getStringArray(R.array.values_layout);
        for (int i = 0; i < layout_entries.length; i++) {
            mLayouts.put(layout_values[i], layout_entries[i]);
        }
        String[] threshold_entries = getResources()
                .getStringArray(R.array.entries_temperature_threshold);
        String[] threshold_values = getResources()
                .getStringArray(R.array.values_temperature_threshold);
        for (int i = 0; i < threshold_entries.length; i++) {
            mTemperatureThresholds.put(threshold_values[i], threshold_entries[i]);
        }
        for (String key : PREFERENCE_KEYS) {
            setPreferenceSummary(key);
        }
        RingtonePreference alert_sound_pref = (RingtonePreference)findPreference("key_alert_sound");
        alert_sound_pref.setOnPreferenceChangeListener(mPreferenceChangeListener);

        Preference position_pref = (Preference)findPreference("key_position");
        position_pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                Notification notification = new Notification();
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                intent.putExtra(KEY_RESTART_SERVICE, true);
                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                notification.icon = R.drawable.ic_stat_name;
                notification.tickerText = "edit mode";
                notification.setLatestEventInfo(getApplicationContext(), "Temperature Layer", "Touch if fixed layout", pi);

                notificationManager.notify(TemperatureLayerService.EDIT_MODE_NOTIFICATION_ID, notification);

                restartServiceIfRunning(true);

                return false;
            }
        });
        
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
        	if (intent.getExtras().getBoolean(KEY_RESTART_SERVICE)) {
                NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(TemperatureLayerService.EDIT_MODE_NOTIFICATION_ID);
                
        		restartServiceIfRunning();
        	}
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(
                        mSharedPreferenceChangeListener);
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(
                SharedPreferences sharedPreferences, String key) {
            setPreferenceSummary(sharedPreferences, key);
        }
    };

    /**
     * a listener for RingtonePreference
     */
    private OnPreferenceChangeListener mPreferenceChangeListener = new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String url = (String)newValue;
            if (!"".equals(url)) {
                Uri uri = Uri.parse(url);
                Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
                preference.setSummary(ringtone.getTitle(getApplicationContext()));
            }
            return true;
        }
    };

    private void setPreferenceSummary(String key) {
        setPreferenceSummary(
                PreferenceManager.getDefaultSharedPreferences(this), key);
    }

    private void setPreferenceSummary(SharedPreferences sharedPreferences,
            String key) {
        @SuppressWarnings("deprecation")
        Preference preference = findPreference(key);
        if (preference == null) {
            return;
        }

        TemperatureLayerConfig config = new TemperatureLayerConfig(this,
                sharedPreferences);
        if (key.equals(TemperatureLayerConfig.KEY_START_ON_BOOT)) {
            preference
                    .setSummary(config.isStartOnBoot() ? getString(R.string.start_on_boot_yes)
                            : getString(R.string.start_on_boot_no));
        } else if (key.equals(TemperatureLayerConfig.KEY_NOTIFICATION)) {
            preference
                    .setSummary(config.isNotify() ? getString(R.string.notification_yes)
                            : getString(R.string.notification_no));
        } else if (key.equals(TemperatureLayerConfig.KEY_COLOR)) {
            // nop
        } else if (key.equals(TemperatureLayerConfig.KEY_LAYOUT)) {
            preference.setSummary(mLayouts.get(Integer.toString(config
                    .getLayout())));
        } else if (key.equals(TemperatureLayerConfig.KEY_TEXT_SIZE)) {
            preference.setSummary(getString(R.string.size_unit,
                    config.getTextSize()));
        } else if (key.equals(TemperatureLayerConfig.KEY_TEMPERATURE_UNIT)) {
            preference.setSummary(getString(R.string.string_degree, "",
                    config.getTemperatureUnit()));
        } else if (key.equals(TemperatureLayerConfig.KEY_FONT)) {
            HashMap<String, String> fonts = FontManager.enumerateFonts();
            String fontName = fonts.get(config.getFontPath());
            preference.setSummary(fontName != null ? fontName : "");
        } else if (key.equals(TemperatureLayerConfig.KEY_ALERT)) {
            preference
                    .setSummary(config.isAlert() ? getString(R.string.alert_yes)
                            : getString(R.string.alert_no));
        } else if (key.equals(TemperatureLayerConfig.KEY_TEMPERATURE_THRESHOLD)) {
            preference.setSummary(mTemperatureThresholds.get(Integer.toString(config
                    .getTemperatureThreshold())));
        } else if (key.equals(TemperatureLayerConfig.KEY_SOUND)) {
            preference
                    .setSummary(config.withSound() ? getString(R.string.sound_yes)
                            : getString(R.string.sound_no));
        } else if (key.equals(TemperatureLayerConfig.KEY_ALERT_SOUND)) {
            Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(config.getAlertSound()));
            preference.setSummary(ringtone.getTitle(this));
        } else if (key.equals(TemperatureLayerConfig.KEY_VIBRATION)) {
            preference
                    .setSummary(config.isAlert() ? getString(R.string.vibration_yes)
                            : getString(R.string.vibration_no));
        }

        restartServiceIfRunning();
    }

    private void restartServiceIfRunning() {
        restartServiceIfRunning(false);
    }
    private void restartServiceIfRunning(boolean editMode) {
        Intent intent = new Intent(this, TemperatureLayerService.class);
        if (TemperatureLayerActivity.isServiceRunning(this)) {
            stopService(intent);
        }
        intent.putExtra(TemperatureLayerService.KEY_EDIT_MODE, editMode);
        startService(intent);
    }

    public void resetSetting() {
        TemperatureLayerConfig config = new TemperatureLayerConfig(this);
        config.reset();
    }
}
