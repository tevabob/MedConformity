package net.innit.drugbug.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import net.innit.drugbug.R;
import net.innit.drugbug.data.Settings;
import net.innit.drugbug.data.SettingsHelper;
import net.innit.drugbug.util.ImageStorage;

import java.util.Collection;
import java.util.Map;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private int oldNumDoses;                // Holder field for initial number of doses to keep
    private ImageStorage imageStorage;      // Image storage object
    private SettingsHelper settingsHelper;  // Settings constants and methods
    private Settings settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsHelper = new SettingsHelper(getActivity());

        settings = Settings.getInstance(getActivity());
        settings.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        oldNumDoses = Integer.parseInt(settings.getString(Settings.Key.NUM_DOSES));

        imageStorage = ImageStorage.getInstance(getActivity());

        ListPreference listPreference = (ListPreference) findPreference(Settings.Key.IMAGE_STORAGE.name());
        Map<String, String> locations = imageStorage.getAvailableLocations();
        Collection<String> values = locations.values();
        listPreference.setEntries(values.toArray(new String[values.size()]));
        values = locations.keySet();
        listPreference.setEntryValues(values.toArray(new String[values.size()]));

        imageStorage.setLocationType(settings.getString(Settings.Key.IMAGE_STORAGE));

        setSummaries(settings);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String keyString) {
        if (isAdded()) {
            // Since we're only using the default shared preferences here, we'll ignore the SharedPreferences
            // parameter and just use the Settings instance we created in onCreate
            Settings.Key key = Settings.Key.valueOf(keyString);
            switch (key) {
                case NUM_DOSES:
                    settingsHelper.numDosesChanged(getActivity(), Integer.parseInt(settings.getString(key)), oldNumDoses);
                    break;
                case KEEP_TIME_TAKEN:
                    settingsHelper.keepTimeTakenChanged(settings.getString(key));
                    break;
                case KEEP_TIME_MISSED:
                    settingsHelper.keepTimeMissedChanged(settings.getString(key));
                    break;
                case IMAGE_STORAGE:
                    imageStorage.setLocationType(settings.getString(key));
                    settingsHelper.imageStorageChanged(imageStorage);
                    break;
            }
            setSummaries(settings);
        }
    }

    /**
     *  Set the summaries for all preferences
     *
     * @param sharedPreferences Shared preferences object containing the preferences to set summaries for
     */
    private void setSummaries(Settings sharedPreferences) {
        // Set summary for number of untaken doses to keep
        findPreference(Settings.Key.NUM_DOSES.name())
                .setSummary(getString(R.string.preference_current) + sharedPreferences.getString(Settings.Key.NUM_DOSES));

        // Set summary for taken dose keep time
        findPreference(Settings.Key.KEEP_TIME_TAKEN.name())
                .setSummary(getString(R.string.preference_current) + SettingsHelper.convertString(getActivity(), sharedPreferences.getString(Settings.Key.KEEP_TIME_TAKEN)));

        // Set summary for untaken dose keep time
        findPreference(Settings.Key.KEEP_TIME_MISSED.name())
                .setSummary(getString(R.string.preference_current) + SettingsHelper.convertString(getActivity(), sharedPreferences.getString(Settings.Key.KEEP_TIME_MISSED)));

        // Set summary for image storage location
        findPreference(Settings.Key.IMAGE_STORAGE.name())
                .setSummary(getString(R.string.preference_current) + imageStorage.getDisplayText());
    }
}
