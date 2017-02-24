package com.spitchenko.focusstart;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.spitchenko.focusstart.adapters.ChannelRecyclerAdapter;
import com.spitchenko.focusstart.model.Channel;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private ChannelRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<Channel> channels = new ArrayList<>();
        Channel one = new Channel();
        one.setTitle("Yandex");
        try {
            one.setImage(new URL("https://company.yandex.ru/i/50x23.gif"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        one.setSubtitle("News ololo");
        channels.add(one);

        mRecyclerView = (RecyclerView) findViewById(R.id.activity_main_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ChannelRecyclerAdapter(channels);
        mRecyclerView.setAdapter(mAdapter);

    }
}
