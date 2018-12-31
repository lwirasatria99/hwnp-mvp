package com.elabram.lm.wmshwnp.model;

/**
 *
 * Created by lalu.mahendra on 16-May-18.
 */

public class Task {

    private String task_id, location_name;
    private String desc, time_start, time_end;
    private String by;
    private String lat;
    private String lng;
    private String status;
    private String date_assignment;

    public Task() {
    }

    public String getDate_assignment() {
        return date_assignment;
    }

    public void setDate_assignment(String date_assignment) {
        this.date_assignment = date_assignment;
    }

    public String getTime_start() {
        return time_start;
    }

    public void setTime_start(String time_start) {
        this.time_start = time_start;
    }

    public String getTime_end() {
        return time_end;
    }

    public void setTime_end(String time_end) {
        this.time_end = time_end;
    }

    public String getLocation_name() {
        return location_name;
    }

    public void setLocation_name(String location_name) {
        this.location_name = location_name;
    }

    public String getTask_id() {
        return task_id;
    }

    public void setTask_id(String task_id) {
        this.task_id = task_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }
}
