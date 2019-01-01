package com.insurance.todojee.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.insurance.todojee.R;
import com.insurance.todojee.activities.ViewGeneralInsurance_Activity;
import com.insurance.todojee.models.LifeGeneralInsuranceMainListPojo;

import java.util.List;

public class GetGeneralInsuranceListAdapter extends RecyclerView.Adapter<GetGeneralInsuranceListAdapter.MyViewHolder> {

    private List<LifeGeneralInsuranceMainListPojo> resultArrayList;
    private Context context;

    public GetGeneralInsuranceListAdapter(Context context, List<LifeGeneralInsuranceMainListPojo> resultArrayList) {
        this.context = context;
        this.resultArrayList = resultArrayList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_row_insurance, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        LifeGeneralInsuranceMainListPojo lifeInsuranceDetails = new LifeGeneralInsuranceMainListPojo();
        lifeInsuranceDetails = resultArrayList.get(position);
        final LifeGeneralInsuranceMainListPojo finalIifeInsuranceDetails = lifeInsuranceDetails;
        String insurerName = "";
        String InsurerTypeId = lifeInsuranceDetails.getInsurer_type_id();
        if (InsurerTypeId.equals("F")) {
            insurerName = (lifeInsuranceDetails.getInsurer_firm_name().trim().equals("")) ? "-" : lifeInsuranceDetails.getInsurer_firm_name();
        } else if (InsurerTypeId.equals("R")) {
            insurerName = (lifeInsuranceDetails.getInsurer_family_name().trim().equals("")) ? "-" : lifeInsuranceDetails.getInsurer_family_name();
        }

        String policyNo = (lifeInsuranceDetails.getPolicy_no().equals("")) ? "-" : lifeInsuranceDetails.getPolicy_no();
        String insuranceCompanyAlias = (lifeInsuranceDetails.getInsurance_company_alias().equals("")) ? "-" : lifeInsuranceDetails.getInsurance_company_alias();
        String startDate = (lifeInsuranceDetails.getStart_date().equals("")) ? "-" : lifeInsuranceDetails.getStart_date();
        String endDate = (lifeInsuranceDetails.getEnd_date().equals("")) ? "-" : lifeInsuranceDetails.getEnd_date();
        String frequency = (lifeInsuranceDetails.getFrequency().equals("")) ? "-" : lifeInsuranceDetails.getFrequency();

        holder.tv_name.setText(insurerName);
        holder.tv_policy_number.setText("Policy Number: " + policyNo);
        holder.tv_InsuranceCompany.setText("Company: " + insuranceCompanyAlias);
        holder.tv_term.setVisibility(View.GONE);

        holder.tv_startenddate.setText("Start Date: " + startDate + "  " + "Renewal Date: " + endDate);

        holder.ll_mainlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewGeneralInsurance_Activity.class);
                intent.putExtra("TYPE", "NONFILTER");
                intent.putExtra("generalInsuranceDetails", finalIifeInsuranceDetails);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return resultArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_name, tv_policy_number, tv_InsuranceCompany, tv_startenddate, tv_term;
        private LinearLayout ll_mainlayout;

        public MyViewHolder(View view) {
            super(view);
            tv_name = view.findViewById(R.id.tv_name);
            tv_policy_number = view.findViewById(R.id.tv_policy_number);
            tv_InsuranceCompany = view.findViewById(R.id.tv_InsuranceCompany);
            tv_startenddate = view.findViewById(R.id.tv_startenddate);
            tv_term = view.findViewById(R.id.tv_term);
            ll_mainlayout = view.findViewById(R.id.ll_mainlayout);
        }
    }
}
