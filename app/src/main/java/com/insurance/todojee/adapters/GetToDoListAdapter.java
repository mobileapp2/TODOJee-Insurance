package com.insurance.todojee.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.insurance.todojee.R;
import com.insurance.todojee.fragments.TodoList_Fragment;
import com.insurance.todojee.models.ToDoListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class GetToDoListAdapter extends RecyclerView.Adapter<GetToDoListAdapter.MyViewHolder> {

    private List<ToDoListPojo> resultArrayList;
    private Context context;
    private UserSessionManager session;
    private String user_id;

    public GetToDoListAdapter(Context context, List<ToDoListPojo> resultArrayList) {
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
        View view = inflater.inflate(R.layout.list_row_todolist, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        ToDoListPojo toDoDetails = new ToDoListPojo();
        toDoDetails = resultArrayList.get(position);
        final ToDoListPojo finalToDoDetails = toDoDetails;

        if (toDoDetails.getIs_completed().equals("0")) {
            holder.cb_completed.setChecked(false);
//            holder.tv_tasks.setTextColor(context.getResources().getColor(R.color.Love_Red));
        } else if (toDoDetails.getIs_completed().equals("1")) {
            holder.cb_completed.setChecked(true);
            holder.tv_tasks.setPaintFlags(holder.tv_tasks.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//            holder.tv_tasks.setTextColor(context.getResources().getColor(R.color.Medium_Forest_Green));
        }
        holder.tv_tasks.setText(toDoDetails.getList());

        holder.cb_completed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isInternetAvailable(context)) {
                    String isCompleted;
                    if (holder.cb_completed.isChecked()) {
                        isCompleted = "1";
                    } else {
                        isCompleted = "0";
                    }
                    new IsCompletedTodoTask().execute(isCompleted,
                            user_id,
                            finalToDoDetails.getId());
                } else {
                    Utilities.showMessageString(context, "Please Check Internet Connection");
                }
            }
        });

        holder.tv_tasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText edt_todotask = new EditText(context);
                edt_todotask.setText(finalToDoDetails.getList());
                edt_todotask.setSelection(finalToDoDetails.getList().length());
                float dpi = context.getResources().getDisplayMetrics().density;
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                builder.setTitle("Edit Task");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (Utilities.isInternetAvailable(context)) {
                            new UpdateTodoTask().execute(edt_todotask.getText().toString().trim(),
                                    finalToDoDetails.getIs_completed(),
                                    user_id,
                                    finalToDoDetails.getId());
                        } else {
                            Utilities.showMessageString(context, "Please Check Internet Connection");
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                final AlertDialog alertD = builder.create();
                alertD.setView(edt_todotask, (int) (19 * dpi), (int) (5 * dpi), (int) (14 * dpi), (int) (5 * dpi));
                alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                alertD.show();

                edt_todotask.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (TextUtils.isEmpty(s)) {
                            alertD.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        } else {
                            alertD.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        }

                    }
                });
            }
        });

        holder.img_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isInternetAvailable(context)) {
                    new DeleteTodoTask().execute(finalToDoDetails.getId());
                } else {
                    Utilities.showMessageString(context, "Please Check Internet Connection");
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return resultArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_tasks;
        private ImageView img_delete;
        private CheckBox cb_completed;

        public MyViewHolder(View view) {
            super(view);
            tv_tasks = view.findViewById(R.id.tv_tasks);
            img_delete = view.findViewById(R.id.img_delete);
            cb_completed = view.findViewById(R.id.cb_completed);
        }
    }

    public class IsCompletedTodoTask extends AsyncTask<String, Void, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context, R.style.CustomDialogTheme);
            pd.setMessage("Please wait ...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "changeStatus");
            obj.addProperty("is_completed", params[0]);
            obj.addProperty("user_id", params[1]);
            obj.addProperty("id", params[2]);
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.TODOLISTAPI, obj.toString());
            return res.trim();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String type = "", message = "";
            try {
                pd.dismiss();
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    if (type.equalsIgnoreCase("success")) {

                        new TodoList_Fragment.GetToDoList().execute(user_id);
//                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
//                        builder.setMessage("Todo Task Updated Successfully");
//                        builder.setIcon(R.drawable.ic_success_24dp);
//                        builder.setTitle("Success");
//                        builder.setCancelable(false);
//                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//
//                            }
//                        });
//                        AlertDialog alertD = builder.create();
//                        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
//                        alertD.show();
                    } else {
                        Utilities.showAlertDialog(context, "Alert", message, false);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class UpdateTodoTask extends AsyncTask<String, Void, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context, R.style.CustomDialogTheme);
            pd.setMessage("Please wait ...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "update");
            obj.addProperty("list", params[0]);
            obj.addProperty("is_completed", params[1]);
            obj.addProperty("user_id", params[2]);
            obj.addProperty("id", params[3]);
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.TODOLISTAPI, obj.toString());
            return res.trim();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String type = "", message = "";
            try {
                pd.dismiss();
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    if (type.equalsIgnoreCase("success")) {

                        new TodoList_Fragment.GetToDoList().execute(user_id);
//                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
//                        builder.setMessage("Todo Task Updated Successfully");
//                        builder.setIcon(R.drawable.ic_success_24dp);
//                        builder.setTitle("Success");
//                        builder.setCancelable(false);
//                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//
//                            }
//                        });
//                        AlertDialog alertD = builder.create();
//                        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
//                        alertD.show();
                    } else {
                        Utilities.showAlertDialog(context, "Alert", message, false);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class DeleteTodoTask extends AsyncTask<String, Void, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context, R.style.CustomDialogTheme);
            pd.setMessage("Please wait ...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "delete");
            obj.addProperty("id", params[0]);
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.TODOLISTAPI, obj.toString());
            return res.trim();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String type = "", message = "";
            try {
                pd.dismiss();
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    if (type.equalsIgnoreCase("success")) {
                        new TodoList_Fragment.GetToDoList().execute(user_id);
//
//                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
//                        builder.setMessage("Todo Task Deleted Successfully");
//                        builder.setIcon(R.drawable.ic_success_24dp);
//                        builder.setTitle("Success");
//                        builder.setCancelable(false);
//                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//
//                            }
//                        });
//                        AlertDialog alertD = builder.create();
//                        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
//                        alertD.show();
                    } else {
                        Utilities.showAlertDialog(context, "Alert", message, false);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
