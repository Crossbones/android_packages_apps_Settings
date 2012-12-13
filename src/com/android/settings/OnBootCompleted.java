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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class OnBootCompleted extends IntentService {

    private static final String TAG = "OnBootCompleted";
	public OnBootCompleted() {
		super("OnBootCompleted");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String governor = prefs.getString(CPUSettings.KEY_GOVERNOR, null);
        String minFrequency = prefs.getString(CPUSettings.KEY_MIN_FREQ, null);
        String maxFrequency = prefs.getString(CPUSettings.KEY_MAX_FREQ, null);
        String scheduler = prefs.getString(CPUSettings.KEY_SCHEDULER, null);
        String gpuClock = prefs.getString(DisplaySettings.KEY_GPU_CLOCK, null);
        boolean bln = prefs.getBoolean(SoundSettings.KEY_BLN, true);
        boolean blnBlink = prefs.getBoolean(SoundSettings.KEY_BLN_BLINK, true);

        if (prefs == null) {
            Log.i(TAG, "No settings saved. No kernel specific settings to restore.");
        } else {
            // Set previous CPU governor
            if (governor != null) {
                List<String> governors = Arrays.asList(KernelUtils.readOneLine(
                        CPUSettings.GOVERNORS_LIST_FILE).split(" "));
                if(governors.contains(governor)) {
                    KernelUtils.writeOneLine(CPUSettings.GOVERNOR_FILE, governor);
                }
            }
            // Set previous min and max CPU frequencies
            if (KernelUtils.fileExists(CPUSettings.FREQ_LIST_FILE)) {
                List<String> frequencies = Arrays.asList(KernelUtils.readOneLine(
                        CPUSettings.FREQ_LIST_FILE).split(" "));
                if (minFrequency != null && frequencies.contains(minFrequency)) {
                    if (KernelUtils.fileExists(CPUSettings.FREQ_MIN_FILE)) {
                        KernelUtils.writeOneLine(CPUSettings.FREQ_MIN_FILE, minFrequency);
                    }
                }
                if (maxFrequency != null && frequencies.contains(maxFrequency)) {
                    if (KernelUtils.fileExists(CPUSettings.FREQ_MAX_FILE)) {
                        KernelUtils.writeOneLine(CPUSettings.FREQ_MAX_FILE, maxFrequency);
                    }
                }
            }
            // Set previous CPU scheduler
            if (scheduler != null) {
                List<String> schedulers = Arrays.asList(KernelUtils.readOneLine(
                        CPUSettings.SCHEDULER_FILE).split(" "));
                if (schedulers.contains(scheduler)) {
                    KernelUtils.writeOneLine(CPUSettings.SCHEDULER_FILE, scheduler);
                }
            }
            // Set previous GPU clock frequency
            if (KernelUtils.fileExists(DisplaySettings.GPU_CLOCK_FILE) && gpuClock != null) {
                KernelUtils.writeOneLine(DisplaySettings.GPU_CLOCK_FILE, gpuClock);
            }
            // Set previous BLN setting
            if (KernelUtils.fileExists(SoundSettings.BLN_FILE)) {
                if (!bln) {
                    KernelUtils.writeOneLine(SoundSettings.BLN_FILE, "0");
                } else {
                    KernelUtils.writeOneLine(SoundSettings.BLN_FILE, "1");
                }
            }
            // Set previous BLN Blink setting
            if (KernelUtils.fileExists(SoundSettings.BLN_FILE) && KernelUtils.fileExists(SoundSettings.BLN_BLINK_FILE)) {
              if (!blnBlink) {
                   KernelUtils.writeOneLine(SoundSettings.BLN_BLINK_FILE, "0");
               } else {
                   KernelUtils.writeOneLine(SoundSettings.BLN_BLINK_FILE, "1");
               }
            }
            Log.i(TAG, "Kernel specific settings restored.");
        }
    }
}
