package com.insurance.todojee.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.insurance.todojee.R;
import com.insurance.todojee.models.NotificationListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.UserSessionManager;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class GetNotificationListAdapter extends RecyclerView.Adapter<GetNotificationListAdapter.MyViewHolder> {

    private List<NotificationListPojo> resultArrayList;
    private Context context;
    private UserSessionManager session;
    private String user_id;

    public GetNotificationListAdapter(Context context, List<NotificationListPojo> resultArrayList) {
        this.context = context;
        this.resultArrayList = resultArrayList;
        session = new UserSessionManager(context);
        try {
            JSONArray user_info = new JSONArray(session.getUserDetails().get(
                    ApplicationConstants.KEY_LOGIN_INFO));
            JSONObject json = user_info.getJSONObject(0);
            user_id = json.getString("id");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_row_notification, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        NotificationListPojo notificationDetails = new NotificationListPojo();
        notificationDetails = resultArrayList.get(position);


        holder.tv_title.setText(notificationDetails.getTitle());
        holder.tv_message.setText(notificationDetails.getMessage());


        if (!notificationDetails.getImage().equals("")) {
            Picasso.with(context)
                    .load(notificationDetails.getImage())
                    .into(holder.imv_notificationimg, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.imv_notificationimg.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError() {
                            holder.imv_notificationimg.setVisibility(View.GONE);
                        }
                    });
        } else {
            holder.imv_notificationimg.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return resultArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imv_notificationimg, imv_delete;
        private TextView tv_title, tv_message;

        public MyViewHolder(View view) {
            super(view);
            imv_notificationimg = view.findViewById(R.id.imv_notificationimg);
            imv_delete = view.findViewById(R.id.imv_delete);
            tv_title = view.findViewById(R.id.tv_title);
            tv_message = view.findViewById(R.id.tv_message);
        }
    }

}
