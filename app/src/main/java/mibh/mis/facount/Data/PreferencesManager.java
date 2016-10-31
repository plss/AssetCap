package mibh.mis.facount.Data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ponlakiss on 07/15/2015.
 */

public class PreferencesManager {

    private static final String PREF_NAME = "ASSET";

    private static PreferencesManager sInstance;
    private final SharedPreferences mPref;

    public static final String EMP_ID = "EMP_ID";
    public static final String ID_CARD = "ID_CARD";
    public static final String FNAME = "FNAME";
    public static final String LNAME = "LNAME";
    public static final String TEL = "TEL";
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGTITUDE = "LONGTITUDE";
    public static final String LOCATIONNAME = "LOCATIONNAME";
    public static final String COMMENT = "COMMENT";
    public static final String VERSION = "VERSION";
    public static final String CHANGEDB = "CHANGEDB";


    private PreferencesManager(Context context) {
        mPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized void initializeInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PreferencesManager(context);
        }
    }

    public static synchronized PreferencesManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException(PreferencesManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return sInstance;
    }

    public void setValue(String KEY, String value) {
        mPref.edit().putString(KEY, value).apply();
    }

    public void setBoolValue(String KEY, Boolean value) {
        mPref.edit().putBoolean(KEY, value).apply();
    }

    public String getValueZ(String KEY) {
        return mPref.getString(KEY, "0");
    }

    public String getValue(String KEY) {
        return mPref.getString(KEY, "");
    }

    public Boolean getBoolValue(String KEY) {
        return mPref.getBoolean(KEY, true);
    }

    public void remove(String key) {
        mPref.edit().remove(key).apply();
    }

    public boolean clear() {
        return mPref.edit().clear().commit();
    }

}
