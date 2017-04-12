package com.spitchenko.focusstart.controller.channelwindow;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.controller.ActivityAndBroadcastObserver;
import com.spitchenko.focusstart.controller.BaseActivityController;
import com.spitchenko.focusstart.controller.KitkatHackController;
import com.spitchenko.focusstart.controller.NetworkDialogShowController;
import com.spitchenko.focusstart.controller.UpdateController;
import com.spitchenko.focusstart.controller.VersionAndroidComparator;
import com.spitchenko.focusstart.model.Channel;
import com.spitchenko.focusstart.userinterface.base.ChannelRecyclerEmptyAdapter;
import com.spitchenko.focusstart.userinterface.base.NoInternetDialog;
import com.spitchenko.focusstart.userinterface.channelwindow.ChannelAddDialogFragment;
import com.spitchenko.focusstart.userinterface.channelwindow.ChannelRecyclerAdapter;
import com.spitchenko.focusstart.userinterface.channelwindow.ChannelRefreshDialog;

import java.util.ArrayList;

import lombok.NonNull;

/**
 * Date: 31.03.17
 * Time: 19:40
 *
 * @author anatoliy
 */
public class ChannelActivityAndBroadcastObserver extends BaseActivityController
        implements ActivityAndBroadcastObserver {
    private final static String CHANNEL_BROADCAST_OBSERVER
            = "com.spitchenko.focusstart.controller.channel_window.ChannelActivityAndBroadcastObserver";
    private final static String RECYCLER_STATE = CHANNEL_BROADCAST_OBSERVER + ".recyclerState";
    private final static String UPDATE = CHANNEL_BROADCAST_OBSERVER + ".update";
    private final static String REFRESHING = CHANNEL_BROADCAST_OBSERVER + ".refreshing";
    private final static String LOCAL_THEME = CHANNEL_BROADCAST_OBSERVER + ".localTHeme";

    private final ChannelBroadcastReceiver channelBroadcastReceiver = new ChannelBroadcastReceiver();
    private LocalBroadcastManager localBroadcastManager;
    private AppCompatActivity activity;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Parcelable recyclerState;
    private boolean isUpdate;
    private final ArrayList<Channel> receivedChannels = new ArrayList<>();
    private UpdateController updateController;
    private NetworkDialogShowController networkDialogShowController;
    private KitkatHackController kitkatHackController;
    private LinearLayout stubLayout;
    private ProgressBar progressBar;

    public ChannelActivityAndBroadcastObserver(final AppCompatActivity context) {
        this.activity = context;
    }

    @Override
    public void updateOnCreate(@Nullable final Bundle savedInstanceState) {
        if (activity.getIntent().getAction().equals(UPDATE)) {
            isUpdate = true;
        }

        setThemeFromPrefs(activity);
        activity.setContentView(R.layout.activity_channel);

        final Toolbar toolbar = (Toolbar) activity.findViewById(R.id.activity_channel_toolbar);
        activity.setSupportActionBar(toolbar);
        if (null != activity.getSupportActionBar()) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        final FloatingActionButton fab = (FloatingActionButton) activity
                .findViewById(R.id.activity_channel_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showAddChannelDialog();
            }
        });

        recyclerView = (RecyclerView) activity.findViewById(R.id.activity_channel_recycler_view);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy){
                if (dy > 0 ||dy<0 && fab.isShown()) {
                    fab.hide();
                }
            }

            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {

                if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    fab.show();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        swipeRefreshLayout
                = (SwipeRefreshLayout) activity.findViewById(R.id.activity_channel_swipe_refresh_layout);

        final ItemTouchHelper.SimpleCallback simpleItemTouchCallback
                = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder
                    , final RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int swipeDir) {
                final ChannelRecyclerAdapter channelAdapter
                        = (ChannelRecyclerAdapter) recyclerView.getAdapter();
                final Channel channelFromRecycler
                        = channelAdapter.getChannels().get(viewHolder.getAdapterPosition());
                channelAdapter.removeItem(viewHolder.getAdapterPosition());

                RssChannelIntentService.start(RssChannelIntentService.getRemoveChannelKey()
                        , activity, channelFromRecycler, null);
            }
        };

        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RssChannelIntentService.start(RssChannelIntentService.getRefreshAllChannelsKey()
                        , activity, null, null);
            }
        });

        stubLayout = (LinearLayout) activity.findViewById(R.id.activity_channel_stub);
        progressBar = (ProgressBar) activity.findViewById(R.id.activity_channel_progressBar);

        updateController = new UpdateController(activity);
        updateController.turnOnUpdate();

        networkDialogShowController = new NetworkDialogShowController(activity);

        if (VersionAndroidComparator.isAndroidOld()) {
            kitkatHackController = new KitkatHackController(activity);
        }
    }

    @Override
    public void updateOnResume() {
        if (readLocalThemeIdFromPrefs() != readThemeIdOrNullFromPrefs(activity)) {
            activity.recreate();
            if (VersionAndroidComparator.isAndroidOld()) {
                kitkatHackController.turnOnHackController();
            }
            return;
        }

        subscribe();

        if (updateController.isUpdate()) {

            RssChannelIntentService.start(RssChannelIntentService.getReadChannelsKey(), activity, null
                    , null);

            updateController.turnOffUpdate();
        }
    }

    private void subscribe() {
        localBroadcastManager = LocalBroadcastManager.getInstance(activity);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ChannelBroadcastReceiver.getReceiveChannelsKey());
        intentFilter.addAction(ChannelBroadcastReceiver.getRefreshDialogKey());
        intentFilter.addAction(ChannelBroadcastReceiver.getRemoveAction());
        intentFilter.addAction(ChannelBroadcastReceiver.getIoExceptionAction());
        intentFilter.addAction(ChannelBroadcastReceiver.getNoInternetAction());
        intentFilter.addAction(ChannelBroadcastReceiver.getLoadingAction());
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        localBroadcastManager.registerReceiver(channelBroadcastReceiver, intentFilter);

        channelBroadcastReceiver.addObserver(this);
    }


    //В андроиде 4.4.4 и 5.0.0 этот метод зачем-то вызывается через цепочку activity.recreate
    // -> onCreate() -> onResume() -> onPause()
    // Для обработки этого состояния был создан класс KitkatHackController
    @Override
    public void updateOnPause() {
        if (VersionAndroidComparator.isAndroidOld()) {
            if (kitkatHackController.isHack() &&
                    1 == kitkatHackController.getOnPauseSequence()) {
                kitkatHackController.setNullOnPauseSequence();
                kitkatHackController.turnOffHackController();
            } else if (kitkatHackController.isHack()) {
                unSubscribe();
                kitkatHackController.onPauseSequenceIncrement();
            } else {
                unSubscribe();
            }
        } else {
            unSubscribe();
        }
    }

    private void unSubscribe() {
        localBroadcastManager.unregisterReceiver(channelBroadcastReceiver);
        channelBroadcastReceiver.removeObserver(this);
        isUpdate = false;
    }

    @Override
    public void updateOnReceiveItems(final ArrayList<?> items, final String action) {
        receivedChannels.clear();
        receivedChannels.addAll(convertArrayListToChannelList(items));
        if (null == recyclerView.getLayoutManager()) {
            final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
            recyclerView.setLayoutManager(layoutManager);
        }
        if (!receivedChannels.isEmpty()) {
            stopLoading();
            final ChannelRecyclerAdapter channelAdapter = new ChannelRecyclerAdapter(receivedChannels);
            recyclerView.setAdapter(channelAdapter);

            if (null != recyclerState) {
                recyclerView.getLayoutManager().onRestoreInstanceState(recyclerState);
            }
            if (isUpdate) {
                checkUpdates(receivedChannels);
            }

        } else {
            stopLoading();
            recyclerView.setAdapter(new ChannelRecyclerEmptyAdapter());
        }
        if (null != action) {
            if (action.equals(ChannelBroadcastReceiver.getNoInternetAction())) {
                stopLoading();
                showNetworkDialog();
            } else if (action.equals(ChannelBroadcastReceiver.getIoExceptionAction())) {
                stopLoading();
                final String toastContent = activity.getResources().getString(R.string.activity_channel_controller_io_exception);
                Toast.makeText(activity, toastContent, Toast.LENGTH_LONG).show();
            } else if (action.equals(ChannelBroadcastReceiver.getLoadingAction())) {
                startLoading();
            }
        }
        swipeRefreshLayout.setRefreshing(false);
        updateController.turnOffUpdate();
    }

    private void startLoading() {
        stubLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        stubLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void checkUpdates(final ArrayList<Channel> receivedChannels) {
        final SharedPreferences preferences
                = RssChannelIntentService.getReadMessagesPreferences(activity);
        for (final Channel channel:receivedChannels) {
            final String message = preferences.getString(channel.getLink(), null);
            if (null != message) {
                showRefreshDialog(channel.getLink(), message);
            }
        }
    }

    void updateOnReceiveNotification() {
        checkUpdates(receivedChannels);
    }

    @Override
    public void updateOnRestoreInstanceState(@Nullable final Bundle savedInstanceState) {
        if (null != savedInstanceState) {
            recyclerState = savedInstanceState.getParcelable(RECYCLER_STATE);
            if (savedInstanceState.getBoolean(UPDATE)) {
                swipeRefreshLayout.setRefreshing(true);
            }
            if (savedInstanceState.getBoolean(REFRESHING)) {
                startLoading();
            }
        }
    }

    @Override
    public void updateOnSavedInstanceState(final Bundle outState) {
        if (null != recyclerView && null != outState && null != recyclerView.getLayoutManager()) {
            outState.putParcelable(RECYCLER_STATE, recyclerView.getLayoutManager().onSaveInstanceState());
            outState.putBoolean(UPDATE, swipeRefreshLayout.isRefreshing());
            if (progressBar.isShown()) {
                outState.putBoolean(REFRESHING, true);
            }
        }
    }

    private ArrayList<Channel> convertArrayListToChannelList(@NonNull final ArrayList<?> list) {
        final ArrayList<Channel> result = new ArrayList<>();
        for (final Object object:list) {
            if (object instanceof Channel) {
                result.add((Channel) object);
            }
        }
        return result;
    }

    private void showAddChannelDialog() {
        final ChannelAddDialogFragment dialogFragment = new ChannelAddDialogFragment();
        final android.app.FragmentTransaction fragmentTransaction = activity.getFragmentManager().beginTransaction();
        fragmentTransaction.add(dialogFragment, ChannelAddDialogFragment.getDialogFragmentTag());
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void showNetworkDialog() {
        if (!networkDialogShowController.isNetworkDialogShow()) {
            final NoInternetDialog noInternetDialog = new NoInternetDialog();
            final FragmentManager fragmentManager = activity.getFragmentManager();
            final android.app.FragmentTransaction fragmentTransaction
                    = fragmentManager.beginTransaction();
            fragmentTransaction.add(noInternetDialog, NoInternetDialog.getNoInternetDialogKey());
            fragmentTransaction.commitAllowingStateLoss();
            networkDialogShowController.turnOnNetworkDialog();
        }
    }

    private void showRefreshDialog(@NonNull final String url, @NonNull final String message) {
        final SharedPreferences preferences
                = RssChannelIntentService.getReadMessagesPreferences(activity);
        final SharedPreferences.Editor edit = preferences.edit();
        edit.putString(url, null);
        edit.apply();

        final ChannelRefreshDialog channelRefreshDialog = new ChannelRefreshDialog();
        final Bundle refreshBundle = new Bundle();
        refreshBundle.putString(ChannelRefreshDialog.CHANNEL_URL, url);
        refreshBundle.putString(ChannelRefreshDialog.MESSAGE, message);
        channelRefreshDialog.setArguments(refreshBundle);
        final android.app.FragmentTransaction fragmentTransaction
                = activity.getFragmentManager().beginTransaction();
        fragmentTransaction.add(channelRefreshDialog, ChannelAddDialogFragment.getDialogFragmentTag());
        fragmentTransaction.commitAllowingStateLoss();
    }

    public static String getUpdateKey() {
        return UPDATE;
    }

    @Override
    protected void saveLocalThemeIdToPrefs(final int themeId) {
        final SharedPreferences preferences
                = activity.getSharedPreferences(LOCAL_THEME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(LOCAL_THEME, themeId);
        editor.apply();
    }

    @Override
    protected int readLocalThemeIdFromPrefs() {
        final SharedPreferences preferences
                = activity.getSharedPreferences(LOCAL_THEME, Context.MODE_PRIVATE);
        return preferences.getInt(LOCAL_THEME, 0);
    }
}
