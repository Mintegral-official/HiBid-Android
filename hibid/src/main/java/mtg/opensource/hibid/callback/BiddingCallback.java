/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package mtg.opensource.hibid.callback;

import mtg.opensource.hibid.data.BiddingResponse;

/**
 * Callback from bidder to transaction
 */
public interface BiddingCallback {

    void onBiddingResponse(BiddingResponse object);
}
