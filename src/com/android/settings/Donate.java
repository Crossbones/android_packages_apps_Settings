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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Donate extends Activity implements OnClickListener {

    private Button mDonateButton;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.donate);

        mDonateButton = (Button) findViewById(R.id.donate_button);
        mDonateButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mDonateButton) {
            Intent browse = new Intent();
            browse.setAction(Intent.ACTION_VIEW);
            browse.setData(Uri.parse(getString(R.string.donate_url)));
            startActivity(browse);
        }
    }
}
