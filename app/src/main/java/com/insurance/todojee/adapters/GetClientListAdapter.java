package com.insurance.todojee.adapters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.insurance.todojee.R;
import com.insurance.todojee.activities.ViewClientDetails_Activity;
import com.insurance.todojee.models.ClientMainListPojo;
import com.insurance.todojee.utilities.Utilities;

import java.util.List;

public class GetClientListAdapter extends RecyclerView.Adapter<GetClientListAdapter.MyViewHolder> {

    private List<ClientMainListPojo> resultArrayList;
    private Context context;

    public GetClientListAdapter(Context context, List<ClientMainListPojo> resultArrayList) {
        this.context = context;
        this.resultArrayList = resultArrayList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_row_client, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        ClientMainListPojo clientDetails = new ClientMainListPojo();
        clientDetails = resultArrayList.get(position);
        final ClientMainListPojo finalClientDetails = clientDetails;

        holder.tv_clientname.setText(clientDetails.getFirst_name());

        holder.tv_clientname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewClientDetails_Activity.class);
                intent.putExtra("clientDetails", finalClientDetails);
                context.startActivity(intent);
            }
        });

        holder.imv_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    context.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.getPackageName(), null)));
                    Utilities.showMessageString(context, "Please provide permission for making call");
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                    alertDialogBuilder.setTitle("Make a Call");
                    alertDialogBuilder.setIcon(R.drawable.icon_call_24dp);
                    alertDialogBuilder.setMessage("Are you sure you want to call " + finalClientDetails.getFirst_name() + " ?");
                    alertDialogBuilder.setCancelable(true);
                    alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @SuppressLint("MissingPermission")
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            context.startActivity(new Intent(Intent.ACTION_CALL,
                                    Uri.parse("tel:" + finalClientDetails.getMobile())));
                        }
                    });
                    alertDialogBuilder.setNegativeButton(
                            "No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert11 = alertDialogBuilder.create();
                    alert11.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                    alert11.show();
                }
            }
        });

        holder.imv_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri uri = Uri.parse("smsto:" + finalClientDetails.getMobile());
                    Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                    intent.putExtra("sms_body", "");
                    context.startActivity(intent);
                } catch (Exception e) {
                    Utilities.showMessageString(context, "No application found to perfrom this activity");
                }
            }
        });

        holder.imv_whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!finalClientDetails.getWhats_app_no().equals("")) {
//                    Uri uri = Uri.parse("smsto:" + finalClientDetails.getWhats_app_no());
//                    Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
//                    context.startActivity(Intent.createChooser(intent, "Select Whatsapp"));

                    String phoneno = "91" + finalClientDetails.getWhats_app_no();
//                    Uri uri = Uri.parse("smsto:" + phoneno);
//                    Intent i = new Intent(Intent.ACTION_SENDTO, uri);
////                    i.putExtra("sms_body", smsText);
//                    i.setPackage("com.whatsapp");
//                    context.startActivity(i);
//                    String URL = "https://api.whatsapp.com/send?phone=" + phoneno + "&text=hello";
                    String URL = "https://wa.me/" + phoneno;
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL)));
                } else {
                    Utilities.showMessageString(context, "Please add Whatsapp number");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return resultArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_clientname;
        private LinearLayout ll_mainlayout;
        private ImageView imv_call, imv_sms, imv_whatsapp;

        public MyViewHolder(View view) {
            super(view);
            tv_clientname = view.findViewById(R.id.tv_clientname);
            imv_call = view.findViewById(R.id.imv_call);
            imv_sms = view.findViewById(R.id.imv_sms);
            imv_whatsapp = view.findViewById(R.id.imv_whatsapp);
            ll_mainlayout = view.findViewById(R.id.ll_mainlayout);
        }
    }
}
