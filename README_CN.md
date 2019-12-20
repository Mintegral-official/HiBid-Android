
# HiBid 开源框架 Android 使用文档

[English](https://github.com/Mintegral-official/HiBid-Android/blob/master/README.md)

## 概要

`HiBid` 是一个聚合了多家支持 `head-bidding` 的主流平台的开源框架，可以支持快速、高效地实现多家平台 `head-bidding` 的功能。目前已经支持的 `Network` 有 `Facebook`、`Mintegral`。

本文档描述 `Android` 平台上如何集成并使用 `HiBid` 开源框架。

## 使用说明

### 对于初始化

在 `application` 里的如下方法中调用初始化方法：

```java
public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        HeaderBiddingAggregator.init(getApplicationContext());
    }
}
```

### 关于 Log

可以通过 `HeaderBiddingAggregator` 的 `setDebugMode()` 方法来打开开关：

```java
public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        HeaderBiddingAggregator.setDebugMode(true);//open log    }
}
```



### 对于如何开始竞价（以Facebook的bidder为例）

1. 初始化 `BidRequestInfo` 对象以及它的 `list` 集合:

```java
BidRequestInfo fb = new BidRequestInfo("your facebook appId",
                    "your facebook placementId", "your custom bidder adapter (class)",
                    "your facebook platformId", false);
                    List<BidRequestInfo> bidderReqs = new ArrayList<BidRequestInfo>();
            bidderReqs.add(fb);
```

2. 初始化 `BidRequestCallback` 对象:

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

3. 调用 `HeaderBiddingAggregator` 类的静态方法 `requestBid`:

```java
HeaderBiddingAggregator.requestBid(bidderReqs,
                    "your unitID","ad_type",10 * 1000, callback);
```


### 快速添加自定义 Bidder-adapter(以创建 Facebook 的 bidder 为例)

1. 在你的项目里创建一个类，并实现 `mtg.opensource.headerbidding.Bidder`。（可以参照开源项目里的 `FacebookBidder` 类）
2. 重载 `Bideer` 里的所有方法。

示例代码如下：

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

## FAQ
1. **开源项目目前支持哪几种广告形式？**

开源项目目前仅支持广告类型： `NATIVE`， `INTERSTITIAL`，`REWARDED_VIDEO`。但支持开发者自定义其他广告类型。

2. **竞价返回的价格是什么货币单位？**

竞价返回的价格必须是以美元计价的，开发者在自定义 `adapter` 的时候，确保其竞价价格单位是美元，不建议使用其他币种和自定义汇率转换。 

3. **出价相同的 `bidder` 如何排序？**

出价格相等的 `bidder`，先返回的`bidder`，排在最前面，默认排在前面的即为本次竞价的 `winner`。 

4. **若返回的竞价都是0代表什么**？ 

若返回的 `bidder` 价格都是0，默认本次竞价没有 `winner`。

5. **如果一家 bidder 竞价 fail 了，那么最终返回的是 success 还是 fail?** 

我们设置了 `10S` 请求超时，如果 `10s` 内，有一家 `bidder` 返回成功，则返回成功，其他失败的 `bidder` 的价格会计为0，并且会在 `reponse` 里的 `otherbidders` 列表里。

## Changelog

版本号 | ChangeLog | 发布时间
------|-----------|------
1.0.0 | HiBid开源框架发布| 2019.05.10

