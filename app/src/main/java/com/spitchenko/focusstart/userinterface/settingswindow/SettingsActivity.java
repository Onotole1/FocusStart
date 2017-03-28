package com.spitchenko.focusstart.userinterface.settingswindow;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.spitchenko.focusstart.R;

/**
 * Date: 23.03.17
 * Time: 9:29
 *
 * @author anatoliy
 */
public final class SettingsActivity extends AppCompatActivity {


	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		final Toolbar toolbar = (Toolbar) findViewById(R.id.activity_settings_toolbar);
		setSupportActionBar(toolbar);
		if (null != getSupportActionBar()) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
		}
		if (null == savedInstanceState) {
			getFragmentManager().beginTransaction().replace(R.id.app_bar_layout_container, new SettingsFragment()).commit();
		}
	}

	@Override
	public boolean onSupportNavigateUp() {
		onBackPressed();
		return true;
	}
}
