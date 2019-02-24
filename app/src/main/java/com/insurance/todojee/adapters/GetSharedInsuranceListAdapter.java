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
import com.insurance.todojee.activities.ViewSharedGeneralInsurance_Activity;
import com.insurance.todojee.activities.ViewSharedLifeInsurance_Activity;
import com.insurance.todojee.models.LifeGeneralInsuranceMainListPojo;

import java.util.List;

public class GetSharedInsuranceListAdapter extends RecyclerView.Adapter<GetSharedInsuranceListAdapter.MyViewHolder> {

    private List<LifeGeneralInsuranceMainListPojo> resultArrayList;
    private Context context;

    public GetSharedInsuranceListAdapter(Context context, List<LifeGeneralInsuranceMainListPojo> resultArrayList) {
        this.context = context;
        this.resultArrayList = resultArrayList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_row_shared_insurance, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        LifeGeneralInsuranceMainListPojo lifeInsuranceDetails = new LifeGeneralInsuranceMainListPojo();
        lifeInsuranceDetails = resultArrayList.get(position);
        final LifeGeneralInsuranceMainListPojo finalIifeInsuranceDetails = lifeInsuranceDetails;
        String insurerName = "", insurance_type = "";
        String InsurerTypeId = lifeInsuranceDetails.getInsurer_type_id();
        if (InsurerTypeId.equals("F")) {
            insurerName = (lifeInsuranceDetails.getInsurer_firm_name().trim().equals("")) ? "-" : lifeInsuranceDetails.getInsurer_firm_name();
        } else if (InsurerTypeId.equals("R")) {
            insurerName = (lifeInsuranceDetails.getInsurer_family_name().trim().equals("")) ? "-" : lifeInsuranceDetails.getInsurer_family_name();
        }

        String policyNo = (lifeInsuranceDetails.getPolicy_no().equals("")) ? "-" : lifeInsuranceDetails.getPolicy_no();
        String insuranceCompanyAlias = (lifeInsuranceDetails.getInsurance_company_alias().equals("")) ? "-" : lifeInsuranceDetails.getInsurance_company_alias();
        String startDate = (lifeInsuranceDetails.getStart_date() == null || lifeInsuranceDetails.getStart_date().trim().isEmpty()) ? "N/A" : lifeInsuranceDetails.getStart_date();
        String endDate = (lifeInsuranceDetails.getStart_date() == null || lifeInsuranceDetails.getEnd_date().equals("")) ? "N/A" : lifeInsuranceDetails.getEnd_date();
        String frequency = (lifeInsuranceDetails.getFrequency().equals("")) ? "N/A" : lifeInsuranceDetails.getFrequency();

        if (lifeInsuranceDetails.getInsurance_type_id().equals("2")) {
            insurance_type = "Life Insurance";
        } else if (lifeInsuranceDetails.getInsurance_type_id().equals("1")) {
            insurance_type = "General Insurance";
        }
        holder.tv_lic_type.setText(insurance_type);
        holder.tv_name.setText("Policy Created by : " + lifeInsuranceDetails.getLic_created_by());
        holder.tv_policy_number.setText("Policy Number: " + policyNo);
        holder.tv_InsuranceCompany.setText("Company: " + insuranceCompanyAlias);
        holder.tv_term.setVisibility(View.GONE);

        //holder.tv_startenddate.setText("Start Date: " + startDate + "  " + "Renewal Date: " + endDate );
        holder.tv_startenddate.setText("premium Amount : " + lifeInsuranceDetails.getPremium_amount());
        holder.ll_mainlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (finalIifeInsuranceDetails.getInsurance_type_id().equals("2")) {
                    Intent intent = new Intent(context, ViewSharedLifeInsurance_Activity.class);
                    intent.putExtra("TYPE", "NONFILTER");
                    intent.putExtra("lifeInsuranceDetails", finalIifeInsuranceDetails);
                    context.startActivity(intent);
                } else if (finalIifeInsuranceDetails.getInsurance_type_id().equals("1")) {
                    Intent intent = new Intent(context, ViewSharedGeneralInsurance_Activity.class);
                    intent.putExtra("TYPE", "NONFILTER");
                    intent.putExtra("lifeInsuranceDetails", finalIifeInsuranceDetails);
                    context.startActivity(intent);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return resultArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_name, tv_policy_number, tv_InsuranceCompany, tv_startenddate, tv_term, tv_lic_type;
        private LinearLayout ll_mainlayout;

        public MyViewHolder(View view) {
            super(view);
            tv_lic_type = view.findViewById(R.id.tv_lic_type);
            tv_name = view.findViewById(R.id.tv_name);
            tv_policy_number = view.findViewById(R.id.tv_policy_number);
            tv_InsuranceCompany = view.findViewById(R.id.tv_InsuranceCompany);
            tv_startenddate = view.findViewById(R.id.tv_startenddate);
            tv_term = view.findViewById(R.id.tv_term);
            ll_mainlayout = view.findViewById(R.id.ll_mainlayout);
        }
    }
}
