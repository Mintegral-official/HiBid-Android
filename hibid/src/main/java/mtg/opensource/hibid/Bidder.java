/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package mtg.opensource.hibid;

import android.content.Context;

import mtg.opensource.hibid.callback.BiddingCallback;
import mtg.opensource.hibid.data.AuctionNotification;
import mtg.opensource.hibid.data.BidRequestInfo;
import mtg.opensource.hibid.exception.BidderInitFailedException;
import mtg.opensource.hibid.exception.BiddingException;
import mtg.opensource.hibid.exception.FailedToGetRenderException;

public interface Bidder {

	/**
	 * return Bidder Class
	 * @return
	 */
	Class getBidderClass();

	/**
	 *  bidder sdk init
	 * @param biddingContext
	 * @throws BidderInitFailedException
	 */
	void init(Context biddingContext) throws BidderInitFailedException;

	/**
	 * bid request
	 * @param bidRequestInfo request parameters
	 * @param adType advertisement type
	 * @param timeOutMS time out in milliseconds
	 * @param callBack bid request callback
	 * @throws BiddingException
	 */
	void bid(BidRequestInfo bidRequestInfo,
			 String adType, int timeOutMS, BiddingCallback callBack) throws BiddingException;

	/**
	 * bid request notification
	 * @param notification
	 */
	void onAuctionNotification(AuctionNotification notification);

	/**
	 * return ad object
	 * @return
	 * @throws FailedToGetRenderException
	 */
	Object getAdsRender() throws FailedToGetRenderException;

	/**
	 * return ad format, eg. facebook ad format, mtg ad format...
	 * @param adType
	 * @return
	 */
	Object getAdBidFormat(String adType);
}
