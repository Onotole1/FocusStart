package com.spitchenko.focusstart.asynctasks;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.spitchenko.focusstart.model.NewsModule;
import com.spitchenko.focusstart.parser.RssChannelParser;

/**
 * Date: 24.02.17
 * Time: 21:52
 *
 * @author anatoliy
 */
public final class RssChannelAsyncTask extends AsyncTask<URL, Void, Void> {

	private Handler mHandler;
	private List<NewsModule> mChannels;

	public RssChannelAsyncTask(final Handler handler) {
		mHandler = handler;
		mChannels = new ArrayList<>();
	}

	@Override
	protected Void doInBackground(URL... params) {
		RssChannelParser rssChannelParser = new RssChannelParser();
		for (URL singleUrl : params) {
			mChannels.add(rssChannelParser.parseXml(singleUrl));
		}


		final Message msg = new Message();
		msg.obj = mChannels;
		mHandler.sendMessage(msg);
		return null;
	}
}
