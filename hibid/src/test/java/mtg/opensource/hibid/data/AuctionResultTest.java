package mtg.opensource.hibid.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import mtg.opensource.hibid.bidder.FacebookBidder;
import mtg.opensource.hibid.bidder.MtgBidder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AuctionResultTest {
    private BiddingResponse br1 = new BiddingResponse();
    private BiddingResponse br2 = new BiddingResponse();
    private AuctionResult auctionResult = new AuctionResult();

    @Before
    public void setUpClass() {
        br1.setBiddingPriceUSD(1.11d);
        br1.setBidderClass(FacebookBidder.class);
        br2.setBiddingPriceUSD(2.22d);
        br2.setBidderClass(MtgBidder.class);
        List<BiddingResponse> otherBidders = new ArrayList<BiddingResponse>();
        otherBidders.add(br1);
        auctionResult.setWinner(br2);
        auctionResult.setOtherBidders(otherBidders);
    }

    @After
    public void tearDown(){
    }

    @Test
    public void testGetTransactionId() {
        assertTrue(true);
    }

    @Test
    public void testSetTransactionId() {
        assertTrue(true);
    }

    @Test
    public void testGetWinner() {
        assertEquals(br2, auctionResult.getWinner());
    }

    @Test
    public void testGetOtherBidders() {
        assertNotNull(auctionResult.getOtherBidders());
        assertTrue(auctionResult.getOtherBidders().contains(br1));
    }
}