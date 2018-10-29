package com.insurance.todojee.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.insurance.todojee.R;
import com.insurance.todojee.models.EventListPojo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.insurance.todojee.utilities.Utilities.changeDateFormat;

public class GetWeekWiseEventListAdapter extends RecyclerView.Adapter<GetWeekWiseEventListAdapter.MyViewHolder> {

    private List<EventListPojo> resultArrayList;
    private List<Date> dateList;
    private Context context;
    private SimpleDateFormat dateFormat;

    public GetWeekWiseEventListAdapter(Context context, List<EventListPojo> resultArrayList, ArrayList<Date> dateList) {
        this.context = context;
        this.resultArrayList = resultArrayList;
        this.dateList = dateList;

        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_row_weekwiseevents, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        EventListPojo eventDetails = new EventListPojo();
        eventDetails = resultArrayList.get(position);

        holder.tv_date.setText(changeDateFormat("MM/dd/yyyy",
                "dd MMM yyyy EEEE", eventDetails.getDate()));
        holder.tv_event.setText(eventDetails.getDescription());
        holder.tv_status.setText(eventDetails.getStatus());

        if (eventDetails.getStatus().equalsIgnoreCase("In Progress")) {
            holder.tv_event.setTextColor(context.getResources().getColor(R.color.Saffron));
            holder.tv_status.setTextColor(context.getResources().getColor(R.color.Saffron));
        } else if (eventDetails.getStatus().equals("Completed")) {
            holder.tv_event.setTextColor(context.getResources().getColor(R.color.Clover_Green));
            holder.tv_status.setTextColor(context.getResources().getColor(R.color.Clover_Green));
        } else if (eventDetails.getStatus().equals("Dismissed")) {
            holder.tv_event.setTextColor(context.getResources().getColor(R.color.Love_Red));
            holder.tv_status.setTextColor(context.getResources().getColor(R.color.Love_Red));
        }

        for (Date dateObj : dateList) {
            String dateStr = dateFormat.format(dateObj);
        }


//        if (position == resultArrayList.size() - 1) {
//            holder.view1.setVisibility(View.GONE);
//        }
    }

    @Override
    public int getItemCount() {
        return resultArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_date, tv_event, tv_status;
        private View view1;

        public MyViewHolder(View view) {
            super(view);
            tv_date = view.findViewById(R.id.tv_date);
            tv_event = view.findViewById(R.id.tv_event);
            tv_status = view.findViewById(R.id.tv_status);
            view1 = view.findViewById(R.id.view);
        }
    }
}
