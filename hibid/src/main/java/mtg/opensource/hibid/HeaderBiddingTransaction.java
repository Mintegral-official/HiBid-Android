/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package mtg.opensource.hibid;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import mtg.opensource.hibid.callback.BidRequestCallback;
import mtg.opensource.hibid.callback.BiddingCallback;
import mtg.opensource.hibid.data.AuctionNotification;
import mtg.opensource.hibid.data.AuctionResult;
import mtg.opensource.hibid.data.BidRequestInfo;
import mtg.opensource.hibid.data.BiddingResponse;
import mtg.opensource.hibid.exception.AuctionResultException;
import mtg.opensource.hibid.exception.BiddingException;

/**
 * One transaction for one runtime bidding
 */
public class HeaderBiddingTransaction implements BiddingCallback {
    private static final String TAG = HeaderBiddingTransaction.class.getName();

    private String transId;
    private boolean isComplete = false;
    private Timer biddingTimer = new Timer();

    private ExecutorService executor;
    private String unitId;
    private String adType;
    private BidRequestCallback bidRequestCallback;

    /** Map: key: Bidder, value :BidRequestInfo*/
    private Map<Bidder, BidRequestInfo> bidders;
    /** Map: key: bidderID, value :BiddingResponse*/
    private Map<Class, BiddingResponse> bidResponses = new HashMap<Class, BiddingResponse>();


    public HeaderBiddingTransaction(ExecutorService executor, String unitId, String adType,
                                    BidRequestCallback bidRequestCallback) {
        this.transId = UUID.randomUUID().toString();
        this.executor = executor;
        this.unitId = unitId;
        this.adType = adType;
        this.bidRequestCallback = bidRequestCallback;
    }

    public String startTransaction(final Map<Bidder, BidRequestInfo> bidders, final int timeOutMS){

        this.bidders = bidders;

        LogUtil.i(TAG, " transId =" + transId + " started time = " + getCurrentTimeStamp());
        startBiddingTimer(timeOutMS);

        if (bidders != null && bidders.size() > 0) {
            for (final Map.Entry<Bidder, BidRequestInfo> entry : bidders.entrySet()) {
                final Bidder bidder = entry.getKey();
                if (bidder != null) {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                bidder.bid(entry.getValue(), adType, timeOutMS, HeaderBiddingTransaction.this);

                            } catch (BiddingException ex) {
                                LogUtil.e(TAG, bidder + " bidding failed " + ex.getMessage());
                            } catch (Exception ex) {
                                LogUtil.e(TAG, bidder + " bidding exception " + ex.getMessage());
                            }
                        }
                    });
                }
            }
        }

        return transId;
    }

    @Override
    public void onBiddingResponse(BiddingResponse response) {
        synchronized (this) {
            if (!isComplete) {
                if (response != null && bidResponses != null &&
                        !bidResponses.containsKey(response.getBidderClass())) {

                    bidResponses.put(response.getBidderClass(), response);

                    if (bidResponses.size() == bidders.size()) {
                        isComplete = true;

                        LogUtil.i(TAG, " transId =" + transId + " -->got all results, return auction result!");
                        onAuctionResult();
                    }
                }
            }
        }
    }


    private void startBiddingTimer(int timeOutMS) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                synchronized (this) {
                    if (!isComplete) {
                        isComplete = true;

                        LogUtil.i(TAG, " transId =" + transId + " --> time out, return auction result!");
                        onAuctionResult();
                    }
                }
            }
        };
        biddingTimer.schedule(task, timeOutMS + 2);
    }

    private String getCurrentTimeStamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS");

        String timeStamp =formatter.format(new Date());

        return timeStamp;
    }

    private void onAuctionResult(){
        LogUtil.i(TAG, " transId =" + transId + " ended time = " + getCurrentTimeStamp());

        if (bidResponses == null || bidResponses.size() == 0){
            onAuctionFail();
        }else {
            onAuctionSuccess();
        }
    }

    private void onAuctionFail(){
        //return auction exception
        bidRequestCallback.onFail(unitId, new AuctionResultException("No bidders returned!"));

        //notify all timeout
        AuctionNotification timeoutNotification =
                AuctionNotification.getAuctionNotification(AuctionNotification.ReasonCode.Timeout);
        if (bidders != null && bidders.size() > 0) {
            for (Map.Entry<Bidder, BidRequestInfo> bidderEntry : bidders.entrySet()) {
                Bidder bidder = bidderEntry.getKey();
                if (bidder != null) {
                    //notify loss with timeout
                    bidder.onAuctionNotification(timeoutNotification);
                }
            }
        }
    }

    private void onAuctionSuccess(){
        if (bidResponses != null && bidResponses.size() > 0) {
            //return auction result
            AuctionResult result = new AuctionResult();
            result.setTransactionId(transId);
            result.setUnitId(unitId);
            List<BiddingResponse> responseList = new ArrayList<BiddingResponse>();
            for(Map.Entry<Class, BiddingResponse> entry : bidResponses.entrySet()){
                responseList.add(entry.getValue());
            }
            Collections.sort(responseList);
            result.setBiddingResponses(responseList);
            bidRequestCallback.onSuccess(result);


            //notify win, loss, timeout
            AuctionNotification winNotification =
                    AuctionNotification.getAuctionNotification(AuctionNotification.ReasonCode.Win);
            AuctionNotification lowPriceNotification =
                    AuctionNotification.getAuctionNotification(AuctionNotification.ReasonCode.Loss);
            AuctionNotification timeoutNotification =
                    AuctionNotification.getAuctionNotification(AuctionNotification.ReasonCode.Timeout);

            Class winnerBidderClass = null;
            if(result.getWinner() != null){
                winnerBidderClass = result.getWinner().getBidderClass();
            }

            if (bidders != null && bidders.size() > 0) {
                for (Map.Entry<Bidder, BidRequestInfo> bidderEntry : bidders.entrySet()) {
                    Bidder bidder = bidderEntry.getKey();
                    if (bidder != null) {
                        if (!bidResponses.containsKey(bidder.getBidderClass())) {
                            bidResponses.put(bidder.getBidderClass(),
                                    new BiddingResponse(bidder.getBidderClass(),
                                            bidder, bidder.getBidderClass() + " time out"));
                            //notify loss with timeout
                            bidder.onAuctionNotification(timeoutNotification);
                        } else {
                            if (winnerBidderClass != null && winnerBidderClass.equals(bidder.getBidderClass())) {
                                //notify win
                                bidder.onAuctionNotification(winNotification);
                            } else {
                                //notify loss with lowprice
                                bidder.onAuctionNotification(lowPriceNotification);
                            }
                        }

                    }
                }
            }
        }
    }


}
