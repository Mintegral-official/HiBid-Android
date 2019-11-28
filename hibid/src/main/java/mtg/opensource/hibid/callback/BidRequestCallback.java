/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package mtg.opensource.hibid.callback;

import mtg.opensource.hibid.data.AuctionResult;

/**
 * Callback from transaction to developer
 */
public interface BidRequestCallback {

    void onBidRequestCallback(String unitId, AuctionResult auctionResult);
}
