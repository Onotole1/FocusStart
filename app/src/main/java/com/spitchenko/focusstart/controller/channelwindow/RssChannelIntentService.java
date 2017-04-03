package com.spitchenko.focusstart.controller.channelwindow;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.webkit.URLUtil;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.database.AtomRssChannelDbHelper;
import com.spitchenko.focusstart.database.AtomRssDataBase;
import com.spitchenko.focusstart.model.Channel;
import com.spitchenko.focusstart.model.ChannelItem;
import com.spitchenko.focusstart.userinterface.channelwindow.ChannelActivity;
import com.spitchenko.focusstart.utils.logger.LogCatHandler;
import com.spitchenko.focusstart.utils.parser.AtomRssParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

import lombok.NonNull;

import static com.spitchenko.focusstart.model.ChannelItem.countMatches;

/**
 * Date: 09.03.17
 * Time: 15:18
 *
 * @author anatoliy
 */
public final class RssChannelIntentService extends IntentService {
	private final static String NAME_CHANNEL_SERVICE = "com.spitchenko.focusstart.controller.channel_window.RssChannelIntentService";
	private final static String KEY_URL = NAME_CHANNEL_SERVICE + "url";
	private final static String READ_CURRENT_CHANNEL = NAME_CHANNEL_SERVICE + ".readCurrentChannelDb";
	private final static String READ_CHANNELS = NAME_CHANNEL_SERVICE + ".controller.channelsDb";
	private final static String REMOVE_CHANNEL = NAME_CHANNEL_SERVICE + ".controller.removeChannel";
	private final static String READ_WRITE_ACTION = NAME_CHANNEL_SERVICE + ".readWriteFromUrl";
	private final static String REFRESH = NAME_CHANNEL_SERVICE + ".refresh";
    private final static String NOTIFICATION = NAME_CHANNEL_SERVICE + ".notification";
    private final static String REFRESH_CURRENT_CHANNEL = NAME_CHANNEL_SERVICE + ".refreshCurrent";
    private final static String REFRESH_ALL_CHANNELS = NAME_CHANNEL_SERVICE + ".refreshAll";
    private final static int NOTIFICATION_ID = 100500;

	public RssChannelIntentService() {
		super(NAME_CHANNEL_SERVICE);
	}

	@Override
	protected final void onHandleIntent(@Nullable final Intent intent) {
        if (null != intent && null != intent.getAction()) {
            switch (intent.getAction()) {
                case READ_CURRENT_CHANNEL:
                    readCurrentChannelDb(intent, ChannelBroadcastReceiver.getReceiveChannelsKey());
                    break;
                case READ_WRITE_ACTION:
                    readWriteFromUrl(intent, ChannelBroadcastReceiver.getReceiveChannelsKey());
                    break;
                case READ_CHANNELS:
                    readChannelsFromDb(ChannelBroadcastReceiver.getReceiveChannelsKey());
                    break;
                case REMOVE_CHANNEL:
                    removeChannel(intent);
                    break;
                case REFRESH:
                    refresh();
                    break;
                case NOTIFICATION:
                    notificationReload();
                    break;
                case REFRESH_CURRENT_CHANNEL:
                    refreshCurrentChannel(intent);
                    break;
                case REFRESH_ALL_CHANNELS:
                    refreshAllChannels();
                    break;
            }
        }
    }

    private void refreshCurrentChannel(final Intent intent) {
        final AtomRssChannelDbHelper channelDbHelper = new AtomRssChannelDbHelper(this);
        final AtomRssParser atomRssParser = new AtomRssParser();
        final String inputUrl = formatHttp(intent.getStringExtra(KEY_URL));

        final Channel channelFromDb = channelDbHelper.readChannelFromDb(inputUrl);
        try {
            final Channel channelFromUrl = atomRssParser.parseXml(inputUrl);

            channelDbHelper.refreshCurrentChannel(channelFromDb, channelFromUrl);

            readChannelsFromDb(ChannelBroadcastReceiver.getReceiveChannelsKey());


        } catch (final IOException | XmlPullParserException e) {
            if (!checkConnection()) {
                ChannelBroadcastReceiver.start(null
                        , ChannelBroadcastReceiver.getNoInternetAction(), getPackageName(), this);
            } else {
                ChannelBroadcastReceiver.start(null
                        , ChannelBroadcastReceiver.getIoExceptionAction(), getPackageName(), this);
            }
        }
    }

