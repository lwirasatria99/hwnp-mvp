package com.elabram.lm.wmsmobile.rest;

import java.util.HashMap;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @FormUrlEncoded
    @POST("login")
    Call<ResponseBody> login(@Field("mem_email") String email,
                             @Field("mem_password") String password,
                             @Field("isDevice") String isDevice,
                             @Field("is_imei") String isImei);

//    @FormUrlEncoded
//    @POST("listCustomer")
//    Call<ResponseBody> listLogo(@Field("token") String token);

    @FormUrlEncoded
    @POST("listCustomer")
    Observable<ResponseBody> listLogo(@Field("token") String token);

    @FormUrlEncoded
    @POST("attDetail")
    Call<ResponseBody> loadStatusCheckin(@Field("token") String token);

    @GET("https://maps.googleapis.com/maps/api/timezone/json")
    Call<ResponseBody> cekTimeZone(@Query("location") String coordinat,
                                   @Query("timestamp") String timestamp,
                                   @Query("key") String keyApi);

//    @FormUrlEncoded
//    @POST("attCheckin")
//    Call<ResponseBody> checkin(@Field("token") String token,
//                               @Field("location") String location,
//                               @Field("timezone") String timezone);

    @FormUrlEncoded
    @POST("attCheckin")
    Observable<ResponseBody> checkin(@Field("token") String token,
                                     @Field("location") String location,
                                     @Field("timezone") String timezone);

    @FormUrlEncoded
    @POST("siteList")
    Call<ResponseBody> siteList(@Field("token") String token);

    @FormUrlEncoded
    @POST("attendance-list")
    Call<ResponseBody> monthlyList(@Field("token") String token,
                                   @Field("att_date[0]") String attDate0,
                                   @Field("att_date[1]") String attDate1,
                                   @Field("mem_id_req") String mem_id_req);

    @POST("get-version")
    Call<ResponseBody> checkVersion();

    @FormUrlEncoded
    @POST("changepassword")
    Call<ResponseBody> changePassword(@FieldMap HashMap<String, String> paramsChange);

    @FormUrlEncoded
    @POST("support-add")
    Observable<ResponseBody> feedback(@FieldMap HashMap<String, String> paramsFeedback);

    @FormUrlEncoded
    @POST("greeting-list")
    Observable<ResponseBody> listGreeting(@FieldMap HashMap<String, String> paramsGreeting);

    @FormUrlEncoded
    @POST("chartAtt")
    Observable<ResponseBody> readChart(@FieldMap HashMap<String, String> paramsChart);


    @FormUrlEncoded
    @POST("gps-violation")
    Observable<ResponseBody> fakeGPS(@FieldMap HashMap<String, String> paramsFakeGPS);
}
