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
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

//
// CPU Related Settings
//
public class CPUSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String GOV_PREF = "cpu_governor";
    public static final String GOVERNORS_LIST_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    public static final String GOVERNOR = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static final String MIN_FREQ_PREF = "cpu_freq_min";
    public static final String MAX_FREQ_PREF = "cpu_freq_max";
    public static final String FREQ_LIST_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static final String FREQ_MIN_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    public static final String FREQ_MAX_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    public static final String SCHED_PREF = "cpu_scheduler";
    public static final String SCHEDULER_FILE = "/sys/block/mtdblock4/queue/scheduler";

    private static final String TAG = "CPUSettings";

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

        String[] availableGovernors;
        String[] availableFrequencies;
        String[] availableSchedulers;
        String[] frequencies;
        String schedulerCurrent;
        String temp;
        String tempFreq;

        addPreferencesFromResource(R.xml.cpu_settings);

        PreferenceScreen PrefScreen = getPreferenceScreen();

        mGovernorPref = (ListPreference) PrefScreen.findPreference(GOV_PREF);
        mMinFrequencyPref = (ListPreference) PrefScreen.findPreference(MIN_FREQ_PREF);
        mMaxFrequencyPref = (ListPreference) PrefScreen.findPreference(MAX_FREQ_PREF);
        mSchedulerPref = (ListPreference) PrefScreen.findPreference(SCHED_PREF);

        temp = readOneLine(GOVERNOR);
        if (temp == null) {
            mGovernorPref.setEnabled(false);
            mGovernorPref.setSummary(R.string.cpu_not_supported);
        } else {
            availableGovernors = readOneLine(GOVERNORS_LIST_FILE).split(" ");
            mGovernorPref.setEntryValues(availableGovernors);
            mGovernorPref.setEntries(availableGovernors);
            mGovernorPref.setValue(temp);
            mGovernorPref.setSummary(String.format(mGovernorFormat, temp));
            mGovernorPref.setOnPreferenceChangeListener(this);
        }

        tempFreq = readOneLine(FREQ_LIST_FILE);

        if (tempFreq == null) {
            mMinFrequencyPref.setEnabled(false);
            mMaxFrequencyPref.setEnabled(false);
            mMinFrequencyPref.setSummary(R.string.cpu_not_supported);
            mMaxFrequencyPref.setSummary(R.string.cpu_not_supported);
        } else {
            temp = readOneLine(FREQ_MIN_FILE);
            if (temp != null) {
                availableFrequencies = readOneLine(FREQ_LIST_FILE).split(" ");

                frequencies = new String[availableFrequencies.length];
                for (int i = 0; i < frequencies.length; i++) {
                    frequencies[i] = toMHz(availableFrequencies[i]);
                }

                mMinFrequencyPref.setEntryValues(availableFrequencies);
                mMinFrequencyPref.setEntries(frequencies);
                mMinFrequencyPref.setValue(temp);
                mMinFrequencyPref.setSummary(String.format(mMinFrequencyFormat, toMHz(temp)));
                mMinFrequencyPref.setOnPreferenceChangeListener(this);
            }

            temp = readOneLine(FREQ_MAX_FILE);
            if (temp != null) {
                availableFrequencies = readOneLine(FREQ_LIST_FILE).split(" ");

                frequencies = new String[availableFrequencies.length];
                for (int i = 0; i < frequencies.length; i++) {
                    frequencies[i] = toMHz(availableFrequencies[i]);
                }

                mMaxFrequencyPref.setEntryValues(availableFrequencies);
                mMaxFrequencyPref.setEntries(frequencies);
                mMaxFrequencyPref.setValue(temp);
                mMaxFrequencyPref.setSummary(String.format(mMaxFrequencyFormat, toMHz(temp)));
                mMaxFrequencyPref.setOnPreferenceChangeListener(this);
            }
        }

        temp = readOneLine(SCHEDULER_FILE);
        if (temp == null) {
            mSchedulerPref.setEnabled(false);
            mSchedulerPref.setSummary(R.string.cpu_not_supported);
        } else {
            availableSchedulers = readOneLine(SCHEDULER_FILE).split(" ");
            schedulerCurrent = getCurrentScheduler(availableSchedulers);
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
        String tempFreq;
        String schedulerResume;
        String[] availableSchedulersResume;

        super.onResume();

        temp = readOneLine(GOVERNOR);
        if (temp != null) {
            mGovernorPref.setSummary(String.format(mGovernorFormat, temp));
        }

        tempFreq = readOneLine(FREQ_LIST_FILE);

        if (tempFreq != null) {
            temp = readOneLine(FREQ_MIN_FILE);
            if (temp != null) {
                mMinFrequencyPref.setValue(temp);
                mMinFrequencyPref.setSummary(String.format(mMinFrequencyFormat, toMHz(temp)));
            }

            temp = readOneLine(FREQ_MAX_FILE);
            if (temp != null) {
                mMaxFrequencyPref.setValue(temp);
                mMaxFrequencyPref.setSummary(String.format(mMaxFrequencyFormat, toMHz(temp)));
            }
        }

        temp = readOneLine(SCHEDULER_FILE);
        if (temp != null) {
            availableSchedulersResume = readOneLine(SCHEDULER_FILE).split(" ");
            if (availableSchedulersResume != null) {
                schedulerResume = getCurrentScheduler(availableSchedulersResume);
                mSchedulerPref.setValue(schedulerResume);
                mSchedulerPref.setSummary(String.format(mSchedulerFormat, schedulerResume));
            }
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String fname = "";

        if (newValue != null) {
            if (preference == mGovernorPref) {
                fname = GOVERNOR;
            } else if (preference == mMinFrequencyPref) {
                fname = FREQ_MIN_FILE;
            } else if (preference == mMaxFrequencyPref) {
                fname = FREQ_MAX_FILE;
            } else if (preference == mSchedulerPref) {
                fname = SCHEDULER_FILE;
            }

            if (writeOneLine(fname, (String) newValue)) {
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

    public static String readOneLine(String fname) {
        BufferedReader br;
        String line = null;
        File readFile = new File(fname);
        if (readFile.exists()) {
            try {
                br = new BufferedReader(new FileReader(fname), 512);
                try {
                    line = br.readLine();
                } finally {
                    br.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "IO Exception when reading /sys/ file", e);
            }
        }
        return line;
    }

    public static boolean writeOneLine(String fname, String value) {
        try {
            FileWriter fw = new FileWriter(fname);
            try {
                fw.write(value);
            } finally {
                fw.close();
            }
        } catch (IOException e) {
            String Error = "Error writing to " + fname + ". Exception: ";
            Log.e(TAG, Error, e);
            return false;
        }
        return true;
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
