package wallet.bitcoin.bitcoinwallet.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class AppPreferenceManager {

    private static AppPreferenceManager instance;

    private final SharedPreferences mPrefs;

    private AppPreferenceManager() {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(App.getContext());
    }

    public static synchronized AppPreferenceManager getInstance(){
        if (instance == null){
            instance = new AppPreferenceManager();
        }

        return instance;
    }

    public boolean getIsFirstLaunch(){
        return mPrefs.getBoolean("LAUNCH", true);
    }

    public void setNotFirstLaunch(){
        putBoolean("LAUNCH", false);
    }


    private Context getContext(){
        return App.getContext();
    }

    private void putBoolean(@NonNull String name, boolean value) {
        mPrefs.edit().putBoolean(name, value).apply();
    }

    private void putInt(@NonNull String name, int value) {
        mPrefs.edit().putInt(name, value).apply();
    }

    private void putLong(@NonNull String name, long value) {
        mPrefs.edit().putLong(name, value).apply();
    }

    private void putString(@NonNull String name, @Nullable String value) {
        mPrefs.edit().putString(name, value).apply();
    }
}
