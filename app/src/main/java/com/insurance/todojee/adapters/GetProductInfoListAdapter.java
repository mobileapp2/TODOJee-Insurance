package com.insurance.todojee.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.insurance.todojee.R;
import com.insurance.todojee.models.ProductInfoListPojo;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GetProductInfoListAdapter extends RecyclerView.Adapter<GetProductInfoListAdapter.MyViewHolder> {

    private List<ProductInfoListPojo> resultArrayList;
    private Context context;

    public GetProductInfoListAdapter(Context context, List<ProductInfoListPojo> resultArrayList) {
        this.context = context;
        this.resultArrayList = resultArrayList;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_row_productslist, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        ProductInfoListPojo productInfoDetails = new ProductInfoListPojo();
        productInfoDetails = resultArrayList.get(position);

        holder.tv_name.setText(productInfoDetails.getText());

        if (!productInfoDetails.getDocument().equals("")) {
            Picasso.with(context)
                    .load(productInfoDetails.getDocument())
                    .placeholder(R.drawable.img_product)
                    .into(holder.imv_product);
        }

    }

    @Override
    public int getItemCount() {
        return resultArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_name;
        private ImageView imv_product;

        public MyViewHolder(View view) {
            super(view);
            tv_name = view.findViewById(R.id.tv_name);
            imv_product = view.findViewById(R.id.imv_product);
        }
    }

}
