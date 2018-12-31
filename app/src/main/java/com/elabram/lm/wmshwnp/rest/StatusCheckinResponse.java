package com.elabram.lm.wmshwnp.rest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StatusCheckinResponse {

    @SerializedName("error")
    @Expose
    private Object error;

    @SerializedName("message")
    @Expose
    private Object message;

    @SerializedName("response_code")
    @Expose
    private Object responseCode;

    @SerializedName("data")
    @Expose
    private List<Object> data = null;


}
