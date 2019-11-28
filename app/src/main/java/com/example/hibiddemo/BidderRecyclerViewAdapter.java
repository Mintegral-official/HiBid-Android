package com.example.hibiddemo;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BidderRecyclerViewAdapter extends RecyclerView.Adapter<BidderRecyclerViewAdapter.BidderViewHolder> {

    private LayoutInflater mInflater;
    private List<BidderBean> mBidders = new ArrayList<BidderBean>();

    public BidderRecyclerViewAdapter(Context context, List<BidderBean> bidders) {

        this.mInflater = LayoutInflater.from(context);
        this.mBidders = bidders;
    }


    @Override
    public BidderRecyclerViewAdapter.BidderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new BidderViewHolder(mInflater.inflate(R.layout.item_bidder, parent, false));
    }

    @Override
    public void onBindViewHolder(BidderRecyclerViewAdapter.BidderViewHolder holder, int position) {

        BidderBean bidderBean = mBidders.get(position);
        holder.setText(bidderBean.getBidderName(), bidderBean.getBidderPriceUSD(), bidderBean.getErrMessage());
    }

    @Override
    public int getItemCount() {
        return mBidders == null ? 0 : mBidders.size();
    }


    public class BidderViewHolder extends RecyclerView.ViewHolder {

        private TextView tvBidderName;
        private TextView tvBidderPrice;
        private TextView tvBidderErrMessage;

        public BidderViewHolder(View itemView) {
            super(itemView);
            tvBidderName = (TextView) itemView.findViewById(R.id.bidder_name);
            tvBidderPrice = (TextView) itemView.findViewById(R.id.bidder_price);
            tvBidderErrMessage = (TextView)itemView.findViewById(R.id.bidder_errmessage);
        }

        public void setText(String bidderName, double bidderPrice, String errMessage) {
            tvBidderName.setText(bidderName + ":");
            tvBidderPrice.setText("$" + BigDecimal.valueOf(bidderPrice));
            tvBidderErrMessage.setText(errMessage);
        }
    }
}
