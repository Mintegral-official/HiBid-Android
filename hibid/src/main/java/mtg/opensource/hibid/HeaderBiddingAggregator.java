/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package mtg.opensource.hibid;

import android.content.Context;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mtg.opensource.hibid.callback.BidRequestCallback;
import mtg.opensource.hibid.constants.Constants;
import mtg.opensource.hibid.data.BidRequestInfo;
import mtg.opensource.hibid.exception.BidRequestException;
import mtg.opensource.hibid.exception.BidderInitFailedException;

/**
 * Entrance class which init bidders and send bid request.
 */
public class HeaderBiddingAggregator {
	private static final String TAG = HeaderBiddingAggregator.class.getName();

	/**switch for developers to enable log or not*/
	private static boolean mIsDebugMode = false;

	private static ExecutorService executor = Executors.newCachedThreadPool();
	private static Context context;

	public static boolean isDebugMode() {
		return mIsDebugMode;
	}

	/**
	 * enable log or not
	 * @param isDebugMode
	 */
	public static void setDebugMode(boolean isDebugMode){
		mIsDebugMode = isDebugMode;
	}


    /**
     * passed in context
     * @param context
     */
	public static void init(final Context context){
		HeaderBiddingAggregator.context = context;
	}

	/**
	 *
	 * @param bidReqs bid request for openrtb
	 * @param timeOutMS time out in microseconds
	 * @param bidRequestCallback bid request callback
	 * @return
	 * @throws BidRequestException
	 */
	public static String requestBid(final List<BidRequestInfo> bidReqs,
									final String unitId, final String adType, int timeOutMS,
									BidRequestCallback bidRequestCallback ) throws BidRequestException {
		if (context == null){
			throw new BidRequestException("Context is null or empty!");
		}

		if (bidReqs == null || bidReqs.size() == 0){
			throw new BidRequestException("Bidders is null or empty!");
		}

		if (TextUtils.isEmpty(unitId.trim())){
			throw new BidRequestException("unitId is null or empty!");
		}

		if (TextUtils.isEmpty(adType.trim())){
			throw new BidRequestException("adType is null or empty!");
		}

		if (bidRequestCallback == null){
			throw new BidRequestException("bidRequestCallback is null");
		}

		if (timeOutMS <= 0){
			timeOutMS = Constants.DEFAULT_TIME_OUT_MS;
		}

		//new and init bidders
		Map<Bidder, BidRequestInfo> bidders = new HashMap<Bidder, BidRequestInfo>();
		try {
			for (int i = 0; i < bidReqs.size(); i++) {
				Class bidderClass = bidReqs.get(i).getBidderClass();
				if (bidderClass != null) {
					Object newInstance = bidderClass.newInstance();
					if (newInstance instanceof Bidder) {
						Bidder bidder = (Bidder)newInstance;
						bidder.init(context);
						bidders.put(bidder, bidReqs.get(i));
					}
				}

			}
		}catch (BidderInitFailedException ex){
			throw new BidRequestException(ex.getMessage());
		}catch (Exception ex){
			throw new BidRequestException(ex.getMessage());
		}


		//do runtime bidding
		HeaderBiddingTransaction transaction =
				new HeaderBiddingTransaction(executor, unitId, adType, bidRequestCallback);
		String transId = transaction.startTransaction(bidders, timeOutMS);

		return transId;

	}

}
