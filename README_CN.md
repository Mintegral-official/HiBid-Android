
# HiBid开源框架Android使用文档


## 概要

HiBid是一个聚合了多家支持head-bidding的主流平台的开源框架，可以支持快速、高效地实现多家平台head-bidding的功能。目前已经支持的Network有Facebook、Mintegral。<br/>
本文档描述Android平台上如何集成并使用HiBid开源框架。




## 使用说明

### 对于初始化

在application里的如下方法中调用初始化方法。<br/>

```java
public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
HeaderBiddingAggregator.init(getApplicationContext());
    }
}
```

### 关于Log
可以通过HeaderBiddingAggregator的setDebugMode()方法来打开开关。

```java
public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        HeaderBiddingAggregator.setDebugMode(true);//open log    }
}
```



### 对于如何开始竞价（以Facebook的bidder为例）

1、初始化BidRequestInfo的对象以及它的list集合。

```java
BidRequestInfo fb = new BidRequestInfo("your facebook appId",
                    "your facebook placementId", "your custom bidder adapter (class)",
                    "your facebook platformId", false);
                    List<BidRequestInfo> bidderReqs = new ArrayList<BidRequestInfo>();
            bidderReqs.add(fb);
```
2、初始化BidRequestCallback对象。

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
3、调用HeaderBiddingAggregator类的静态方法requestBid。

```java
HeaderBiddingAggregator.requestBid(bidderReqs,
                    "your unitID","ad_type",10 * 1000, callback);
```


### 快速添加自定义Bidder-adapter(以创建Facebook的bidder为例)

1、在你的项目里创建一个类，并实现mtg.opensource.headerbidding.Bidder。（可以参照开源项目里的FacebookBidder类）<br/>
2、重载Bideer里的所有方法。



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




##FAQ
1.**开源项目目前支持哪几种广告形式？**<br/>
开源项目目前仅支持广告类型： NATIVE， INTERSTITIAL，REWARDED_VIDEO。但支持开发者自定义其他广告类型。<br/>
2.**竞价返回的价格是什么货币单位？**<br/>
竞价返回的价格必须是以美元计价的，开发者在自定义adapter的时候，确保其竞价价格单位是美元，不建议使用其他币种和自定义汇率转换。<br/> 
3.**出价相同的bidder如何排序？**<br/>
出价格相等的bidder，先返回的bidder,排在最前面,默认排在前面的即为本次竞价的winner。<br/> 
4.**若返回的竞价都是0代表什么**？<br/> 
若返回的bidder价格都是0，默认本次竞价没有winner。 <br/>
5.**如果一家bidder竞价fail了，那么最终返回的是success还是fail?**<br/> 
我们设置了10S请求超时，如果10s内，有一家bidder返回成功，则返回成功，其他失败的bidder的价格会计为0，并且会在reponse里的otherbidders列表里。

##Changelog
版本号 | ChangeLog | 发布时间
------|-----------|------
1.0.0 | HiBid开源框架发布| 2019.05.10

