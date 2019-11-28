/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package mtg.opensource.hibid.data;

import mtg.opensource.hibid.Bidder;

/**
 * Response for one bidder's bid request
 */
public class BiddingResponse implements Comparable<BiddingResponse>{

	private Class bidderClass;
	private double biddingPriceUSD;
    private Object payload;
	private Bidder bidder;
	private String errorMessage;

	public BiddingResponse(){

	}

	public BiddingResponse(Class bidderClass, double biddingPriceUSD, Object payload, Bidder bidder) {
		this.bidderClass = bidderClass;
		this.biddingPriceUSD = biddingPriceUSD;
		this.payload = payload;
		this.bidder = bidder;
	}

	public BiddingResponse(Class bidderClass, Bidder bidder, String errorMessage) {
		this.bidderClass = bidderClass;
		this.bidder = bidder;
		this.errorMessage = errorMessage;
	}

	public Class getBidderClass() {
		return bidderClass;
	}

	public void setBidderClass(Class bidderClass) {
		this.bidderClass = bidderClass;
	}

	public double getBiddingPriceUSD() {
		return biddingPriceUSD;
	}

	public void setBiddingPriceUSD(double biddingPriceUSD) {
		this.biddingPriceUSD = biddingPriceUSD;
	}

	public Object getPayload() {
		return payload;
	}

	public void setPayload(Object payload) {
		this.payload = payload;
	}

	public Bidder getBidder() {
		return bidder;
	}

	public void setBidder(Bidder bidder) {
		this.bidder = bidder;
	}

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
	public int compareTo(BiddingResponse other) {
		if (this.biddingPriceUSD > other.getBiddingPriceUSD()) {
			return -1;
		} else if (this.biddingPriceUSD == other.getBiddingPriceUSD()) {
			int code = 0;
			if (bidderClass != null && other.getBidderClass() != null) {
				String bidderClassName = bidderClass.getSimpleName();
				String otherClassName = other.getBidderClass().getSimpleName();
				code = bidderClassName.compareTo(otherClassName);
				if (code > 0){
					code = 1;
				}
			}
			return code;
		} else {
			return 1;
		}
	}


}
