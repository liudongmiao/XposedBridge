package de.robv.android.xposed;

import java.io.File;
import java.util.Map;
import java.util.Set;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;

public class XSharedPreferences implements SharedPreferences {

	SharedPreferences mSharedPreferences;

	public XSharedPreferences(File file) {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
			mSharedPreferences = new XSharedPreferencesHC(file);
		} else {
			mSharedPreferences = new XSharedPreferencesGB(file, 0, null);
		}
	}

	public boolean makeWorldReadable() {
		if (mSharedPreferences instanceof XSharedPreferencesGB) {
			return ((XSharedPreferencesGB) mSharedPreferences).makeWorldReadable();
		} else if (mSharedPreferences instanceof XSharedPreferencesHC) {
			return ((XSharedPreferencesHC) mSharedPreferences).makeWorldReadable();
		} else {
			return false;
		}
	}

	public XSharedPreferences(String packageName, String prefFileName) {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
			mSharedPreferences = new XSharedPreferencesHC(packageName, prefFileName);
		} else {
			File mFile = new File(Environment.getDataDirectory(), "data/" + packageName + "/shared_prefs/" + prefFileName + ".xml");
			mSharedPreferences = new XSharedPreferencesGB(mFile, 0, null);
		}
	}

	public XSharedPreferences(String packageName) {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
			mSharedPreferences = new XSharedPreferencesHC(packageName);
		} else {
			File mFile = new File(Environment.getDataDirectory(), "data/" + packageName + "/shared_prefs/" + packageName + "_preferences.xml");
			mSharedPreferences = new XSharedPreferencesGB(mFile, 0, null);
		}
	}

	@Override
	public boolean contains(String key) {
		return mSharedPreferences.contains(key);
	}

	@Override
	public Editor edit() {
		return mSharedPreferences.edit();
	}

	@Override
	public Map<String, ?> getAll() {
		return mSharedPreferences.getAll();
	}

	@Override
	public boolean getBoolean(String key, boolean defValue) {
		return mSharedPreferences.getBoolean(key, defValue);
	}

	@Override
	public float getFloat(String key, float defValue) {
		return mSharedPreferences.getFloat(key, defValue);
	}

	@Override
	public int getInt(String key, int defValue) {
		return mSharedPreferences.getInt(key, defValue);
	}

	@Override
	public long getLong(String key, long defValue) {
		return mSharedPreferences.getLong(key, defValue);
	}

	@Override
	public String getString(String key, String defValue) {
		return mSharedPreferences.getString(key, defValue);
	}

	@Override
	public Set<String> getStringSet(String key, Set<String> defValue) {
		if (mSharedPreferences instanceof XSharedPreferencesGB) {
			return ((XSharedPreferencesGB) mSharedPreferences).getStringSet(key, defValue);
		} else if (mSharedPreferences instanceof XSharedPreferencesHC) {
			return ((XSharedPreferencesHC) mSharedPreferences).getStringSet(key, defValue);
		} else {
			return defValue;
		}
	}

	@Override
	public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		mSharedPreferences.registerOnSharedPreferenceChangeListener(listener);
	}

	@Override
	public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
	}

	public void reload() {
		if (mSharedPreferences instanceof XSharedPreferencesGB) {
			((XSharedPreferencesGB) mSharedPreferences).reload();
		} else if (mSharedPreferences instanceof XSharedPreferencesHC) {
			((XSharedPreferencesHC) mSharedPreferences).reload();
		}
	}

}