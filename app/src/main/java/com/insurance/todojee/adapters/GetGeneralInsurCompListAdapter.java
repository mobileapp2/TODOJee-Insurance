package com.insurance.todojee.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.insurance.todojee.R;
import com.insurance.todojee.models.InsuranceCompanyListPojo;

import java.util.List;

public class GetGeneralInsurCompListAdapter extends RecyclerView.Adapter<GetGeneralInsurCompListAdapter.MyViewHolder> {

    private List<InsuranceCompanyListPojo> resultArrayList;
    private Context context;

    public GetGeneralInsurCompListAdapter(Context context, List<InsuranceCompanyListPojo> resultArrayList) {
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

        holder.tv_namealias.setText(companyDetails.getCompany_name() + " | " + companyDetails.getCompany_alias());
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
