package com.spitchenko.focusstart;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.spitchenko.focusstart.adapters.ChannelRecyclerAdapter;
import com.spitchenko.focusstart.asynctasks.RssChannelAsyncTask;
import com.spitchenko.focusstart.model.Channel;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private ChannelRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private final Handler handler = new Handler(newHandlerBurnerViewCallback());
    private List<Channel> channels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RssChannelAsyncTask rssChannelAsyncTask = new RssChannelAsyncTask(handler);
        try {
            rssChannelAsyncTask.execute(new URL("https://news.yandex.ru/index.rss")
                    , new URL("http://www.xn--atemschutzunflle-7nb.de/asu.xml")
		            /*, new URL("http://feeds.pcworld.com/pcworld/latestnews")
		            , new URL("https://news.rambler.ru/rss/head/")
		            , new URL("https://lenta.ru/rss/news")*/);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.activity_main_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

    }

    private Handler.Callback newHandlerBurnerViewCallback() {
        return new Handler.Callback() {
            @Override
            public boolean handleMessage(android.os.Message msg) {
                channels = ((List<Channel>) msg.obj);
	            mAdapter = new ChannelRecyclerAdapter(channels);
	            mRecyclerView.setAdapter(mAdapter);
                return true;
            }
        };
    }
}
