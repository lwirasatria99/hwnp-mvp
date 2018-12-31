package com.elabram.lm.wmshwnp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elabram.lm.wmshwnp.adapter.TaskAdapter;
import com.elabram.lm.wmshwnp.model.Task;
import com.elabram.lm.wmshwnp.utilities.AppInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.elabram.lm.wmshwnp.utilities.AppInfo.token;

public class ChooseTaskActivity extends AppCompatActivity {

    @BindView(R.id.listview)
    ListView listView;
    @BindView(R.id.progressBar)
    ProgressBar progressDialog;
    @BindView(R.id.tvNoData)
    TextView tvNoData;

    private String TAG = ChooseTaskActivity.class.getSimpleName();

    private TaskAdapter adapter;
    private List<Task> taskList;

    private String s_nodata = "-No Data-";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        ButterKnife.bind(this);

        taskList = new ArrayList<>();
        adapter = new TaskAdapter(this, taskList);

        getSharedUserDetail();
        volleyGETListTask();

        listView.setOnItemClickListener(this::listClick);
    }


    @SuppressWarnings("unused")
    private void listClick(AdapterView<?> adapterView, View view, int i, long l) {
        Task task = taskList.get(i);
        String task_id = task.getTask_id();
        String location_name = task.getLocation_name();
        String date_ = task.getDate_assignment();
        String lat_ = task.getLat();
        String long_ = task.getLng();

        SharedPreferences preferences = getSharedPreferences("listclick", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("task_id", task_id);
        editor.putString("location_name", location_name);
        editor.putString("date_", date_);
        editor.putString("lat_", lat_);
        editor.putString("long_", long_);
        editor.apply();

        startActivity(new Intent(this, CheckinActivity.class));
    }

    private void getSharedUserDetail() {
        SharedPreferences preferences = getSharedPreferences(AppInfo.PREFS_LOGIN, Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
    }


    private void volleyGETListTask() {
        progressDialog.setVisibility(View.VISIBLE);

        String API = AppInfo.PRE_URL + "/taskAssignment-detailTaskList";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, API, response -> {
            Log.e(TAG, "onResponse: " + response);
            progressDialog.setVisibility(View.GONE);

            try {
                JSONObject jsonObject = new JSONObject(response);
                String message = jsonObject.getString("message");
                if (message.equals("success")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    if (jsonArray.length() <= 0) {
                        tvNoData.setText(s_nodata);
                        listView.setEmptyView(tvNoData);
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);

                        String task_id = jsonObject1.getString("task_id");
                        String description = jsonObject1.getString("description");
                        String assign_by = jsonObject1.getString("assign_by");
                        String lat = jsonObject1.getString("lat");
                        String lng = jsonObject1.getString("long");
                        String status = jsonObject1.getString("status");
//                        String start_assignment = jsonObject1.getString("start_assignment");
//                        String end_assignment = jsonObject1.getString("end_assignment");
                        String date_assignment = jsonObject1.getString("date_assignment");
                        String location_name = jsonObject1.getString("location_name");
                        String start_time = jsonObject1.getString("start_time");
                        String end_time = jsonObject1.getString("end_time");

                        Task task = new Task();
                        task.setDesc(description);
                        task.setBy(assign_by);
                        task.setLat(lat);
                        task.setLng(lng);
                        task.setStatus(status);
                        task.setTask_id(task_id);
                        task.setDate_assignment(date_assignment);
                        task.setLocation_name(location_name);
                        task.setTime_start(start_time);
                        task.setTime_end(end_time);
                        taskList.add(task);
                        listView.setAdapter(adapter);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            adapter.notifyDataSetChanged();
        }, error -> {

            Activity c = ChooseTaskActivity.this;
            AppInfo info = new AppInfo();
            info.errorResponse(error, c);
            progressDialog.setVisibility(View.GONE);
        }) {

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> params = new HashMap<>();

                params.put("token", token);

                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
