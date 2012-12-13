/*
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

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

//
// Common functions to adjust kernel sysfs values
//
public class KernelUtils {

    private static final String TAG = "KernelUtils";


    // Returns true if file exists/sys/class/misc/soundcontrol/highperf_enabled
    public static boolean fileExists(String fname) {
        return new File(fname).exists();
    }

    // Returns the String value of a file
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

    // Writes a String value to a file and returns true if successful
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
}