    private void refreshAllChannels() {
        final AtomRssChannelDbHelper channelDbHelper = new AtomRssChannelDbHelper(this);
        final ArrayList<Channel> channelsDb = channelDbHelper.readAllChannelsFromDb();
        final AtomRssParser atomRssParser = new AtomRssParser();

        for (final Channel channel:channelsDb) {
            try {
                final Channel channelUrl = atomRssParser.parseXml(channel.getLink());

                final ArrayList<ChannelItem> itemsAll
                        = new ArrayList<>(channel.getChannelItems().size());

                for (final ChannelItem item:channel.getChannelItems()) {
                    itemsAll.add(item.cloneChannelItem());
                }
                for (final ChannelItem item:channelUrl.getChannelItems()) {
                    itemsAll.add(item.cloneChannelItem());
                }

                if (channel.getChannelItems().size() > countMatches(itemsAll)) {
                    channelDbHelper.refreshCurrentChannel(channel, channelUrl);
                }
                readChannelsFromDb(ChannelBroadcastReceiver.getReceiveChannelsKey());
            } catch (final IOException | XmlPullParserException e) {
                if (!checkConnection()) {
                    ChannelBroadcastReceiver.start(null
                            , ChannelBroadcastReceiver.getNoInternetAction(), getPackageName()
                            , this);
                } else {
                    ChannelBroadcastReceiver.start(null
                            , ChannelBroadcastReceiver.getIoExceptionAction(), getPackageName()
                            , this);
                }
            }
        }
    }

    private void notificationReload() {
        ChannelActivity.start(this);
        ChannelBroadcastReceiver.start(null, ChannelBroadcastReceiver.getRefreshDialogKey()
                , getPackageName(), this);
    }

    private void refresh() {
		final AtomRssChannelDbHelper channelDbHelper = new AtomRssChannelDbHelper(this);
		final AtomRssParser atomRssParser = new AtomRssParser();
		final ArrayList<Channel> channelsFromDb = channelDbHelper.readAllChannelsFromDb();
		final ArrayList<Channel> channelsFromNet = new ArrayList<>();

		for (final Channel channelDb:channelsFromDb) {
            try {
                channelsFromNet.add(atomRssParser.parseXml(channelDb.getLink()));
            } catch (final IOException | XmlPullParserException e) {
                if (!checkConnection()) {
                    ChannelBroadcastReceiver.start(null
                            , ChannelBroadcastReceiver.getNoInternetAction(), getPackageName(), this);
                } else {
                    ChannelBroadcastReceiver.start(null
                            , ChannelBroadcastReceiver.getIoExceptionAction(), getPackageName()
                            , this);
                }
            }
        }

        channelsFromDb.addAll(channelsFromNet);
        final HashMap<Channel, Integer> channels = convertChannelsToMap(channelsFromDb);

        if (!channels.isEmpty()) {
            sendNotification(channels);
        }
	}

	private HashMap<Channel, Integer> convertChannelsToMap(
	        final ArrayList<Channel> channels) {
        final HashMap<Channel, Integer> channelMap = new HashMap<>();
        final Iterator<Channel> channelIterator = channels.iterator();
        while (channelIterator.hasNext()) {
            final Channel current = channelIterator.next();
            channelIterator.remove();
            for (final Channel leftChannel:channels) {
                if (leftChannel.getLink().equals(current.getLink())) {
                    final ArrayList<ChannelItem> channelItems = current.getChannelItems();
                    channelItems.addAll(leftChannel.getChannelItems());
                    current.setChannelItems(channelItems);
                    final Integer features = current.getChannelItems().size() / 2
                            - countMatches(current.getChannelItems());
                    if (features > 0) {
                        channelMap.put(current, features);
                    }
                }
            }
        }
        return channelMap;
    }

