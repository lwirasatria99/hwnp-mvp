package com.elabram.lm.wmsmobile.rest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("getProfile")
    Call<ResponseBody> loadProfileParams(@Field("token") String token);

    @FormUrlEncoded
    @POST("attDetail")
    Call<ResponseBody> loadStatusCheckin(@Field("token") String token);

    @FormUrlEncoded
    @POST("attCheckin")
    Call<ResponseBody> checkin(@Field("token") String token,
                               @Field("location") String site_name);

    @FormUrlEncoded
    @POST("siteList")
    Call<ResponseBody> siteList(@Field("token") String token);

    @FormUrlEncoded
    @POST("attendance-list")
    Call<ResponseBody> monthlyList(@Field("token") String token,
                                   @Field("att_date[0]") String attDate0,
                                   @Field("att_date[1]") String attDate1,
                                   @Field("mem_id_req") String mem_id_req);
}