package com.elabram.lm.wmshwnp.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.elabram.lm.wmshwnp.R;
import com.elabram.lm.wmshwnp.model.Task;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *
 * Created by lalu.mahendra on 16-May-18.
 */

public class TaskAdapter extends BaseAdapter {

    private Activity mActivity;
    private List<Task> tasks;
    private LayoutInflater inflater;

    public TaskAdapter(Activity mActivity, List<Task> tasks) {
        this.mActivity = mActivity;
        this.tasks = tasks;
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (inflater == null)
            inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            assert inflater != null;
            view = inflater.inflate(R.layout.row_task, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        Task task = tasks.get(i);

        String date_range = task.getDate_assignment();

        holder.date.setText(date_range);
        holder.location_name.setText(task.getLocation_name());
        holder.time_start.setText(task.getTime_start());
        holder.time_end.setText(task.getTime_end());
        holder.description.setText(task.getDesc());

        return view;
    }

    static class ViewHolder {
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.location_name)
        TextView location_name;
        @BindView(R.id.time_start) TextView time_start;
        @BindView(R.id.time_end) TextView time_end;
        @BindView(R.id.description) TextView description;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

    }
}
