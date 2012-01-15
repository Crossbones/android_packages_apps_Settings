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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;

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
        String minFrequency = prefs.getString(CPUSettings.MIN_FREQ_PREF, null);
        String maxFrequency = prefs.getString(CPUSettings.MAX_FREQ_PREF, null);
        String scheduler = prefs.getString(CPUSettings.SCHED_PREF, null);

        boolean noSettings = (governor == null) && (minFrequency == null) && (maxFrequency == null) && (scheduler == null);

        if (noSettings) {
            Log.d(TAG, "No settings saved. Nothing to restore.");
        } else {
            List<String> governors = Arrays.asList(CPUSettings.readOneLine(
                    CPUSettings.GOVERNORS_LIST_FILE).split(" "));
            List<String> frequencies = Arrays.asList(CPUSettings.readOneLine(
                    CPUSettings.FREQ_LIST_FILE).split(" "));
            List<String> schedulers = Arrays.asList(CPUSettings.readOneLine(
                    CPUSettings.SCHEDULER_FILE).split(" "));
            if (governor != null && governors.contains(governor)) {
                CPUSettings.writeOneLine(CPUSettings.GOVERNOR, governor);
            }
            if (maxFrequency != null && frequencies.contains(maxFrequency)) {
                CPUSettings.writeOneLine(CPUSettings.FREQ_MAX_FILE, maxFrequency);
            }
            if (minFrequency != null && frequencies.contains(minFrequency)) {
                CPUSettings.writeOneLine(CPUSettings.FREQ_MIN_FILE, minFrequency);
            }
            if (scheduler != null && schedulers.contains(scheduler)) {
                CPUSettings.writeOneLine(CPUSettings.SCHEDULER_FILE, scheduler);
            }
            Log.d(TAG, "CPU settings restored.");
        }
    }
}
