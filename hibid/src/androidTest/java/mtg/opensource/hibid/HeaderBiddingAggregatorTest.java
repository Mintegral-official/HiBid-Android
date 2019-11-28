package mtg.opensource.hibid;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import mtg.opensource.hibid.bidder.FacebookBidder;
import mtg.opensource.hibid.bidder.MtgBidder;
import mtg.opensource.hibid.callback.BidRequestCallback;
import mtg.opensource.hibid.constants.ADType;
import mtg.opensource.hibid.data.AuctionResult;
import mtg.opensource.hibid.data.BidRequestInfo;
import mtg.opensource.hibid.exception.BidRequestException;

@RunWith(AndroidJUnit4.class)
public class HeaderBiddingAggregatorTest {
    private Context context = InstrumentationRegistry.getTargetContext();
    private  List<BidRequestInfo> bidderReqs = new ArrayList<BidRequestInfo>();

    @Before
    public void prepare() {

        BidRequestInfo fb = new BidRequestInfo("567961460375854","567961460375854_567962500375750", FacebookBidder.class);
        BidRequestInfo mtg = new BidRequestInfo("92762", "21310",  MtgBidder.class);
        bidderReqs.add(fb);
        bidderReqs.add(mtg);

        HeaderBiddingAggregator.init(context);
    }

    @Test
    public void testRequestBid() {
        requestBid();
    }

    @Test
    public void testRequestBidWithSoMany() {
        for (int i = 0; i < 100; i++) {
            requestBid();
        }
    }

    private void requestBid() {
        try {
            HeaderBiddingAggregator.requestBid(bidderReqs, "unitid1",
                    ADType.INTERSTITIAL, 10, new BidRequestCallback() {
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
            });
        } catch (BidRequestException e) {
            e.printStackTrace();
        }
    }
}