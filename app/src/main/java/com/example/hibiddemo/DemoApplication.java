package com.example.hibiddemo;

import android.app.Application;

import mtg.opensource.hibid.HeaderBiddingAggregator;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        HeaderBiddingAggregator.init(getApplicationContext());
    }
}

