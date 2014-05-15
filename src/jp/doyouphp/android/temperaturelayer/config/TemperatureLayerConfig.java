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

package jp.doyouphp.android.temperaturelayer.config;

import jp.doyouphp.android.temperaturelayer.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;

/**
 * Configuration class
 *
 * This class is an interface to access configuration in SharedPreferences.
 */
public final class TemperatureLayerConfig {
    public static final String KEY_START_ON_BOOT = "key_start_on_boot";
    public static final String KEY_NOTIFICATION = "key_notification";
    public static final String KEY_TEMPERATURE_UNIT = "key_temperature_unit";
    public static final String KEY_TEXT_SIZE = "key_text_size";
    public static final String KEY_COLOR = "key_color";
    public static final String KEY_FONT = "key_font";
    public static final String KEY_ALERT = "key_alert";
    public static final String KEY_SOUND = "key_sound";
    public static final String KEY_ALERT_SOUND = "key_alert_sound";
    public static final String KEY_VIBRATION = "key_vibration";
    public static final String KEY_TEMPERATURE_THRESHOLD = "key_temperature_threshold";

    public static final String KEY_X = "key_x";
    public static final String KEY_Y = "key_y";

    private static final String KEY_INITIALIZED = "key_initialized";

    private Context mContext;
    private SharedPreferences mSharedPreferences;

    public TemperatureLayerConfig(Context context) {
        this(context, PreferenceManager.getDefaultSharedPreferences(context));
    }

    public TemperatureLayerConfig(Context context,
            SharedPreferences sharedPreferences) {
        mContext = context;
        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        initialize();
    }

    private void initialize() {
        initialize(false);
    }

    private void initialize(boolean force) {
        if (force || !isInitialized()) {
            mSharedPreferences
                    .edit()
                    .putBoolean(KEY_START_ON_BOOT, false)
                    .putBoolean(KEY_NOTIFICATION, false)
                    .putString(
                            KEY_TEMPERATURE_UNIT,
                            mContext.getResources().getString(
                                    R.string.default_temperature_unit))
                    .putInt(KEY_TEXT_SIZE,
                            mContext.getResources().getInteger(
                                    R.integer.default_text_size))
                    .putInt(KEY_COLOR,
                            mContext.getResources().getColor(
                                    R.color.default_color))
                    .putString(KEY_ALERT_SOUND, Settings.System.DEFAULT_NOTIFICATION_URI.toString())
                    .putBoolean(KEY_INITIALIZED, true).commit();

            /**
             * @todo layout情報があったらX/Yの初期値を入れる？
             */
        }
    }

    private boolean isInitialized() {
        return mSharedPreferences.getBoolean(KEY_INITIALIZED, false) == true;
    }

    public boolean isStartOnBoot() {
        return mSharedPreferences.getBoolean(KEY_START_ON_BOOT, false);
    }

    public String getTemperatureUnit() {
        return mSharedPreferences.getString(KEY_TEMPERATURE_UNIT, mContext
                .getResources().getString(R.string.default_temperature_unit));
    }

    public int getTextSize() {
        return mSharedPreferences.getInt(KEY_TEXT_SIZE, mContext.getResources()
                .getInteger(R.integer.default_text_size));
    }

    public int getColor() {
        return mSharedPreferences.getInt(KEY_COLOR, mContext.getResources()
                .getInteger(R.color.default_color));
    }

    public void reset() {
        initialize(true);
    }

    public boolean isNotify() {
        return mSharedPreferences.getBoolean(KEY_NOTIFICATION, false);
    }

    public boolean isAlert() {
        return mSharedPreferences.getBoolean(KEY_ALERT, false);
    }

    public boolean withSound() {
        return mSharedPreferences.getBoolean(KEY_SOUND, false);
    }

    public boolean withVibration() {
        return mSharedPreferences.getBoolean(KEY_VIBRATION, false);
    }

    public String getFontPath() {
        return mSharedPreferences.getString(KEY_FONT, null);
    }

    public String getAlertSound() {
        return mSharedPreferences.getString(KEY_ALERT_SOUND, Settings.System.DEFAULT_NOTIFICATION_URI.toString());
    }

    public int getTemperatureThreshold() {
        return mSharedPreferences.getInt(KEY_TEMPERATURE_THRESHOLD, mContext.getResources()
                .getInteger(R.integer.default_temperature_threshold));
    }

    public boolean useCelsius() {
        final String celsiusUnit = mContext
                .getString(R.string.default_temperature_unit);
        final String currentUnit = getTemperatureUnit();
        return currentUnit.equals(celsiusUnit);
    }

    public Context getContext() {
        return mContext;
    }

    public void setX(int x) {
        mSharedPreferences.edit().putInt(KEY_X, x).commit();
    }

    public void setY(int y) {
        mSharedPreferences.edit().putInt(KEY_Y, y).commit();
    }

    public int getX() {
        return mSharedPreferences.getInt(KEY_X, mContext.getResources()
                .getInteger(R.integer.default_x));
    }

    public int getY() {
        return mSharedPreferences.getInt(KEY_Y, mContext.getResources()
                .getInteger(R.integer.default_y));
    }
}
