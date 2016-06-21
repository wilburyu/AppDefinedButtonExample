
package appdefinedbuttonexample.odg.com.appdefinedbuttonexample.videoplayer;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.util.Log;

import appdefinedbuttonexample.odg.com.appdefinedbuttonexample.R;
import appdefinedbuttonexample.odg.com.appdefinedbuttonexample.TheApp;

public class Preferences extends PreferenceActivity {
    private static final String TAG = "Preferences";

    public enum Preference {
        PREF_VIDEO(TheApp.getInstance().getString(R.string.pref_default_video)), //
        PREF_LOOP(TheApp.getInstance().getString(R.string.pref_default_loop)); //
        private final String dflt;

        String getDefault() {
            return dflt;
        }

        Preference(String dflt) {
            this.dflt = dflt;
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        SettingsFragment settings_fragment = new SettingsFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, settings_fragment)
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements
            OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onStart() {
            // Set up a listener whenever a key changes
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(
                    this);
            updateSummaries(getPreferenceScreen());
            super.onStart();
        }

        @Override
        public void onStop() {
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
            super.onStop();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            android.preference.Preference pref = findPreference(key);
            setSummary(pref);
        }

        private void updateSummaries(PreferenceGroup pg) {
            for (int i = 0; i < pg.getPreferenceCount(); ++i) {
                android.preference.Preference p = pg.getPreference(i);
                if (p instanceof PreferenceGroup)
                    updateSummaries((PreferenceGroup) p); // recursion
                else
                    setSummary(p);
            }
        }

        private void setSummary(android.preference.Preference pref) {
            if (pref.getKey().equals(Preference.PREF_VIDEO.toString())) {
                String format = getString(R.string.pref_summary_video);
                EditTextPreference editTextPref = (EditTextPreference) pref;
                String str = String.format(format, editTextPref.getText());
                pref.setSummary(str);
            }
            if (pref.getKey().equals(Preference.PREF_LOOP.toString())) {
                String format = getString(R.string.pref_summary_loop);
                CheckBoxPreference checkboxPref = (CheckBoxPreference) pref;
                String value = checkboxPref.isChecked() ? getString(R.string.on)
                        : getString(R.string.off);
                String str = String.format(format, value);
                pref.setSummary(str);
            }
        }

    }

    public static String getStringValue(Preference preference) {
        if (preference == null)
            return null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(TheApp
                .getInstance());
        return prefs.getString(preference.toString(), preference.getDefault());
    }

    public static void setStringValue(Preference preference, String value) {
        Log.d(TAG, "setStringValue -  " + preference.toString() + ": " + value);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(TheApp
                .getInstance());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(preference.toString(), value);
        editor.commit();
    }

    public static int getIntValue(Preference preference) {
        int value = 0;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(TheApp
                .getInstance());
        try {
            String string = prefs.getString(preference.toString(), preference.getDefault());
            value = Integer.parseInt(string);
        } catch (Exception e) {
            try {
                value = prefs.getInt(preference.toString(),
                        Integer.parseInt(preference.getDefault()));
            } catch (NumberFormatException e1) {
                Log.d(TAG, "getIntValue = " + e.getMessage());
            }
        }
        return value;
    }

    public static void setIntValue(Preference preference, int value) {
        Log.d(TAG, "setIntValue -  " + preference.toString() + ": " + value);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(TheApp
                .getInstance());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(preference.toString(), Integer.toString(value));
        editor.commit();
    }

    public static Boolean getBooleanValue(Preference preference) {
        if (preference == null)
            return null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(TheApp
                .getInstance());
        return prefs.getBoolean(preference.toString(),
                Boolean.parseBoolean(preference.getDefault()));
    }

    public static void setBooleanValue(Preference preference, boolean value) {
        Log.d(TAG, "setBooleanValue -  " + preference.toString() + ": " + value);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(TheApp
                .getInstance());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(preference.toString(), value);
        editor.commit();
    }

}
