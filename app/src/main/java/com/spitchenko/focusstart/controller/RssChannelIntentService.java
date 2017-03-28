package com.spitchenko.focusstart.controller;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.webkit.URLUtil;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.database.AtomRssChannelDbHelper;
import com.spitchenko.focusstart.database.AtomRssDataBase;
import com.spitchenko.focusstart.model.Channel;
import com.spitchenko.focusstart.model.ChannelItem;
import com.spitchenko.focusstart.model.Message;
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

/**
 * Date: 09.03.17
 * Time: 15:18
 *
 * @author anatoliy
 */
public final class RssChannelIntentService extends IntentService {
	private final static String NAME_CHANNEL_SERVICE = "com.spitchenko.focusstart.controller.RssChannelIntentService";
	private final static String KEY_URL = NAME_CHANNEL_SERVICE + "url";
	private final static String READ_CURRENT_CHANNEL = NAME_CHANNEL_SERVICE + ".readCurrentChannelDb";
	private final static String READ_CHANNELS = NAME_CHANNEL_SERVICE + ".controller.channelsDb";
	private final static String REMOVE_CHANNEL = NAME_CHANNEL_SERVICE + ".controller.removeChannel";
	private final static String READ_WRITE_ACTION = NAME_CHANNEL_SERVICE + ".readWriteFromUrl";
	private final static String REFRESH = NAME_CHANNEL_SERVICE + ".refresh";
    private final static String NOTIFICATION = NAME_CHANNEL_SERVICE + ".notification";
    private final static String REFRESH_CURRENT_CHANNEL = NAME_CHANNEL_SERVICE + ".refreshCurrent";
    private final static int NOTIFICATION_ID = 100500;

	public RssChannelIntentService() {
		super(NAME_CHANNEL_SERVICE);
	}

