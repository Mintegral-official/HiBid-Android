/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package mtg.opensource.hibid.bidder;

import android.content.Context;
import android.text.TextUtils;

import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.NativeAd;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.bidding.FBAdBidFormat;
import com.facebook.bidding.FBAdBidRequest;
import com.facebook.bidding.FBAdBidResponse;

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
 * Facebook bidder adapter
 */
public class FacebookBidder implements Bidder {

    private static volatile boolean sdkInitialized = false;

    private Context mContext;
    private FBAdBidFormat adBidFormat;
    private FBAdBidResponse bidResponse;

    @Override
    public Class getBidderClass() {
        return FacebookBidder.class;
    }

    @Override
    public void init(Context context) throws BidderInitFailedException{
        try {
            mContext = context;
            if (!sdkInitialized) {
                sdkInitialized = true;
                AudienceNetworkAds.initialize(context);
            }
        }catch (Exception ex){
            throw new BidderInitFailedException("Facebook Bidder init failed", ex.getCause());
        }

    }

    @Override
    public void bid(BidRequestInfo bidRequestInfo, String adType, int timeOutMS,
                    final BiddingCallback callBack) throws BiddingException {

        if(bidRequestInfo == null || mContext == null ){
            throw new BiddingException("facebook: bidRequestInfo == null || context == null");
        }

        if (TextUtils.isEmpty(bidRequestInfo.getAppId()) || TextUtils.isEmpty(bidRequestInfo.getPlacementId())){
            throw new BiddingException("facebook: appId == null || placementId == null");
        }

        Object object = getAdBidFormat(adType);
        if (object != null){
            adBidFormat = (FBAdBidFormat)object;
        }else {
            BiddingResponse biddingResponse = new BiddingResponse(FacebookBidder.class,
                    FacebookBidder.this, "Unsupported facebook AD format!");
            if (callBack != null) {
                callBack.onBiddingResponse(biddingResponse);
                return;
            }
        }

        FBAdBidRequest bidRequest = new FBAdBidRequest(
                mContext,
                bidRequestInfo.getAppId(),
                bidRequestInfo.getPlacementId(),
                adBidFormat)
                .withPlatformId(bidRequestInfo.getPlatformId())
                .withTimeoutMS(timeOutMS)
                .withTestMode(bidRequestInfo.isTest());

        bidRequest.getFBBid(new FBAdBidRequest.BidResponseCallback() {
            @Override
            public void handleBidResponse(final FBAdBidResponse bidResponse) {
                if (bidResponse != null) {
                    FacebookBidder.this.bidResponse = bidResponse;

                    //Currency exchange， to US dollar
                    BiddingResponse biddingResponse;
                    if(bidResponse.isSuccess()){
                        biddingResponse = new BiddingResponse(FacebookBidder.class,
                                bidResponse.getPrice(), bidResponse.getPayload(), FacebookBidder.this);
                    }else {
                        biddingResponse = new BiddingResponse(FacebookBidder.class,
                                FacebookBidder.this, bidResponse.getErrorMessage());
                    }
                    if (callBack != null) {
                        callBack.onBiddingResponse(biddingResponse);
                    }
                }
            }
        });
    }

    @Override
    public void onAuctionNotification(AuctionNotification notification) {
        if (bidResponse != null) {
            if (notification.isWinner()) {
                bidResponse.notifyWin();
            } else {
                bidResponse.notifyLoss();
            }
        }

    }

    @Override
    public Object getAdsRender() throws FailedToGetRenderException {
        if (adBidFormat == null){
            throw new FailedToGetRenderException("Unsupported facebook AD format!");
        }

        Object adsRender = null;
        switch (adBidFormat){
            case NATIVE:{
                adsRender = new NativeAd(mContext, bidResponse.getPlacementId());
                break;
            }
            case INTERSTITIAL:{
                adsRender = new InterstitialAd(mContext, bidResponse.getPlacementId());
                break;
            }
            case REWARDED_VIDEO:{
                adsRender = new RewardedVideoAd(mContext, bidResponse.getPlacementId());
                break;
            }
            default:
                break;
        }
        return adsRender;
    }

    @Override
    public Object getAdBidFormat(String adType) {
        FBAdBidFormat fbAdBidFormat = null;
        switch (adType){
            case ADType.INTERSTITIAL:{
                fbAdBidFormat = FBAdBidFormat.INTERSTITIAL;
                break;
            }
            case ADType.NATIVE:{
                fbAdBidFormat = FBAdBidFormat.NATIVE;
                break;
            }
            case ADType.REWARDED_VIDEO:{
                fbAdBidFormat = FBAdBidFormat.REWARDED_VIDEO;
                break;
            }
            default:{
                break;
            }
        }
        return fbAdBidFormat;
    }
}
