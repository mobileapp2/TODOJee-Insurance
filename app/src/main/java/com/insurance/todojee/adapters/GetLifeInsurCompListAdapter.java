package com.insurance.todojee.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.insurance.todojee.R;
import com.insurance.todojee.activities.EditInsuranceCompany_Activity;
import com.insurance.todojee.models.InsuranceCompanyListPojo;

import java.util.List;

public class GetLifeInsurCompListAdapter extends RecyclerView.Adapter<GetLifeInsurCompListAdapter.MyViewHolder> {

    private List<InsuranceCompanyListPojo> resultArrayList;
    private Context context;

    public GetLifeInsurCompListAdapter(Context context, List<InsuranceCompanyListPojo> resultArrayList) {
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
        InsuranceCompanyListPojo companyDetails = new InsuranceCompanyListPojo();
        companyDetails = resultArrayList.get(position);
        InsuranceCompanyListPojo finalCompanyDetails = companyDetails;

        holder.tv_namealias.setText(finalCompanyDetails.getCompany_name() + " | " + finalCompanyDetails.getCompany_alias());

        holder.tv_namealias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditInsuranceCompany_Activity.class);
                intent.putExtra("companyDetails", finalCompanyDetails);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return resultArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_namealias;

        public MyViewHolder(View view) {
            super(view);
            tv_namealias = view.findViewById(R.id.tv_namealias);
        }
    }
}
