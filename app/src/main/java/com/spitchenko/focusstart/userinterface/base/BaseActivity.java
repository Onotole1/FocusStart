package com.spitchenko.focusstart.userinterface.base;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.model.Channel;
import com.spitchenko.focusstart.model.ChannelItem;
import com.spitchenko.focusstart.userinterface.settingswindow.SettingsActivity;

import java.util.ArrayList;

import lombok.NonNull;

/**
 * Date: 23.03.17
 * Time: 14:17
 *
 * @author anatoliy
 */
public abstract class BaseActivity extends AppCompatActivity {
	protected RecyclerView recyclerView;
	protected Parcelable recyclerState;

    @Override
	public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
		getMenuInflater().inflate(R.menu.channel_menu, menu);
		return true;
	}

	@Override
	public boolean onSupportNavigateUp() {
		onBackPressed();
		return true;
	}

	protected ArrayList<Channel> convertObjectToChannelList(@NonNull final Object object) {
		final ArrayList<Channel> result = new ArrayList<>();
		if (object instanceof ArrayList) {
			for (final Object o:(ArrayList)object) {
				if (o instanceof Channel) {
					result.add((Channel) o);
				}
			}
		}
		return result;
	}

	protected ArrayList<ChannelItem> convertObjectToChannelItemList(@NonNull final Object object) {
		final ArrayList<ChannelItem> result = new ArrayList<>();
		if (object instanceof ArrayList) {
			for (final Object o:(ArrayList)object) {
				if (o instanceof ChannelItem) {
					result.add((ChannelItem) o);
				}
			}
		}
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_settings:
				showSettingsFragment();
		}
		return super.onOptionsItemSelected(item);
	}

	private void showSettingsFragment() {
		final Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (null != savedInstanceState && null != recyclerView) {
            final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            layoutManager.onRestoreInstanceState(recyclerState);
            recyclerView.setLayoutManager(layoutManager);
        }
    }

    protected void restoreRecyclerState() {
		if (null != recyclerState && null != recyclerView) {
			final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
			layoutManager.onRestoreInstanceState(recyclerState);
			recyclerView.setLayoutManager(layoutManager);
		}
	}
}
