package com.insurance.todojee.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.insurance.todojee.R;
import com.insurance.todojee.activities.AddClientDetails_Activity;
import com.insurance.todojee.models.ContactListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.UserSessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;


public class ContactListRVAapter extends RecyclerView.Adapter<ContactListRVAapter.MyViewHolder> {

    private Context context;
    private List<ContactListPojo> contactList;
    private UserSessionManager session;
    private String user_id;
    private static JSONArray MobileNumbers;

    public ContactListRVAapter(Context context, List<ContactListPojo> contactList) {
        this.context = context;
        this.contactList = contactList;
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

    /* @Override
     public void onViewDetachedFromWindow(MyViewHolder holder) {
         super.onViewDetachedFromWindow(holder);
         holder.itemView.clearAnimation();

     }*/
    public void setFadeAnimation(View view) {
       /*AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
       anim.setDuration(500);
       view.startAnimation(anim);*/
        TranslateAnimation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(1000);
        view.startAnimation(animation);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_row_contact, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.tv_initletter.setText(contactList.get(position).getInitLetter());
        holder.tv_name.setText(contactList.get(position).getName());
        holder.tv_phoneno.setText(contactList.get(position).getPhoneNo());

        setFadeAnimation(holder.rl_mainlayout);

        String mobile_number = contactList.get(position).getPhoneNo();
        if (mobile_number.length() > 10) {
            if (mobile_number.startsWith("+91")) {
                mobile_number = mobile_number.substring(3);
            } else if (mobile_number.startsWith("0")) {
                mobile_number = mobile_number.substring(1);
            } else if (mobile_number.contains("-")) {
                mobile_number = mobile_number.replace("-", "");
            }
        }
        if (mobile_number.length() > 10 || mobile_number.length() < 10) {
            mobile_number = "";
        }

        final String finalMobile_number = mobile_number;
        holder.rl_mainlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddClientDetails_Activity.class);
                intent.putExtra("type", "contact");
                intent.putExtra("contact_name", contactList.get(position).getName());
                intent.putExtra("contact_mobile", finalMobile_number);
                intent.putExtra("contact_email", contactList.get(position).getEmail());
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout rl_mainlayout;
        private TextView tv_initletter, tv_name, tv_phoneno;
        private ImageView tv_img;


        public MyViewHolder(View view) {
            super(view);
            tv_initletter = (TextView) view.findViewById(R.id.tv_initletter);
            tv_name = (TextView) view.findViewById(R.id.tv_bankname);
            tv_phoneno = (TextView) view.findViewById(R.id.tv_accountno);
            rl_mainlayout = view.findViewById(R.id.rl_mainlayout);
//            tv_img = view.findViewById(R.id.ic_image);
        }
    }


}
