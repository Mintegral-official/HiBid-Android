
# Documentation of HiBid open-source framework in Android 
[中文](https://github.com/Mintegral-official/HiBid-Android/blob/master/README_CN.md)


## Overview

HiBid is an open source framework that aggregates multiple mainstream platforms that support head-bidding. Developers can quickly and efficiently implement head-bidding services of multiple platforms by using the HiBid open-source framework. Currently, the Network it already supports are Facebook and Mintegral.<br/>
This document describes how to integrate and integrate the HiBid framework on the Android platform.



## How to use

### Initialize the HiBid SDK

Please initialize in application.


```java
public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        HeaderBiddingAggregator.init(getApplicationContext());
    }
}
```

### Log Setting

The setDebugMode method can control Debug mode.You can set true to check logs.


```java
public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        HeaderBiddingAggregator.setDebugMode(true);//open log
    }
}
```



### Start to bid(For example, Using a Facebook bidder)

1.Initialize the object of BidRequestInfo and its list.

```java
BidRequestInfo fb = new BidRequestInfo("your facebook appId",
                    "your facebook placementId", "your custom bidder adapter (class)",
                    "your facebook platformId", false);
                    List<BidRequestInfo> bidderReqs = new ArrayList<BidRequestInfo>();
            bidderReqs.add(fb);
```
2.Initialize the BidRequestCallback object.

```java
 BidRequestCallback callback = new BidRequestCallback(){
                @Override
                public void onSuccess(final AuctionResult auctionResult) {
                //bidding success
                    LogUtil.i(TAG, "unitId = " + auctionResult.getUnitId() + " return success!");

                }

                @Override
                public void onFail(String unitId, final Throwable e) {
                //bidding fail
                    LogUtil.i(TAG, "unitId = " + unitId + " return fail!");
                   
                }
            };
```
3.Call requestBid method for the bidding.

```java
HeaderBiddingAggregator.requestBid(bidderReqs,
                    "your unitID","ad_type",10 * 1000, callback);
```




### Custom Bidder-adapter(For example, Creating a Facebook bidder)

1.Create a subclass of mtg.opensource.headerbidding.Bidder in your project.（You can refer to the FacebookBidder.java）<br/>
2.Override all the methods.<br/>



Sample code：



```java
public class FacebookBidder implements Bidder {
    private static final String TAG = FacebookBidder.class.getSimpleName();

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
                LogUtil.i(TAG, "Facebook Bidder Wins");
                bidResponse.notifyWin();
            } else {
                LogUtil.i(TAG, "Facebook Bidder Loss");
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
```



##FAQ
1.**What kinds of advertising formats are currently supported by HiBid?**<br/>
Currently，The ad types we supported: Native,Interstitial RewardVideo. But developers are allowed to customize other ad types.<br/>
2.**What currency is the price returned by the auction?**<br/>
The price returned by the auction is in US dollars. When the developer customizes the adapter, Make sure the price is in US dollars and it is not recommended to use other currencies and exchange rate conversions.<br/>
3.**How does it order the same bidder?**<br/>
If the bidders with the same prices, the one who returned first, is ranked first,and also is the winner.<br/>
4.**If the returned bid is 0, what does it mean?**<br/>
If the returned price is 0, so there is no winner in this auction.<br/>
5.**If there is a bidder failed, then the final response is success or fail?**<br/>
We have a 10S request-timeout. If one bidder returns successfully within 10s, it will return success. And the price of other failed bidders is 0, and they will be in the otherbidders list.

##changelog
Version | ChangeLog | Date
------|-----------|------
1.0.0 | HiBid open source release| 2019.05.10

