package com.insurance.todojee.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.insurance.todojee.R;
import com.insurance.todojee.activities.EditPolicyType_Activity;
import com.insurance.todojee.models.PolicyTypeListPojo;
import com.insurance.todojee.utilities.RecyclerItemClickListener;

import java.util.List;

public class GetLifeInsurParentPolicyTypeListAdapter extends RecyclerView.Adapter<GetLifeInsurParentPolicyTypeListAdapter.MyViewHolder> {

    private List<PolicyTypeListPojo> resultArrayList;
    private Context context;

    public GetLifeInsurParentPolicyTypeListAdapter(Context context, List<PolicyTypeListPojo> resultArrayList) {
        this.context = context;
        this.resultArrayList = resultArrayList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_row_parentpolicytype, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        PolicyTypeListPojo policyDetails = new PolicyTypeListPojo();
        policyDetails = resultArrayList.get(position);
        PolicyTypeListPojo finalPolicyDetails = policyDetails;

        holder.tv_companyname.setText(policyDetails.getCompany_name());

        if (policyDetails.getPolicy_details().size() != 0) {
            holder.tv_nopolicytype.setVisibility(View.GONE);
            holder.rv_policytypelist.setVisibility(View.VISIBLE);
            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            holder.rv_policytypelist.setLayoutManager(layoutManager);
            holder.rv_policytypelist.setAdapter(new GetLifeInsurChildPolicyTypeListAdapter(context, policyDetails.getPolicy_details()));
        } else {
            holder.tv_nopolicytype.setVisibility(View.VISIBLE);
            holder.rv_policytypelist.setVisibility(View.GONE);
        }

        holder.rv_policytypelist.addOnItemTouchListener(new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(context, EditPolicyType_Activity.class);
                intent.putExtra("position", position);
                intent.putExtra("policyDetails", finalPolicyDetails);
                context.startActivity(intent);
            }
        }));

    }

    @Override
    public int getItemCount() {
        return resultArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_companyname, tv_nopolicytype;
        private RecyclerView rv_policytypelist;


        public MyViewHolder(View view) {
            super(view);
            tv_companyname = view.findViewById(R.id.tv_companyname);
            tv_nopolicytype = view.findViewById(R.id.tv_nopolicytype);
            rv_policytypelist = view.findViewById(R.id.rv_policytypelist);
        }
    }
}