	private void removeChannel(@NonNull final Intent intent) {
		final AtomRssChannelDbHelper channelDbHelper = new AtomRssChannelDbHelper(this);
		final Channel inputChannel = intent.getParcelableExtra(REMOVE_CHANNEL);
		channelDbHelper.deleteChannelFromDb(inputChannel);
        ChannelBroadcastReceiver.start(inputChannel, REMOVE_CHANNEL, getPackageName(), this);
	}

	private void readWriteFromUrl(@NonNull final Intent intent, @Nullable final String action) {
		final AtomRssParser atomRssParser = new AtomRssParser();
		final AtomRssChannelDbHelper channelDbHelper = new AtomRssChannelDbHelper(this);

		final String inputUrl = formatHttp(intent.getStringExtra(KEY_URL));

		final Channel channelFromDb = channelDbHelper.readChannelFromDb(inputUrl);
        try {
            final Channel channelFromUrl = atomRssParser.parseXml(inputUrl);

            if (channelFromDb != null) {
                channelDbHelper.deleteChannelFromDb(channelFromDb);
                channelDbHelper.writeChannelToDb(channelFromUrl);
                ChannelBroadcastReceiver.start(channelFromUrl, action, getPackageName(), this);
            } else {
                channelDbHelper.writeChannelToDb(channelFromUrl);
                ChannelBroadcastReceiver.start(channelFromUrl, action, getPackageName(), this);
            }

        } catch (final IOException | XmlPullParserException e) {
            if (!checkConnection()) {
                ChannelBroadcastReceiver.start(null, ChannelBroadcastReceiver.getNoInternetAction()
                        , getPackageName(), this);
            } else {
                ChannelBroadcastReceiver.start(null
                        , ChannelBroadcastReceiver.getIoExceptionAction(), getPackageName(), this);
            }
        }

	}

    private boolean checkConnection() {
        try {
            final URL httpsLink = new URL("https://www.google.ru/");
            final HttpURLConnection httpURLConnection = (HttpURLConnection) httpsLink.openConnection();
            httpURLConnection.connect();
            if (HttpURLConnection.HTTP_OK == httpURLConnection.getResponseCode()) {
                httpURLConnection.disconnect();
                return true;
            } else {
                httpURLConnection.disconnect();
                return false;
            }
        } catch (final IOException e) {
            return false;
        }


    }

    private void readCurrentChannelDb(@NonNull final Intent intent, @NonNull final String action) {
		final Channel inputChannel = intent.getParcelableExtra(READ_CURRENT_CHANNEL);
		if (!inputChannel.isRead()) {
			final AtomRssChannelDbHelper channelDbHelper = new AtomRssChannelDbHelper(this);
			channelDbHelper.updateValueFromDb(AtomRssDataBase.ChannelEntry.TABLE_NAME, AtomRssDataBase.ChannelEntry.CHANNEL_IS_READ
					, Long.toString(channelDbHelper.boolToLong(true))
					, AtomRssDataBase.ChannelEntry.CHANNEL_LINK, inputChannel.getLink());
			inputChannel.setRead(true);

            ChannelBroadcastReceiver.start(inputChannel, action, getPackageName(), this);
		}
	}

	private void readChannelsFromDb(@Nullable final String action) {
		final AtomRssChannelDbHelper channelDbHelper = new AtomRssChannelDbHelper(this);
		final ArrayList<Channel> channels = channelDbHelper.readAllChannelsFromDb();

		for (final Channel channel:channels) {
            ChannelBroadcastReceiver.start(channel, action, getPackageName(), this);
		}
	}

