/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package mtg.opensource.hibid.data;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Auction result for one runtime bidding
 */
public class AuctionResult {
	private String transactionId = "";
	private String unitId = "";
	private BiddingResponse winner = null;
	private List<BiddingResponse> biddingResponses = new ArrayList<BiddingResponse>();

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getUnitId() {
		return unitId;
	}

	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}

    /**
     *  Winner's price must be greater than zero
     * @return
     */
	public BiddingResponse getWinner(){
		winner = null;
	    if (biddingResponses != null && biddingResponses.size() > 0) {
			for (BiddingResponse biddingResponse : biddingResponses) {
				if(biddingResponse.getBiddingPriceUSD() <= 0.0 || !TextUtils.isEmpty(biddingResponse.getErrorMessage())){
					continue;
				}
				if (winner == null) {
					winner = biddingResponse;
				} else {
					if (winner.getBiddingPriceUSD() < biddingResponse.getBiddingPriceUSD()) {
						winner = biddingResponse;
					}
				}
			}
		}
		return winner;
	}

	public List<BiddingResponse> getBiddingResponses() {
		return biddingResponses;
	}

	public void setBiddingResponses(List<BiddingResponse> biddingResponses) {
		this.biddingResponses = biddingResponses;
	}

}
