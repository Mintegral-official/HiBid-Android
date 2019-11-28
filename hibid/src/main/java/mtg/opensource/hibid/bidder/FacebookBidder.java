/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package mtg.opensource.hibid.bidder;

import android.text.TextUtils;

import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.NativeAd;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.bidding.FBAdBidFormat;
import com.facebook.bidding.FBAdBidRequest;
import com.facebook.bidding.FBAdBidResponse;

import mtg.opensource.hibid.Bidder;
import mtg.opensource.hibid.LogUtil;
import mtg.opensource.hibid.callback.BiddingCallback;
import mtg.opensource.hibid.constants.ADType;
import mtg.opensource.hibid.data.AuctionNotification;
import mtg.opensource.hibid.data.BidRequestInfo;
import mtg.opensource.hibid.data.BiddingResponse;
import mtg.opensource.hibid.data.HiBidContext;
import mtg.opensource.hibid.exception.BidderInitFailedException;
import mtg.opensource.hibid.exception.BiddingException;
import mtg.opensource.hibid.exception.FailedToGetRenderException;
import mtg.opensource.hibid.exception.SdkIntegratedException;

/**
 * Facebook bidder adapter
 */
public class FacebookBidder implements Bidder {
    private static final String TAG = FacebookBidder.class.getSimpleName();

    private static volatile boolean sdkInitialized = false;

    private HiBidContext mContext;
    private FBAdBidFormat adBidFormat;
    private FBAdBidResponse curBidResponsed = null;
    private BidRequestInfo curBidRequestInfo = null;

    @Override
    public Class getBidderClass() {
        return FacebookBidder.class;
    }

    @Override
    public BidRequestInfo getBidderRequestInfo() {
        return curBidRequestInfo;
    }

    @Override
    public void init(HiBidContext context) throws BidderInitFailedException, SdkIntegratedException{
        try {
            mContext = context;
            if (!sdkInitialized) {
                AudienceNetworkAds.initialize(mContext.getContext());
                sdkInitialized = true;
            }
        }catch (Exception ex){
            throw new BidderInitFailedException("Facebook Bidder init failed", ex.getCause());
        }catch (NoClassDefFoundError ex){
            throw new SdkIntegratedException("Facebook sdk not integrated!", ex.getCause());
        }

    }

    @Override
    public void bid(final BidRequestInfo bidRequestInfo, String adType, int timeOutMS,
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
                    "Unsupported facebook AD format!",FacebookBidder.this, bidRequestInfo);
            if (callBack != null) {
                callBack.onBiddingResponse(biddingResponse);
                return;
            }
        }

        curBidRequestInfo = bidRequestInfo;
        Object isTestObj = bidRequestInfo.get("isTest");
        boolean isTest = isTestObj != null ? (boolean) isTestObj  : false;

        FBAdBidRequest bidRequest = new FBAdBidRequest(
                mContext.getContext(),
                curBidRequestInfo.getAppId(),
                curBidRequestInfo.getPlacementId(),
                adBidFormat)
                .withPlatformId(curBidRequestInfo.getPlatformId())
                .withTimeoutMS(timeOutMS)
                .withTestMode(isTest);

        bidRequest.getFBBid(new FBAdBidRequest.BidResponseCallback() {
            @Override
            public void handleBidResponse(final FBAdBidResponse bidResponse) {
                BiddingResponse biddingResponse;
                if (bidResponse != null) {
                    FacebookBidder.this.curBidResponsed = bidResponse;

                    if(bidResponse.isSuccess()){
                        /**Currency exchange， to US dollar*/
                        biddingResponse = new BiddingResponse(FacebookBidder.class,
                                bidResponse.getPrice(), bidResponse.getPayload(), FacebookBidder.this, curBidRequestInfo);
                    }else {
                        biddingResponse = new BiddingResponse(FacebookBidder.class,
                                bidResponse.getErrorMessage(),FacebookBidder.this, curBidRequestInfo);
                    }
                }else {
                    biddingResponse = new BiddingResponse(FacebookBidder.class,
                            "Facebook bid response is NULL",FacebookBidder.this, curBidRequestInfo);
                }
                if (callBack != null) {
                    callBack.onBiddingResponse(biddingResponse);
                }
            }
        });
    }

    @Override
    public void onAuctionNotification(AuctionNotification notification) {
        if (curBidResponsed != null && mContext != null) {
            if (notification.isWinner()) {
                LogUtil.i(TAG, "Facebook Bidder Wins");
                curBidResponsed.notifyWin();
            } else {
                LogUtil.i(TAG, "Facebook Bidder Loss");
                curBidResponsed.notifyLoss();
            }
        }

    }

    @Override
    public Object getAdsRender() throws FailedToGetRenderException {
        if (adBidFormat == null){
            throw new FailedToGetRenderException("Unsupported FACEBOOK AD format!");
        }

        if (mContext == null){
            throw new FailedToGetRenderException("HiBidContext == NULL!");
        }

        Object adsRender = null;
        switch (adBidFormat){
            case NATIVE:{
                adsRender = new NativeAd(mContext.getContext(), curBidResponsed.getPlacementId());
                break;
            }
            case INTERSTITIAL:{
                adsRender = new InterstitialAd(mContext.getContext(), curBidResponsed.getPlacementId());
                break;
            }
            case REWARDED_VIDEO:{
                adsRender = new RewardedVideoAd(mContext.getContext(), curBidResponsed.getPlacementId());
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
            case ADType.NATIVE:{
                fbAdBidFormat = FBAdBidFormat.NATIVE;
                break;
            }
            case ADType.INTERSTITIAL:{
                fbAdBidFormat = FBAdBidFormat.INTERSTITIAL;
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