	private String formatHttp(@NonNull final String input) {
        if (!URLUtil.isHttpUrl(input) || !URLUtil.isHttpsUrl(input)) {
            try {
                final URL httpLink = new URL("http://" + input);
                final URL httpsLink = new URL("https://" + input);

                final HttpURLConnection httpURLConnection = (HttpURLConnection) httpLink.openConnection();
                httpURLConnection.connect();
                if (HttpURLConnection.HTTP_OK == httpURLConnection.getResponseCode()) {
                    httpURLConnection.disconnect();
                    return httpLink.toString();
                }

                final HttpsURLConnection httpsURLConnection = (HttpsURLConnection) httpsLink.openConnection();
                httpsURLConnection.connect();
                if (HttpURLConnection.HTTP_OK == httpsURLConnection.getResponseCode()) {
                    httpsURLConnection.disconnect();
                    return httpsLink.toString();
                }

                httpURLConnection.disconnect();
                httpsURLConnection.disconnect();
            } catch (final IOException e) {
                LogCatHandler.publishInfoRecord(e.getMessage());
            }
        }
		return input;
	}

    private String makeText(@NonNull final HashMap<Channel, Integer> input) {
        final StringBuilder result = new StringBuilder();

        for (final Channel key:input.keySet()) {
            final String plural = this.getResources()
                    .getQuantityString(R.plurals.plurals_news, input.get(key), input.get(key));
            result.append(getResources().getString(R.string.plural_prefix));
            result.append(" ").append(key.getTitle()).append(" ");
            result.append(plural);
            result.append("\n");
        }
        return result.toString().trim();
    }

    private String makeTextFromChannel(@NonNull final String title, final int number) {
            final StringBuilder stringBuilder = new StringBuilder();
            final String plural = this.getResources()
                    .getQuantityString(R.plurals.plurals_news, number, number);
            stringBuilder.append(getResources().getString(R.string.plural_prefix));
            stringBuilder.append(" ").append(title).append(" ");
            stringBuilder.append(plural);
            stringBuilder.append("\n");
        return stringBuilder.toString().trim();
    }

    private void sendNotification(@NonNull final HashMap<Channel, Integer> messages) {
        final String content = makeText(messages);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_rss_feed_white_18dp)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(content))
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true);

        for (final Channel key:messages.keySet()) {
            writeNotificationToPrefs(key.getLink(), makeTextFromChannel(key.getTitle()
                    , messages.get(key)));
        }
        final Intent resultIntent = new Intent(this, RssChannelIntentService.class);
        resultIntent.setAction(NOTIFICATION);
        final PendingIntent resultPendingIntent = PendingIntent.getService(this, 0, resultIntent, 0);
        builder.setContentIntent(resultPendingIntent);
        final NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static String getKeyUrl() {
        return KEY_URL;
    }

    public static String getReadCurrentChannelKey() {
        return READ_CURRENT_CHANNEL;
    }

    public static String getReadChannelsKey() {
        return READ_CHANNELS;
    }

    public static String getRemoveChannelKey() {
        return REMOVE_CHANNEL;
    }

    public static String getReadWriteActionKey() {
        return READ_WRITE_ACTION;
    }

    public static String getRefreshKey() {
        return REFRESH;
    }

    public static String getRefreshCurrentChannelKey() {
        return REFRESH_CURRENT_CHANNEL;
    }

    public static String getRefreshAllChannelsKey() {
        return REFRESH_ALL_CHANNELS;
    }

    public static void start(@NonNull final String action, @NonNull final Context context
            , @Nullable final Parcelable extra, @Nullable final String channelUrl) {
        final Intent intent = new Intent(context, RssChannelIntentService.class);
        intent.setAction(action);
        if (null != extra) {
            intent.putExtra(action, extra);
        } else if (null != channelUrl) {
            intent.putExtra(action, channelUrl);
        }
        context.startService(intent);
    }

    private void writeNotificationToPrefs(final String link, final String message) {
        final SharedPreferences sharedPreferences
                = getSharedPreferences(NOTIFICATION, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(link, message);
        editor.apply();
    }

    public static SharedPreferences getReadMessagesPreferences(final Context context) {
        return context.getSharedPreferences(NOTIFICATION, Context.MODE_PRIVATE);
    }
}
