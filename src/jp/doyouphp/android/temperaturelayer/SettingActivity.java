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

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;

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
            TemperatureLayerConfig.KEY_RING,
            TemperatureLayerConfig.KEY_RINGTONE,
            TemperatureLayerConfig.KEY_VIBRATION };
    private Map<String, String> mLayouts = new HashMap<String, String>();

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);

        String[] entries = getResources()
                .getStringArray(R.array.entries_layout);
        String[] values = getResources().getStringArray(R.array.values_layout);
        for (int i = 0; i < entries.length; i++) {
            mLayouts.put(values[i], entries[i]);
        }

                
/*
        List<String> threshold_entries = new ArrayList<String>();
        List<String> threshold_values = new ArrayList<String>();
        TemperatureLayerConfig config = new TemperatureLayerConfig(this);
        for (int i = getResources().getInteger(R.integer.min_temperature_threshold);
        		i <= getResources().getInteger(R.integer.max_temperature_threshold); i++) {
            threshold_entries.add(TemperatureLayerService.getTemperatureAsString(config, i * 10));
            threshold_values.add(Integer.toString(i));
        }
//        IntegerListPreference threshold = (IntegerListPreference)findPreference("key_temperature_threshold");
//        threshold.setEntries((String[])threshold_entries.toArray(new String[0]));
//        threshold.setEntryValues((String[])threshold_values.toArray(new String[0]));
*/
        for (String key : PREFERENCE_KEYS) {
            setPreferenceSummary(key);
        }
        RingtonePreference pref = (RingtonePreference)findPreference("key_ringtone");
        pref.setOnPreferenceChangeListener(mPreferenceChangeListener);
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
        } else if (key.equals(TemperatureLayerConfig.KEY_RING)) {
            preference
                    .setSummary(config.withRing() ? getString(R.string.ring_yes)
                            : getString(R.string.ring_no));
        } else if (key.equals(TemperatureLayerConfig.KEY_RINGTONE)) {
            if (config.getRingtone() != null) {
                Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(config.getRingtone()));
                preference.setSummary(ringtone.getTitle(this));
            }
        } else if (key.equals(TemperatureLayerConfig.KEY_VIBRATION)) {
            preference
                    .setSummary(config.isAlert() ? getString(R.string.vibration_yes)
                            : getString(R.string.vibration_no));
        }

        restartServiceIfRunning();
    }

    private void restartServiceIfRunning() {
        if (TemperatureLayerActivity.isServiceRunning(this)) {
            stopService(new Intent(this, TemperatureLayerService.class));
            startService(new Intent(this, TemperatureLayerService.class));
        }
    }

    public void resetSetting() {
        TemperatureLayerConfig config = new TemperatureLayerConfig(this);
        config.reset();
    }
}
