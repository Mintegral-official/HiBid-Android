package mtg.opensource.hibid.bidder;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import mtg.opensource.hibid.callback.BiddingCallback;
import mtg.opensource.hibid.constants.ADType;
import mtg.opensource.hibid.data.BidRequestInfo;
import mtg.opensource.hibid.data.BiddingResponse;
import mtg.opensource.hibid.exception.BidderInitFailedException;
import mtg.opensource.hibid.exception.BiddingException;

@RunWith(AndroidJUnit4.class)
public class MtgBidderTest {
    private Context context = InstrumentationRegistry.getTargetContext();

    @Test
    public void testMTGBidder() {
        MtgBidder mb = new MtgBidder();
        try {
            mb.init(context);
        }catch (BidderInitFailedException e) {

        }

        BidRequestInfo bidRequestInfo = new BidRequestInfo("92762","21310", MtgBidder.class);
        BiddingCallback callback = new BiddingCallback() {
            @Override
            public void onBiddingResponse(BiddingResponse object) {
                Assert.assertNotNull("response", object);
            }
        };

        try {
            mb.bid(bidRequestInfo, ADType.INTERSTITIAL,10, callback);
        }catch (BiddingException e) {

        }
    }
}
