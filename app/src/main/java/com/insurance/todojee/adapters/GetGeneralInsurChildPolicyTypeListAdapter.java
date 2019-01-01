package com.insurance.todojee.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.insurance.todojee.R;
import com.insurance.todojee.models.PolicyTypeListPojo;

import java.util.List;

public class GetGeneralInsurChildPolicyTypeListAdapter extends RecyclerView.Adapter<GetGeneralInsurChildPolicyTypeListAdapter.MyViewHolder> {

    private List<PolicyTypeListPojo.Policy_details> resultArrayList;
    private Context context;

    public GetGeneralInsurChildPolicyTypeListAdapter(Context context, List<PolicyTypeListPojo.Policy_details> resultArrayList) {
        this.context = context;
        this.resultArrayList = resultArrayList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_row_single, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        PolicyTypeListPojo.Policy_details policyDetails = new PolicyTypeListPojo.Policy_details();
        policyDetails = resultArrayList.get(position);

        holder.tv_namealias.setText(policyDetails.getType() + " | " + policyDetails.getAlias());
        holder.tv_namealias.setTypeface(null, Typeface.NORMAL);
        if (position == resultArrayList.size() - 1) {
            holder.view1.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return resultArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_namealias;
        View view1;

        public MyViewHolder(View view) {
            super(view);
            tv_namealias = view.findViewById(R.id.tv_namealias);
            view1 = view.findViewById(R.id.view);
        }
    }
}
