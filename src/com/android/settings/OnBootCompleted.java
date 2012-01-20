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

import com.android.settings.CPUSettings;
import com.android.settings.SoundSettings;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.List;

public class OnBootCompleted extends IntentService {

    private static final String TAG = "Settings.OnBootCompleted";
	public OnBootCompleted() {
		super("OnBootCompleted");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String governor = prefs.getString(CPUSettings.GOV_PREF, null);
        String existsFrequency = CPUSettings.readOneLine("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies");
        String minFrequency = prefs.getString(CPUSettings.MIN_FREQ_PREF, null);
        String maxFrequency = prefs.getString(CPUSettings.MAX_FREQ_PREF, null);
        String scheduler = prefs.getString(CPUSettings.SCHED_PREF, null);
        boolean bln = prefs.getBoolean(SoundSettings.KEY_BLN, true);
        boolean blnBlink = prefs.getBoolean(SoundSettings.KEY_BLN_BLINK, true);
        boolean blnExists = new File(SoundSettings.BLN_FILE).exists();
        boolean blnBlinkExists = new File(SoundSettings.BLN_BLINK_FILE).exists();

        boolean noSettings = (governor == null) && (minFrequency == null) && (maxFrequency == null) && (scheduler == null) && (blnExists == false) && (blnBlinkExists == false);

        if (noSettings) {
            Log.d(TAG, "No settings saved. No kernel specific settings to restore.");
        } else {
            if (governor != null) {
                List<String> governors = Arrays.asList(CPUSettings.readOneLine(
                        CPUSettings.GOVERNORS_LIST_FILE).split(" "));
                if(governors.contains(governor)) {
                    CPUSettings.writeOneLine(CPUSettings.GOVERNOR, governor);
                }
            }
            if (existsFrequency != null) {
                List<String> frequencies = Arrays.asList(CPUSettings.readOneLine(
                        CPUSettings.FREQ_LIST_FILE).split(" "));
                if (maxFrequency != null && frequencies.contains(maxFrequency)) {
                    CPUSettings.writeOneLine(CPUSettings.FREQ_MAX_FILE, maxFrequency);
                }
                if (minFrequency != null && frequencies.contains(minFrequency)) {
                    CPUSettings.writeOneLine(CPUSettings.FREQ_MIN_FILE, minFrequency);
                }
            }
            if (scheduler != null) {
                List<String> schedulers = Arrays.asList(CPUSettings.readOneLine(
                        CPUSettings.SCHEDULER_FILE).split(" "));
                if (schedulers.contains(scheduler)) {
                    CPUSettings.writeOneLine(CPUSettings.SCHEDULER_FILE, scheduler);
                }
            }
            if (bln == false || blnExists == false) {
                SoundSettings.writeOneLine(SoundSettings.BLN_FILE, "0");
            } else if (bln == true || blnExists == true){
                SoundSettings.writeOneLine(SoundSettings.BLN_FILE, "1");
            }
            if (blnBlink == false || blnBlinkExists == false) {
                SoundSettings.writeOneLine(SoundSettings.BLN_BLINK_FILE, "0");
            } else if (blnBlink == true || blnBlinkExists == true) {
                SoundSettings.writeOneLine(SoundSettings.BLN_BLINK_FILE, "1");
            }
            Log.d(TAG, "Kernel specific settings restored.");
        }
    }
}
