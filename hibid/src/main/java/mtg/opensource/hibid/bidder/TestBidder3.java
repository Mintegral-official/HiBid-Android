/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package mtg.opensource.hibid.bidder;

import android.content.Context;
import android.text.TextUtils;

import mtg.opensource.hibid.Bidder;
import mtg.opensource.hibid.callback.BiddingCallback;
import mtg.opensource.hibid.constants.ADType;
import mtg.opensource.hibid.data.AuctionNotification;
import mtg.opensource.hibid.data.BidRequestInfo;
import mtg.opensource.hibid.data.BiddingResponse;
import mtg.opensource.hibid.exception.BidderInitFailedException;
import mtg.opensource.hibid.exception.BiddingException;
import mtg.opensource.hibid.exception.FailedToGetRenderException;


/**
 * Test bidder adapter
 */
public class TestBidder3 implements Bidder {

    private static volatile boolean sdkInitialized = false;

    private Context mContext;


    @Override
    public Class getBidderClass() {
        return TestBidder3.class;
    }

    @Override
    public void init(Context context) throws BidderInitFailedException {
        try {
            mContext = context;
            if (!sdkInitialized) {
                sdkInitialized = true;
                //TODO: SDK Init
            }
        }catch (Exception ex){
            throw new BidderInitFailedException("TestBidder3 init failed", ex.getCause());
        }
    }


    @Override
    public void bid(BidRequestInfo bidRequestInfo, String adType, int timeOutMS,
                    final BiddingCallback callBack) throws BiddingException {

        if(bidRequestInfo == null || mContext == null ){
            throw new BiddingException("TestBidder3: bidRequestInfo == null || context == null");
        }

        if (TextUtils.isEmpty(bidRequestInfo.getAppId()) || TextUtils.isEmpty(bidRequestInfo.getPlacementId())){
            throw new BiddingException("TestBidder3: appId == null || placementId == null");
        }

        Object adBidFormat = getAdBidFormat(adType);
        if (adBidFormat == null){
            BiddingResponse biddingResponse = new BiddingResponse(TestBidder3.class,
                    TestBidder3.this, "Unsupported TestBidder3 AD format!");
            if (callBack != null) {
                callBack.onBiddingResponse(biddingResponse);
                return;
            }
        }

        try {
            Thread.sleep(8 * 1000);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        BiddingResponse biddingResponse =new BiddingResponse(TestBidder3.class,
                30.0, "TestBidder3-payload", TestBidder3.this);
        if (callBack != null) {
            callBack.onBiddingResponse(biddingResponse);
        }
    }

    @Override
    public void onAuctionNotification(AuctionNotification notification) {

    }

    @Override
    public Object getAdsRender() throws FailedToGetRenderException {
        return null;
    }

    @Override
    public Object getAdBidFormat(String adType) {
        Object adBidFormat = null;
        switch (adType){
            case ADType.INTERSTITIAL:
            case ADType.NATIVE:
            case ADType.REWARDED_VIDEO:{
                adBidFormat = new Object();
                break;
            }
            default:{
                break;
            }
        }
        return adBidFormat;
    }
}

