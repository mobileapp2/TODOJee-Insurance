package com.insurance.todojee.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.insurance.todojee.R;
import com.insurance.todojee.models.EventListPojo;

import java.util.List;

public class GetEventListAdapter extends RecyclerView.Adapter<GetEventListAdapter.MyViewHolder> {

    private List<EventListPojo> resultArrayList;
    private Context context;

    public GetEventListAdapter(Context context, List<EventListPojo> resultArrayList) {
        this.context = context;
        this.resultArrayList = resultArrayList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_row_events, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        EventListPojo eventDetails = new EventListPojo();
        eventDetails = resultArrayList.get(position);

        holder.tv_event.setText(eventDetails.getDescription());
        holder.tv_status.setText(eventDetails.getStatus());


        if (eventDetails.getStatus().equalsIgnoreCase("In ")) {

        } else if (eventDetails.getStatus().equals("")) {

        } else if (eventDetails.getStatus().equals("")) {

        }

        if (position == resultArrayList.size() - 1) {
            holder.view1.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return resultArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_event, tv_status;
        private View view1;

        public MyViewHolder(View view) {
            super(view);
            tv_event = view.findViewById(R.id.tv_event);
            tv_status = view.findViewById(R.id.tv_status);
            view1 = view.findViewById(R.id.view);
        }
    }
}
