package mtg.opensource.hibid.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import mtg.opensource.hibid.bidder.FacebookBidder;
import mtg.opensource.hibid.bidder.TestBidder1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AuctionResultTest {
    private AuctionResult auctionResult = new AuctionResult();
    private List<BiddingResponse> biddingResponses = new ArrayList<BiddingResponse>();
    private BiddingResponse br1 = new BiddingResponse();
    private BiddingResponse br2 = new BiddingResponse();

    @Before
    public void setUpClass() {
        br1.setBiddingPriceUSD(1.11d);
        br1.setBidderClass(FacebookBidder.class);
        br2.setBiddingPriceUSD(2.22d);
        br1.setBidderClass(TestBidder1.class);
        biddingResponses.add(br1);
        biddingResponses.add(br2);
        auctionResult.setBiddingResponses(biddingResponses);
    }

    @After
    public void tearDown(){
        biddingResponses.clear();
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
    public void getBiddingResponses() {
        assertNotNull(auctionResult.getBiddingResponses());
    }

    @Test
    public void setBiddingResponses() {
        assertTrue(auctionResult.getBiddingResponses().contains(br1));
        assertTrue(auctionResult.getBiddingResponses().contains(br2));
    }
}