package mtg.opensource.hibid.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mtg.opensource.hibid.bidder.FacebookBidder;
import mtg.opensource.hibid.bidder.MtgBidder;

import static org.junit.Assert.assertTrue;

public class BiddingResponseTest {

    private BiddingResponse br1 = new BiddingResponse();
    private BiddingResponse br2 = new BiddingResponse();

    @Before
    public void setUpClass() {
        br1.setBiddingPriceUSD(1.11d);
        br1.setBidderClass(FacebookBidder.class);
        br2.setBiddingPriceUSD(2.22d);
        br2.setBidderClass(MtgBidder.class);
    }

    @After
    public void tearDown(){
    }

    @Test
    public void testCompareTo() {
        assertTrue(br1.compareTo(br2) < 0);
    }


}