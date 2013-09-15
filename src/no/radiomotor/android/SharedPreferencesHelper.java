package no.radiomotor.android;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class SharedPreferencesHelper {

	public static Context ctx;
	private static final String PREFS_FILE = "radiomotorPrefs";

	private SharedPreferencesHelper(Context context) {
		ctx = context;
	}

	public static SharedPreferencesHelper get(Context context) {
		return new SharedPreferencesHelper(context);
	}

	public void putBoolean(String key, boolean value) {
		SharedPreferences settings = ctx.getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public boolean getBoolean(String key, boolean defValue) {
		SharedPreferences settings = ctx.getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
		return settings.getBoolean(key, defValue);
	}
}
