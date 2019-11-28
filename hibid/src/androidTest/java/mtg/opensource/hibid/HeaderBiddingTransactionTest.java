package mtg.opensource.hibid;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mtg.opensource.hibid.bidder.FacebookBidder;
import mtg.opensource.hibid.bidder.TestBidder1;
import mtg.opensource.hibid.callback.BidRequestCallback;
import mtg.opensource.hibid.constants.ADType;
import mtg.opensource.hibid.data.AuctionResult;
import mtg.opensource.hibid.data.BidRequestInfo;

@RunWith(AndroidJUnit4.class)
public class HeaderBiddingTransactionTest {
    private ExecutorService executor = Executors.newCachedThreadPool();
    private HeaderBiddingTransaction transaction;
    private BidRequestCallback bidRequestCallback;
    private HashMap<Bidder, BidRequestInfo> bidders = new HashMap<>();

    @Before
    public void prepare() {
        bidders.put(new FacebookBidder(), new BidRequestInfo("567961460375854","567961460375854_567962500375750", FacebookBidder.class));
        bidders.put(new TestBidder1(), new BidRequestInfo("567961460375854","567961460375854_567962500375750", TestBidder1.class));

        bidRequestCallback = new BidRequestCallback() {
            @Override
            public void onSuccess(AuctionResult auctionResult) {
                Assert.assertNotNull("result", auctionResult);
                System.out.println(auctionResult.toString());
            }

            @Override
            public void onFail(String unitId, Throwable e) {
                Assert.assertNotNull("result", e.getMessage());
                System.out.println(e.getMessage());
            }
        };
        transaction = new HeaderBiddingTransaction(executor, "", ADType.INTERSTITIAL, bidRequestCallback);
    }

    @Test
    public void testTransaction() {
        transaction.startTransaction(bidders, 10);
    }
}