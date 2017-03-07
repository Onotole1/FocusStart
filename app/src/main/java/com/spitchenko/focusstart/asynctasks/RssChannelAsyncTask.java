package com.spitchenko.focusstart.asynctasks;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.spitchenko.focusstart.model.NewsModule;
import com.spitchenko.focusstart.parser.AtomRssParser;

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
		AtomRssParser atomRssParser = new AtomRssParser();
		for (URL singleUrl : params) {
			synchronized (this) {
				mChannels.add(atomRssParser.parseXml(singleUrl));
			}
		}


		final Message msg = new Message();
		msg.obj = mChannels;
		mHandler.sendMessage(msg);
		return null;
	}
}
