package tripster.tripster.UILayer.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

import tripster.tripster.R;

import static tripster.tripster.Constants.USER_ABOUT_K;
import static tripster.tripster.UILayer.TripsterActivity.currentUserId;
import static tripster.tripster.UILayer.TripsterActivity.tDb;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

  private SharedPreferences sharedPreferences;

  @Override
  public void onCreatePreferences(Bundle bundle, String s) {
    // Add xml
    addPreferencesFromResource(R.xml.fragment_settings);

    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

    onSharedPreferenceChanged(sharedPreferences, "metersTracking");
    onSharedPreferenceChanged(sharedPreferences, "trackingTime");
    onSharedPreferenceChanged(sharedPreferences, "aboutPreference");
  }

  @Override
  public void onResume() {
    super.onResume();
    // Register the preferenceChange listener
    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    Preference preference = findPreference(key);
    if (preference instanceof ListPreference) {
      updateListPreference(sharedPreferences, key, preference);
    } else if (preference instanceof EditTextPreference) {
      updateEditPreference((EditTextPreference) preference);
      } else if (preference instanceof CheckBoxPreference) {
      CheckBoxPreference checkBox = (CheckBoxPreference) preference;
      checkBox.setChecked(checkBox.isChecked());
    }
  }

  private void updateEditPreference(EditTextPreference preference) {
    if (preference.getText() != null && preference.getText().trim().length() > 0) {
      String aboutText = preference.getText();

      Map<String, Object> properties = new HashMap<>();
      properties.put(USER_ABOUT_K, aboutText);
      tDb.upsertNewDocById(currentUserId, properties);

      preference.setSummary(aboutText);
    } else {
      preference.setSummary("Say something about yourself.");
    }
  }

  private void updateListPreference(SharedPreferences sharedPreferences, String key, Preference preference) {
    ListPreference listPreference = (ListPreference) preference;
    int prefIndex = listPreference.findIndexOfValue(sharedPreferences.getString(key, ""));
    if (prefIndex >= 0) {
      preference.setSummary(listPreference.getEntries()[prefIndex]);
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    // Unregister the preference change listener
    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
  }
}
