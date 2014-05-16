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

package jp.doyouphp.android.temperaturelayer;

import java.util.HashMap;
import java.util.Map;

import com.ulduzsoft.font.FontManager;

import jp.doyouphp.android.temperaturelayer.config.TemperatureLayerConfig;
import jp.doyouphp.android.temperaturelayer.service.TemperatureLayerService;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.widget.Toast;

/**
 * an Activity class for setting screen
 */
public class SettingActivity extends PreferenceActivity {
    private static final String[] PREFERENCE_KEYS = {
            TemperatureLayerConfig.KEY_START_ON_BOOT,
            TemperatureLayerConfig.KEY_NOTIFICATION,
            TemperatureLayerConfig.KEY_HIDE_ICON,
            TemperatureLayerConfig.KEY_TEMPERATURE_UNIT,
            TemperatureLayerConfig.KEY_TEXT_SIZE,
            TemperatureLayerConfig.KEY_FONT,
            TemperatureLayerConfig.KEY_COLOR,
            TemperatureLayerConfig.KEY_ALERT,
            TemperatureLayerConfig.KEY_TEMPERATURE_THRESHOLD,
            TemperatureLayerConfig.KEY_SOUND,
            TemperatureLayerConfig.KEY_ALERT_SOUND,
            TemperatureLayerConfig.KEY_VIBRATION };

    public static final String KEY_RESTART_SERVICE = "RESTART_SERVICE";

    private Map<String, String> mTemperatureThresholds = new HashMap<String, String>();

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildPreferenceFromResource();

        String[] threshold_entries = getResources()
                .getStringArray(R.array.entries_temperature_threshold);
        String[] threshold_values = getResources()
                .getStringArray(R.array.values_temperature_threshold);
        for (int i = 0; i < threshold_entries.length; i++) {
            mTemperatureThresholds.put(threshold_values[i], threshold_entries[i]);
        }
        for (String key : PREFERENCE_KEYS) {
            setupPreferenceSummary(key);
        }
        RingtonePreference alert_sound_pref = (RingtonePreference)findPreference("key_alert_sound");
        alert_sound_pref.setOnPreferenceChangeListener(mPreferenceChangeListener);

        Preference position_pref = (Preference)findPreference("key_position");
        position_pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                restartServiceIfRunning(true, true);
                Toast.makeText(getApplicationContext(), R.string.enter_edit_mode, Toast.LENGTH_LONG).show();
                return true;
            }
        });
    }

    @SuppressWarnings("deprecation")
    protected void buildPreferenceFromResource() {
        addPreferencesFromResource(R.xml.pref_set_on_boot);
        addPreferencesFromResource(R.xml.pref_display);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            addPreferencesFromResource(R.xml.pref_notification_icon);
        } else {
            addPreferencesFromResource(R.xml.pref_notification);
        }
        addPreferencesFromResource(R.xml.pref_alert);
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

    private void setupPreferenceSummary(String key) {
        setPreferenceSummary(
                PreferenceManager.getDefaultSharedPreferences(this), key, false);
    }

    private void setPreferenceSummary(SharedPreferences sharedPreferences,
            String key) {
        setPreferenceSummary(sharedPreferences, key, true);
    }

    private void setPreferenceSummary(SharedPreferences sharedPreferences,
            String key, boolean requireRestart) {
        @SuppressWarnings("deprecation")
        Preference preference = findPreference(key);
        if (preference == null) {
            return;
        }

        TemperatureLayerConfig config = new TemperatureLayerConfig(this,
                sharedPreferences);
        String summary = null;
        if (key.equals(TemperatureLayerConfig.KEY_START_ON_BOOT)) {
            summary = config.isStartOnBoot() ? getString(R.string.start_on_boot_yes)
                            : getString(R.string.start_on_boot_no);
            requireRestart = false;
        } else if (key.equals(TemperatureLayerConfig.KEY_NOTIFICATION)) {
            summary = config.isNotify() ? getString(R.string.notification_yes)
                            : getString(R.string.notification_no);
        } else if (key.equals(TemperatureLayerConfig.KEY_HIDE_ICON)) {
            summary = config.withIcon() ? getString(R.string.hide_icon_no)
                            : getString(R.string.hide_icon_yes);
        } else if (key.equals(TemperatureLayerConfig.KEY_TEXT_SIZE)) {
            summary = getString(R.string.size_unit, config.getTextSize());
        } else if (key.equals(TemperatureLayerConfig.KEY_TEMPERATURE_UNIT)) {
            summary = getString(R.string.string_degree, "", config.getTemperatureUnit());
        } else if (key.equals(TemperatureLayerConfig.KEY_FONT)) {
            HashMap<String, String> fonts = FontManager.enumerateFonts();
            String fontName = fonts.get(config.getFontPath());
            summary = fontName != null ? fontName : "";
        } else if (key.equals(TemperatureLayerConfig.KEY_ALERT)) {
            summary = config.isAlert() ? getString(R.string.alert_yes)
                            : getString(R.string.alert_no);
            requireRestart = false;
        } else if (key.equals(TemperatureLayerConfig.KEY_TEMPERATURE_THRESHOLD)) {
            summary = mTemperatureThresholds.get(Integer.toString(config.getTemperatureThreshold()));
            requireRestart = false;
        } else if (key.equals(TemperatureLayerConfig.KEY_SOUND)) {
            summary = config.withSound() ? getString(R.string.sound_yes)
                            : getString(R.string.sound_no);
            requireRestart = false;
        } else if (key.equals(TemperatureLayerConfig.KEY_ALERT_SOUND)) {
            Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(config.getAlertSound()));
            summary = ringtone.getTitle(this);
            requireRestart = false;
        } else if (key.equals(TemperatureLayerConfig.KEY_VIBRATION)) {
            summary = config.isAlert() ? getString(R.string.vibration_yes)
                            : getString(R.string.vibration_no);
            requireRestart = false;
        }

        if (summary != null) {
            preference.setSummary(summary);
        }

        if (requireRestart) {
            restartServiceIfRunning();
        }
    }

    private void restartServiceIfRunning() {
        restartServiceIfRunning(false, false);
    }
    private void restartServiceIfRunning(boolean editMode, boolean forceStart) {
        Intent intent = new Intent(this, TemperatureLayerService.class);
        boolean isRunning = TemperatureLayerActivity.isServiceRunning(this);
        if (isRunning) {
            stopService(intent);
        }
        if (isRunning || forceStart) {
            intent.putExtra(TemperatureLayerService.KEY_EDIT_MODE, editMode);
            startService(intent);
        }
    }

    public void resetSetting() {
        TemperatureLayerConfig config = new TemperatureLayerConfig(this);
        config.reset();
    }
}
