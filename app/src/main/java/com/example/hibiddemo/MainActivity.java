package com.example.hibiddemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mtg.opensource.hibid.HeaderBiddingAggregator;
import mtg.opensource.hibid.HeaderBiddingTransaction;
import mtg.opensource.hibid.LogUtil;
import mtg.opensource.hibid.bidder.FacebookBidder;
import mtg.opensource.hibid.bidder.MtgBidder;
import mtg.opensource.hibid.callback.BidRequestCallback;
import mtg.opensource.hibid.constants.ADType;
import mtg.opensource.hibid.data.AuctionResult;
import mtg.opensource.hibid.data.BidRequestInfo;
import mtg.opensource.hibid.data.BiddingResponse;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getName();

    private TextView tvBidResult;
    private Button bidButton;
    private RecyclerView biddersRecycle;

    private List<BidderBean> mBidders = new ArrayList<>();
    private BidderRecyclerViewAdapter mBidderAdapter;

    private List<HeaderBiddingTransaction> transactionList = new ArrayList<HeaderBiddingTransaction>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        biddersRecycle = (RecyclerView)findViewById(R.id.bidders_recycle);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        biddersRecycle.setLayoutManager(layoutManager);
        biddersRecycle.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mBidderAdapter = new BidderRecyclerViewAdapter(this, mBidders);
        biddersRecycle.setAdapter(mBidderAdapter);

        tvBidResult = (TextView) findViewById(R.id.bid_result);

        bidButton = (Button) findViewById(R.id.bid_for_ad);
        bidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBidButtonClick();
            }
        });

    }

    private void resetBidStatus(){
        mBidders.clear();
        mBidderAdapter.notifyDataSetChanged();

        tvBidResult.setText("Now, Bidding...");
    }

    private void onBidButtonClick(){
        resetBidStatus();


        /**---------start--using bidding framework------------------*/
        final String unitId = "unitid";
        try {

            //prepare bidder request info
            BidRequestInfo fb = new BidRequestInfo();
            fb.put(BidRequestInfo.KEY_APP_ID, getString(R.string.facebook_app_id));
            fb.put(BidRequestInfo.KEY_PLACEMENT_ID, getString(R.string.facebook_native_placement_id));
            fb.put(BidRequestInfo.KEY_BIDDER_CLASS, FacebookBidder.class);
            fb.put(BidRequestInfo.KEY_PLATFORM_ID, getString(R.string.facebook_app_id));
            fb.put("isTest", true);

            BidRequestInfo fb1 = new BidRequestInfo();
            fb1.put(BidRequestInfo.KEY_APP_ID, getString(R.string.facebook_app_id));
            fb1.put(BidRequestInfo.KEY_PLACEMENT_ID, getString(R.string.facebook_interstitial_placement_id));
            fb1.put(BidRequestInfo.KEY_BIDDER_CLASS, FacebookBidder.class);
            fb1.put(BidRequestInfo.KEY_PLATFORM_ID, getString(R.string.facebook_app_id));
            fb1.put("isTest", true);

            BidRequestInfo mtg = new BidRequestInfo();
            mtg.put(BidRequestInfo.KEY_APP_ID, getString(R.string.mtg_app_id));
            mtg.put(BidRequestInfo.KEY_APP_KEY, getString(R.string.mtg_app_key));
            mtg.put(BidRequestInfo.KEY_PLACEMENT_ID, getString(R.string.mtg_placement_id));
            mtg.put(BidRequestInfo.KEY_BIDDER_CLASS, MtgBidder.class);

            List<BidRequestInfo> bidderReqs = new ArrayList<BidRequestInfo>();
            bidderReqs.add(fb);
            bidderReqs.add(fb1);
            bidderReqs.add(mtg);

            //send request
            HeaderBiddingTransaction transaction = HeaderBiddingAggregator.requestBid(bidderReqs,
                    unitId, ADType.INTERSTITIAL,2000, callback);
            transactionList.add(transaction);

        }catch (final Exception ex){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tvBidResult.setText(ex.getMessage());
                    mBidders.clear();
                    mBidderAdapter.notifyDataSetChanged();
                }
            }, 20);
        }

        /**---------end----using bidding framework------------------*/


    }

    private BidRequestCallback callback = new BidRequestCallback(){

        @Override
        public void onBidRequestCallback(String unitId, final AuctionResult auctionResult) {
            LogUtil.i(TAG, "unitId = " + auctionResult.getUnitId() + " return result!");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (auctionResult != null) {
                        if (auctionResult.getWinner() != null) {
                            tvBidResult.setText(auctionResult.getWinner().getBidderClass().getSimpleName() + " Wins");
                        } else {
                            tvBidResult.setText("No Bidder Wins");
                        }

                        mBidders.clear();
                        if (auctionResult.getWinner() != null) {
                            BidderBean bidderBean = new BidderBean();
                            bidderBean.setBidderName(auctionResult.getWinner().getBidderClass().getSimpleName());
                            bidderBean.setBidderPriceUSD(auctionResult.getWinner().getBiddingPriceUSD());
                            mBidders.add(bidderBean);
                        }
                        List<BiddingResponse> otherBidders = auctionResult.getOtherBidders();
                        for (BiddingResponse biddingResponse : otherBidders) {
                            BidderBean bidderBean = new BidderBean();
                            bidderBean.setBidderName(biddingResponse.getBidderClass().getSimpleName());
                            bidderBean.setBidderPriceUSD(biddingResponse.getBiddingPriceUSD());
                            bidderBean.setErrMessage(biddingResponse.getErrorMessage());
                            mBidders.add(bidderBean);
                        }
                        mBidderAdapter.notifyDataSetChanged();
                    }else {
                        tvBidResult.setText("No Bidder Wins");
                        mBidders.clear();
                        mBidderAdapter.notifyDataSetChanged();
                    }

                }
            });

        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();

        for(int i=0; i < transactionList.size(); i++){
            transactionList.get(i).cancelTimer();
        }
    }
}
