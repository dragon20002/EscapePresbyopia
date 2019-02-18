package kr.or.hanium.chungbukhansung.escapepresbyopia.activity;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import kr.or.hanium.chungbukhansung.escapepresbyopia.R;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        bindPreferenceSummaryToValue(findPreference("voiceId"));
    }

    private static void bindPreferenceSummaryToValue(Preference pref) {
        pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                String stringValue = value.toString();

                if (preference instanceof ListPreference) {
                    ListPreference listPref = (ListPreference) preference;
                    int index = listPref.findIndexOfValue(stringValue);
                    preference.setSummary(index >= 0 ? listPref.getEntries()[index] : null);
                } else {
                    preference.setSummary(stringValue);
                }
                return true;
            }
        });
        pref.getOnPreferenceChangeListener().onPreferenceChange(pref,
                PreferenceManager
                        .getDefaultSharedPreferences(pref.getContext())
                        .getString(pref.getKey(), null));
    }

}
