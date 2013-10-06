package de.robv.android.xposed;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.android.internal.util.XmlUtils;

import android.content.SharedPreferences;
import android.os.FileUtils;
import android.os.FileUtils.FileStatus;
import android.util.Log;

public class XSharedPreferencesGB implements SharedPreferences {

	private static final String TAG = "ReadOnlySharedPreferences";

	private final File mFile;
	private Map<String, Object> mMap; // guarded by 'this'
	private int mDiskWritesInFlight = 0; // guarded by 'this'
	private boolean mLoaded = false; // guarded by 'this'
	private long mStatTimestamp; // guarded by 'this'
	private long mStatSize; // guarded by 'this'

	@SuppressWarnings({ "unchecked", "rawtypes" })
	XSharedPreferencesGB(File file, int mode, Map initialContents) {
		mFile = file;
		makeBackupFile(file);
		mLoaded = initialContents != null;
		mMap = initialContents != null ? initialContents : new HashMap<String, Object>();
		FileStatus stat = new FileStatus();
		if (FileUtils.getFileStatus(file.getPath(), stat)) {
			mStatTimestamp = stat.mtime;
		}
		reload();
	}

	// Has this SharedPreferences ever had values assigned to it?
	boolean isLoaded() {
		synchronized (this) {
			return mLoaded;
		}
	}

	// Has the file changed out from under us? i.e. writes that
	// we didn't instigate.
	public boolean hasFileChangedUnexpectedly() {
		synchronized (this) {
			if (mDiskWritesInFlight > 0) {
				// // If we know we caused it, it's not unexpected.
				// if (DEBUG) Log.d(TAG, "disk write in flight, not unexpected.");
				return false;
			}
		}
		FileStatus stat = new FileStatus();
		if (!FileUtils.getFileStatus(mFile.getPath(), stat)) {
			return true;
		}
		synchronized (this) {
			return mStatTimestamp != stat.mtime || mStatSize != stat.size;
		}
	}

	/* package */@SuppressWarnings({ "rawtypes", "unchecked" })
	void replace(Map newContents, FileStatus stat) {
		synchronized (this) {
			mLoaded = true;
			if (newContents != null) {
				mMap = newContents;
			}
			if (stat != null) {
				mStatTimestamp = stat.mtime;
				mStatSize = stat.size;
			}
		}
	}

	public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		throw new UnsupportedOperationException("listeners are not supported in this implementation");
	}

	public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		throw new UnsupportedOperationException("listeners are not supported in this implementation");
	}

	public Map<String, ?> getAll() {
		synchronized (this) {
			// noinspection unchecked
			return new HashMap<String, Object>(mMap);
		}
	}

	public String getString(String key, String defValue) {
		synchronized (this) {
			String v = (String) mMap.get(key);
			return v != null ? v : defValue;
		}
	}

	public int getInt(String key, int defValue) {
		synchronized (this) {
			Integer v = (Integer) mMap.get(key);
			return v != null ? v : defValue;
		}
	}

	public long getLong(String key, long defValue) {
		synchronized (this) {
			Long v = (Long) mMap.get(key);
			return v != null ? v : defValue;
		}
	}

	public float getFloat(String key, float defValue) {
		synchronized (this) {
			Float v = (Float) mMap.get(key);
			return v != null ? v : defValue;
		}
	}

	public boolean getBoolean(String key, boolean defValue) {
		synchronized (this) {
			Boolean v = (Boolean) mMap.get(key);
			return v != null ? v : defValue;
		}
	}

	public boolean contains(String key) {
		synchronized (this) {
			return mMap.containsKey(key);
		}
	}

	public Editor edit() {
		throw new UnsupportedOperationException("read-only implementation");
	}

	@Override
	public Set<String> getStringSet(String key, Set<String> defValue) {
		synchronized (this) {
			@SuppressWarnings("unchecked")
            Set<String> v = (Set<String>) mMap.get(key);
			return v != null ? v : defValue;
		}
	}

	private static File makeBackupFile(File prefsFile) {
		return new File(prefsFile.getPath() + ".bak");
	}

	public void reload() {
		synchronized (this) {
			File prefsFile = mFile;

			File backup = makeBackupFile(prefsFile);
			if (backup.exists()) {
				prefsFile.delete();
				backup.renameTo(prefsFile);
			}

			@SuppressWarnings("rawtypes")
			Map map = null;
			FileStatus stat = new FileStatus();
			if (FileUtils.getFileStatus(prefsFile.getPath(), stat) && prefsFile.canRead()) {
				try {
					FileInputStream str = new FileInputStream(prefsFile);
					map = XmlUtils.readMapXml(str);
					str.close();
				} catch (org.xmlpull.v1.XmlPullParserException e) {
					Log.w(TAG, "getSharedPreferences", e);
				} catch (FileNotFoundException e) {
					Log.w(TAG, "getSharedPreferences", e);
				} catch (IOException e) {
					Log.w(TAG, "getSharedPreferences", e);
				}
			}
			replace(map, stat);
		}
	}

	public boolean makeWorldReadable() {
		if (!mFile.exists()) // just in case - the file should never be created if it doesn'e exist
			return false;
		return mFile.setReadable(true, false);
    }
}
