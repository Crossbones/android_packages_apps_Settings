/*
 * Copyright (C) 2011 The CyanogenMod Project
 * Copyright (C) 2012 Crossbones Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import com.android.settings.R;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

//
// CPU Related Settings
//
public class CPUSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String KEY_GOVERNOR = "cpu_governor";
    public static final String KEY_MIN_FREQ = "cpu_freq_min";
    public static final String KEY_MAX_FREQ = "cpu_freq_max";
    public static final String KEY_SCHEDULER = "cpu_scheduler";

    public static final String GOVERNORS_LIST_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    public static final String GOVERNOR_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static final String FREQ_LIST_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static final String FREQ_MIN_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    public static final String FREQ_MAX_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    public static final String SCHEDULER_FILE = "/sys/block/mmcblk0/queue/scheduler";

    private String mGovernorFormat;
    private String mMinFrequencyFormat;
    private String mMaxFrequencyFormat;
    private String mSchedulerFormat;

    private ListPreference mGovernorPref;
    private ListPreference mMinFrequencyPref;
    private ListPreference mMaxFrequencyPref;
    private ListPreference mSchedulerPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGovernorFormat = getString(R.string.cpu_governors_summary);
        mMinFrequencyFormat = getString(R.string.cpu_min_freq_summary);
        mMaxFrequencyFormat = getString(R.string.cpu_max_freq_summary);
        mSchedulerFormat = getString(R.string.cpu_schedulers_summary);

        String temp;

        addPreferencesFromResource(R.xml.cpu_settings);

        PreferenceScreen PrefScreen = getPreferenceScreen();

        mGovernorPref = (ListPreference) PrefScreen.findPreference(KEY_GOVERNOR);
        mMinFrequencyPref = (ListPreference) PrefScreen.findPreference(KEY_MIN_FREQ);
        mMaxFrequencyPref = (ListPreference) PrefScreen.findPreference(KEY_MAX_FREQ);
        mSchedulerPref = (ListPreference) PrefScreen.findPreference(KEY_SCHEDULER);

        if (!KernelUtils.fileExists(GOVERNOR_FILE)) {
            mGovernorPref.setEnabled(false);
            mGovernorPref.setSummary(R.string.feature_not_supported);
        } else {
            temp = KernelUtils.readOneLine(GOVERNOR_FILE);
            String governorList = KernelUtils.readOneLine(GOVERNORS_LIST_FILE);
            String[] availableGovernors = governorList.split(" ");
            mGovernorPref.setEntryValues(availableGovernors);
            mGovernorPref.setEntries(availableGovernors);
            mGovernorPref.setValue(temp);
            mGovernorPref.setSummary(String.format(mGovernorFormat, temp));
            mGovernorPref.setOnPreferenceChangeListener(this);
        }

        if (!KernelUtils.fileExists(FREQ_LIST_FILE)) {
            mMinFrequencyPref.setEnabled(false);
            mMaxFrequencyPref.setEnabled(false);
            mMinFrequencyPref.setSummary(R.string.feature_not_supported);
            mMaxFrequencyPref.setSummary(R.string.feature_not_supported);
        } else {
            String tempFreq = KernelUtils.readOneLine(FREQ_LIST_FILE);
            String[] availableFrequencies = tempFreq.split(" ");
            String[] frequencies = new String[availableFrequencies.length];
            for (int i = 0; i < frequencies.length; i++) {
                frequencies[i] = toMHz(availableFrequencies[i]);
            }

            if (KernelUtils.fileExists(FREQ_MIN_FILE)) {
                temp = KernelUtils.readOneLine(FREQ_MIN_FILE);
                mMinFrequencyPref.setEntryValues(availableFrequencies);
                mMinFrequencyPref.setEntries(frequencies);
                mMinFrequencyPref.setValue(temp);
                mMinFrequencyPref.setSummary(String.format(mMinFrequencyFormat, toMHz(temp)));
                mMinFrequencyPref.setOnPreferenceChangeListener(this);
            }

            if (KernelUtils.fileExists(FREQ_MAX_FILE)) {
                temp = KernelUtils.readOneLine(FREQ_MAX_FILE);
                mMaxFrequencyPref.setEntryValues(availableFrequencies);
                mMaxFrequencyPref.setEntries(frequencies);
                mMaxFrequencyPref.setValue(temp);
                mMaxFrequencyPref.setSummary(String.format(mMaxFrequencyFormat, toMHz(temp)));
                mMaxFrequencyPref.setOnPreferenceChangeListener(this);
            }
        }

        if (!KernelUtils.fileExists(SCHEDULER_FILE)) {
            mSchedulerPref.setEnabled(false);
            mSchedulerPref.setSummary(R.string.feature_not_supported);
        } else {
            temp = KernelUtils.readOneLine(SCHEDULER_FILE);
            String[] availableSchedulers = temp.split(" ");
            String schedulerCurrent = getCurrentScheduler(availableSchedulers);
            mSchedulerPref.setEntryValues(availableSchedulers);
            mSchedulerPref.setEntries(availableSchedulers);
            mSchedulerPref.setValue(schedulerCurrent);
            mSchedulerPref.setSummary(String.format(mSchedulerFormat, schedulerCurrent));
            mSchedulerPref.setOnPreferenceChangeListener(this);
        }

    }

    @Override
    public void onResume() {
        String temp;

        super.onResume();

        if (KernelUtils.fileExists(GOVERNOR_FILE)) {
            temp = KernelUtils.readOneLine(GOVERNOR_FILE);
            mGovernorPref.setSummary(String.format(mGovernorFormat, temp));
        }

        if (KernelUtils.fileExists(FREQ_LIST_FILE)) {
            if (KernelUtils.fileExists(FREQ_MIN_FILE)) {
                temp = KernelUtils.readOneLine(FREQ_MIN_FILE);
                mMinFrequencyPref.setValue(temp);
                mMinFrequencyPref.setSummary(String.format(mMinFrequencyFormat, toMHz(temp)));
            }
            if (KernelUtils.fileExists(FREQ_MAX_FILE)) {
                temp = KernelUtils.readOneLine(FREQ_MAX_FILE);
                mMaxFrequencyPref.setValue(temp);
                mMaxFrequencyPref.setSummary(String.format(mMaxFrequencyFormat, toMHz(temp)));
            }
        }

        if (KernelUtils.fileExists(SCHEDULER_FILE)) {
            temp = KernelUtils.readOneLine(SCHEDULER_FILE);
            String[] availableSchedulers = temp.split(" ");
            if (availableSchedulers != null) {
                String schedulerResume = getCurrentScheduler(availableSchedulers);
                mSchedulerPref.setValue(schedulerResume);
                mSchedulerPref.setSummary(String.format(mSchedulerFormat, schedulerResume));
            }
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String fname = "";

        if (newValue != null) {
            if (preference == mGovernorPref) {
                fname = GOVERNOR_FILE;
            } else if (preference == mMinFrequencyPref) {
                fname = FREQ_MIN_FILE;
            } else if (preference == mMaxFrequencyPref) {
                fname = FREQ_MAX_FILE;
            } else if (preference == mSchedulerPref) {
                fname = SCHEDULER_FILE;
            }

            if (KernelUtils.writeOneLine(fname, (String) newValue)) {
                if (preference == mGovernorPref) {
                    mGovernorPref.setSummary(String.format(mGovernorFormat, (String) newValue));
                } else if (preference == mMinFrequencyPref) {
                    mMinFrequencyPref.setSummary(String.format(mMinFrequencyFormat,
                            toMHz((String) newValue)));
                } else if (preference == mMaxFrequencyPref) {
                    mMaxFrequencyPref.setSummary(String.format(mMaxFrequencyFormat,
                            toMHz((String) newValue)));
                } else if (preference == mSchedulerPref) {
                    mSchedulerPref.setSummary(String.format(mSchedulerFormat, (String) newValue));
                }
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    private String toMHz(String mhzString) {
        return new StringBuilder().append(Integer.valueOf(mhzString) / 1000).append(" MHz").toString();
    }

    private String getCurrentScheduler(String[] available) {
        String[] schedulers = new String[available.length];
        int current = 0;
        for (int i = 0; i < schedulers.length; i++) {
            if(available[i].startsWith("[")) {
                available[i] = available[i].substring(1, available[i].length() - 1);
                current = i;
                break;
            }
        }
        return available[current];
    }
}
