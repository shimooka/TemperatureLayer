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

import jp.doyouphp.android.temperaturelayer.service.TemperatureLayerService;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * an Activity class for setting screen
 */
public class SettingActivity extends PreferenceActivity {
    private static final String[] PREFERENCE_KEYS = {
    	"key_start_on_boot",
    	"key_temperature_unit",
    	"key_layout",
    	"key_text_size",
    	"key_color"
    };
    private Map<String, String> mLayouts = new HashMap<String, String>();

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);

        String[] entries = getResources().getStringArray(R.array.entries_layout);
        String[] values = getResources().getStringArray(R.array.values_layout);
        for (int i = 0; i < entries.length; i++) {
            mLayouts.put(values[i], entries[i]);
        }

        for (String key: PREFERENCE_KEYS) {
            setPreferenceSummary(key);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
        		.registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
        		.unregisterOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
    }

    /**
     * @see http://y-anz-m.blogspot.jp/2010/07/androidpreference-summary.html
     */
    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener =
        new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                setPreferenceSummary(sharedPreferences, key);
            }
        };

    private void setPreferenceSummary(String key) {
        setPreferenceSummary(PreferenceManager.getDefaultSharedPreferences(this), key);
    }
    private void setPreferenceSummary(SharedPreferences sharedPreferences, String key) {
        @SuppressWarnings("deprecation")
        Preference preference = findPreference(key);
        if (preference == null) {
            return;
        }
        if (key.equals("key_start_on_boot")) {
            preference.setSummary(
            		sharedPreferences.getBoolean(key, false) ?
            		getString(R.string.start_on_boot_yes) :
            		getString(R.string.start_on_boot_no));
        } else if (key.equals("key_color")) {
            // nop
        } else if (key.equals("key_layout")) {
            preference.setSummary(mLayouts.get(Integer.toString(sharedPreferences.getInt(key, getResources().getInteger(R.integer.default_layout)))));
        } else if (key.equals("key_text_size")){
            preference.setSummary(getString(R.string.size_unit, sharedPreferences.getInt(key, getResources().getInteger(R.integer.default_text_size))));
        } else if (key.equals("key_temperature_unit")){
            preference.setSummary(getString(R.string.string_degree, "", sharedPreferences.getString(key, getResources().getString(R.string.default_temperature_unit))));
        }

        restartServiceIfRunning();
    }

    private void restartServiceIfRunning() {
        if (TemperatureLayerActivity.isServiceRunning(this)) {
            stopService(new Intent(this, TemperatureLayerService.class));
            startService(new Intent(this, TemperatureLayerService.class));
        }
    }
}
