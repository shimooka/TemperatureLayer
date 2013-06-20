package jp.doyouphp.android.temperaturelayer.config;

import jp.doyouphp.android.temperaturelayer.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class TemperatureLayerConfig {
    public static final String KEY_START_ON_BOOT = "key_start_on_boot";
    public static final String KEY_TEMPERATURE_UNIT = "key_temperature_unit";
    public static final String KEY_LAYOUT = "key_layout";
    public static final String KEY_TEXT_SIZE = "key_text_size";
    public static final String KEY_COLOR = "key_color";
    private static final String KEY_INITIALIZED = "key_initialized";

    private Context mContext;
    private SharedPreferences mSharedPreferences;

    public TemperatureLayerConfig(Context context) {
        this(context, PreferenceManager.getDefaultSharedPreferences(context));
    }

    public TemperatureLayerConfig(Context context, SharedPreferences sharedPreferences) {
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        initialize();
    }

    private void initialize() {
        if (!isInitialized()) {
            mSharedPreferences.edit()
                .putBoolean(KEY_START_ON_BOOT, false)
                .putString(KEY_TEMPERATURE_UNIT, mContext.getResources().getString(R.string.default_temperature_unit))
                .putInt(KEY_LAYOUT, mContext.getResources().getInteger(R.integer.default_layout))
                .putInt(KEY_TEXT_SIZE, mContext.getResources().getInteger(R.integer.default_text_size))
                .putInt(KEY_COLOR, mContext.getResources().getColor(R.color.default_color))
                .putBoolean(KEY_INITIALIZED, true)
                .commit();
        }
    }

    private boolean isInitialized() {
        return mSharedPreferences.getBoolean(KEY_INITIALIZED, false) == true;
    }

    public boolean isStartOnBoot() {
        return mSharedPreferences.getBoolean(KEY_START_ON_BOOT, false);
    }

    public String getTemperatureUnit() {
        return mSharedPreferences.getString(KEY_TEMPERATURE_UNIT,
                mContext.getResources().getString(R.string.default_temperature_unit));
    }

    public int getTextSize() {
        return mSharedPreferences.getInt(KEY_TEXT_SIZE,
                mContext.getResources().getInteger(R.integer.default_text_size));
    }

    public int getLayout() {
        return mSharedPreferences.getInt(KEY_LAYOUT,
                mContext.getResources().getInteger(R.integer.default_layout));
    }

    public int getColor() {
        return mSharedPreferences.getInt(KEY_COLOR,
                mContext.getResources().getInteger(R.color.default_color));
    }
}
