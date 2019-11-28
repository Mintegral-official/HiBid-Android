package com.example.hibiddemo;

import java.io.Serializable;

public class BidderBean implements Serializable {
    private static final long serialVersionUID = -3919079918823946061L;

    private String bidderName;
    private double bidderPriceUSD;
    private String errMessage;

    public String getBidderName() {
        return bidderName;
    }

    public void setBidderName(String bidderName) {
        this.bidderName = bidderName;
    }

    public double getBidderPriceUSD() {
        return bidderPriceUSD;
    }

    public void setBidderPriceUSD(double bidderPriceUSD) {
        this.bidderPriceUSD = bidderPriceUSD;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }
}
