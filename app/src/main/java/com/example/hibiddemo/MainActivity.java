package com.example.hibiddemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mtg.opensource.hibid.HeaderBiddingAggregator;
import mtg.opensource.hibid.LogUtil;
import mtg.opensource.hibid.bidder.FacebookBidder;
import mtg.opensource.hibid.bidder.TestBidder3;
import mtg.opensource.hibid.bidder.TestBidder1;
import mtg.opensource.hibid.bidder.TestBidder2;
import mtg.opensource.hibid.callback.BidRequestCallback;
import mtg.opensource.hibid.constants.ADType;
import mtg.opensource.hibid.data.AuctionResult;
import mtg.opensource.hibid.data.BidRequestInfo;
import mtg.opensource.hibid.data.BiddingResponse;
import mtg.opensource.hibid.exception.BidRequestException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();

    private TextView tvBidResult;
    private Button bidButton;
    private RecyclerView biddersRecycle;

    private List<BidderBean> mBidders = new ArrayList<>();
    private BidderRecyclerViewAdapter mBidderAdapter;

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
        try {

            //prepare bidder request info
            BidRequestInfo fb = new BidRequestInfo(MainActivity.this.getString(R.string.app_id),
                    MainActivity.this.getString(R.string.interstitial_placement_id), FacebookBidder.class,
                    MainActivity.this.getString(R.string.app_id), true);
            BidRequestInfo mtg = new BidRequestInfo(MainActivity.this.getString(R.string.app_id),
                    MainActivity.this.getString(R.string.interstitial_placement_id),  TestBidder3.class);
            BidRequestInfo other1 = new BidRequestInfo(MainActivity.this.getString(R.string.app_id),
                    MainActivity.this.getString(R.string.interstitial_placement_id),  TestBidder1.class);
            BidRequestInfo other2 = new BidRequestInfo(MainActivity.this.getString(R.string.app_id),
                    MainActivity.this.getString(R.string.interstitial_placement_id),  TestBidder2.class);

            List<BidRequestInfo> bidderReqs = new ArrayList<BidRequestInfo>();
            bidderReqs.add(fb);
            bidderReqs.add(mtg);
//            bidderReqs.add(other1);
//            bidderReqs.add(other2);

            BidRequestCallback callback = new BidRequestCallback(){
                @Override
                public void onSuccess(final AuctionResult auctionResult) {
                    LogUtil.i(TAG, "unitId = " + auctionResult.getUnitId() + " return success!");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (auctionResult.getWinner() != null) {
                                tvBidResult.setText(auctionResult.getWinner().getBidderClass().getSimpleName() + " Wins");
                            }else {
                                tvBidResult.setText( "No Bidder Wins");
                            }

                            mBidders.clear();
                            List<BiddingResponse> responseList = auctionResult.getBiddingResponses();
                            for (BiddingResponse biddingResponse : responseList){
                                BidderBean bidderBean = new BidderBean();
                                bidderBean.setBidderName(biddingResponse.getBidderClass().getSimpleName());
                                bidderBean.setBidderPriceUSD(biddingResponse.getBiddingPriceUSD());
                                bidderBean.setErrMessage(biddingResponse.getErrorMessage());
                                mBidders.add(bidderBean);
                            }
                            mBidderAdapter.notifyDataSetChanged();

                        }
                    });
                }

                @Override
                public void onFail(String unitId, final Throwable e) {
                    LogUtil.i(TAG, "unitId = " + unitId + " return fail!");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvBidResult.setText(e.getMessage());

                            mBidders.clear();
                            mBidderAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

            //send request
            HeaderBiddingAggregator.requestBid(bidderReqs,
                    "unitid", ADType.INTERSTITIAL,10 * 1000, callback);

        }catch (BidRequestException ex){
            ex.printStackTrace();
        }catch (Exception ex){
            ex.printStackTrace();
        }

        /**---------end----using bidding framework------------------*/


    }

}
