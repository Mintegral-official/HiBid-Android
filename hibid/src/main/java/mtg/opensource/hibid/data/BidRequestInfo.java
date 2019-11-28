/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package mtg.opensource.hibid.data;

/**
 * Reqeust info for one bidder
 */
public class BidRequestInfo {

    private String appId;
    private String placementId;
    private Class bidderClass;
    private String platformId;
    private boolean isTest;

    public BidRequestInfo(String appId, String placementId, Class bidderClass){
        this.appId = appId;
        this.placementId = placementId;
        this.bidderClass = bidderClass;
    }

    public BidRequestInfo(String appId, String placementId, Class bidderClass, String platformId, boolean isTest){
        this.appId = appId;
        this.placementId = placementId;
        this.bidderClass = bidderClass;
        this.platformId = platformId;
        this.isTest = isTest;
    }

    public String getAppId() {
        return appId;
    }

    public String getPlacementId() {
        return placementId;
    }

    public Class getBidderClass() {
        return bidderClass;
    }

    public String getPlatformId() {
        return platformId;
    }

    public boolean isTest() {
        return isTest;
    }
}
