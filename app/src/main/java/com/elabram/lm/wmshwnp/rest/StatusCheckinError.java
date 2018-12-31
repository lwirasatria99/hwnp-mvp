package com.elabram.lm.wmshwnp.rest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StatusCheckinError {

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("response_code")
    @Expose
    private Integer responseCode;

    @SerializedName("data")
    @Expose
    private Object data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
//
//    public class Object {
//
//        @SerializedName("is_date")
//        @Expose
//        private String isDate;
//        @SerializedName("time_first")
//        @Expose
//        private String timeFirst;
//        @SerializedName("location_first")
//        @Expose
//        private String locationFirst;
//        @SerializedName("time_last")
//        @Expose
//        private String timeLast;
//        @SerializedName("location_last")
//        @Expose
//        private String locationLast;
//
//        public String getIsDate() {
//            return isDate;
//        }
//
//        public void setIsDate(String isDate) {
//            this.isDate = isDate;
//        }
//
//        public String getTimeFirst() {
//            return timeFirst;
//        }
//
//        public void setTimeFirst(String timeFirst) {
//            this.timeFirst = timeFirst;
//        }
//
//        public String getLocationFirst() {
//            return locationFirst;
//        }
//
//        public void setLocationFirst(String locationFirst) {
//            this.locationFirst = locationFirst;
//        }
//
//        public String getTimeLast() {
//            return timeLast;
//        }
//
//        public void setTimeLast(String timeLast) {
//            this.timeLast = timeLast;
//        }
//
//        public String getLocationLast() {
//            return locationLast;
//        }
//
//        public void setLocationLast(String locationLast) {
//            this.locationLast = locationLast;
//        }
//    }

}
