package mtg.opensource.hibid.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AuctionNotificationTest {

    @Test
    public void testAuctionWin() {
        AuctionNotification winNotification =
                AuctionNotification.getAuctionNotification(AuctionNotification.ReasonCode.Win);
        assertEquals(AuctionNotification.ReasonCode.Win, winNotification.getReasonCode());
        assertEquals(true, winNotification.isWinner());
    }

    @Test
    public void testAuctionLowPrice() {
        AuctionNotification lowPriceNotification =
                AuctionNotification.getAuctionNotification(AuctionNotification.ReasonCode.Loss);
        assertEquals(AuctionNotification.ReasonCode.Loss, lowPriceNotification.getReasonCode());
        assertEquals(false, lowPriceNotification.isWinner());
    }

    @Test
    public void testAuctionTimeout() {
        AuctionNotification timeoutNotification =
                AuctionNotification.getAuctionNotification(AuctionNotification.ReasonCode.Timeout);
        assertEquals(AuctionNotification.ReasonCode.Timeout, timeoutNotification.getReasonCode());
        assertEquals(false, timeoutNotification.isWinner());
    }
}