	@Override
	protected final void onHandleIntent(@Nullable final Intent intent) {
        if (null != intent && null != intent.getAction()) {
            switch (intent.getAction()) {
                case READ_CURRENT_CHANNEL:
                    readCurrentChannelDb(intent, null);
                    break;
                case READ_WRITE_ACTION:
                    readWriteFromUrl(intent, null);
                    break;
                case READ_CHANNELS:
                    readChannelsFromDb(null);
                    break;
                case REMOVE_CHANNEL:
                    removeChannel(intent);
                    break;
                case REFRESH:
                    refresh();
                    break;
                case NOTIFICATION:
                    notificationReload(intent);
                    break;
                case REFRESH_CURRENT_CHANNEL:
                    refreshCurrentChannel(intent);
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
            sendChannelToBroadcast(channelFromUrl, null);

        } catch (final IOException | XmlPullParserException e) {
            if (checkConnection()) {
                sendChannelToBroadcast(null, ChannelActivity.getIoExceptionActionKey());
            } else {
                sendChannelToBroadcast(null, ChannelActivity.getNoinetActionKey());
            }
        }
    }

    private void notificationReload(@NonNull final Intent intent) {
        if (ChannelActivity.isActivityRun) {
            final ArrayList<Parcelable> input = intent.getParcelableArrayListExtra(NOTIFICATION);
            for (final Parcelable message:input) {
                if (message instanceof Message) {
                    sendNotifyToBroadcast(((Message) message).getUrl()
                            , ((Message) message).getMessage());
                }
            }
        } else {
            //resultIntent.putParcelableArrayListExtra(NOTIFICATION, atomRssChannelHelper.getAllMessages());
            final Intent activityIntent = new Intent(this, ChannelActivity.class);
            activityIntent.setAction(ChannelActivity.getRefreshKey());
            activityIntent.putExtra(ChannelActivity.getRefreshKey()
                    , intent.getParcelableArrayListExtra(NOTIFICATION));
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(activityIntent);
        }
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
                if (checkConnection()) {
                    sendChannelToBroadcast(null, ChannelActivity.getIoExceptionActionKey());
                } else {
                    sendChannelToBroadcast(null, ChannelActivity.getNoinetActionKey());
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

    private Integer countMatches(final ArrayList<ChannelItem> input) {
        Integer result = 0;
        final Iterator<ChannelItem> iterator = input.iterator();
        while (iterator.hasNext()) {
            final ChannelItem current = iterator.next();
            iterator.remove();
            for (final ChannelItem item:input) {
                if (item.getLink().equals(current.getLink())) {
                    result++;
                }
            }
        }
        return result;
    }

	private void removeChannel(@NonNull final Intent intent) {
		final AtomRssChannelDbHelper channelDbHelper = new AtomRssChannelDbHelper(this);
		final Channel inputChannel = intent.getParcelableExtra(REMOVE_CHANNEL);
		channelDbHelper.deleteChannelFromDb(inputChannel);
		sendChannelToBroadcast(inputChannel, REMOVE_CHANNEL);
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
                sendChannelToBroadcast(channelFromUrl, action);
            } else {
                channelDbHelper.writeChannelToDb(channelFromUrl);
                sendChannelToBroadcast(channelFromUrl, action);
            }

        } catch (final IOException | XmlPullParserException e) {
            if (checkConnection()) {
                sendChannelToBroadcast(null, ChannelActivity.getIoExceptionActionKey());
            } else {
                sendChannelToBroadcast(null, ChannelActivity.getNoinetActionKey());
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

    private void readCurrentChannelDb(@NonNull final Intent intent, @Nullable final String action) {
		final Channel inputChannel = intent.getParcelableExtra(READ_CURRENT_CHANNEL);
		if (!inputChannel.isRead()) {
			final AtomRssChannelDbHelper channelDbHelper = new AtomRssChannelDbHelper(this);
			channelDbHelper.updateValueFromDb(AtomRssDataBase.ChannelEntry.TABLE_NAME, AtomRssDataBase.ChannelEntry.CHANNEL_IS_READ
					, Long.toString(channelDbHelper.boolToLong(true))
					, AtomRssDataBase.ChannelEntry.CHANNEL_LINK, inputChannel.getLink());
			inputChannel.setRead(true);

			sendChannelToBroadcast(inputChannel, action);
		}
	}

	private void readChannelsFromDb(@Nullable final String action) {
		final AtomRssChannelDbHelper channelDbHelper = new AtomRssChannelDbHelper(this);
		final ArrayList<Channel> channels = channelDbHelper.readAllChannelsFromDb();

		for (final Channel channel:channels) {
			sendChannelToBroadcast(channel, action);
		}
	}

	private void sendChannelToBroadcast(@Nullable final Channel channel, @Nullable final String action) {
		final Intent broadcastIntent = new Intent(ChannelBroadcastReceiver.getReceiveChannelsKey());
		broadcastIntent.setPackage(getPackageName());
		broadcastIntent.putExtra(ChannelBroadcastReceiver.getReceiveChannelsKey(), channel);
        broadcastIntent.putExtra(ChannelBroadcastReceiver.getMessageKey(), action);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
	}

    private void sendNotifyToBroadcast(@NonNull final String url, @NonNull final String message) {
        final Intent broadcastIntent = new Intent(ChannelBroadcastReceiver.getRefreshDialogKey());
        broadcastIntent.setPackage(getPackageName());
        broadcastIntent.putExtra(ChannelBroadcastReceiver.getChannelUrlKey(), url);
        broadcastIntent.putExtra(ChannelBroadcastReceiver.getMessageKey(), message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
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

        final AtomRssChannelDbHelper atomRssChannelHelper = new AtomRssChannelDbHelper(this);
        for (final Channel key:messages.keySet()) {
            final Message currentMessage = new Message(key.getLink()
                    , makeTextFromChannel(key.getTitle(), messages.get(key)));
            atomRssChannelHelper.saveMessage(currentMessage);
        }
        final Intent resultIntent = new Intent(this, RssChannelIntentService.class);
        resultIntent.setAction(NOTIFICATION);
        resultIntent.putParcelableArrayListExtra(NOTIFICATION, atomRssChannelHelper.getAllMessages());
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
}
