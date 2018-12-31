package com.elabram.lm.wmshwnp.rest;

import java.util.HashMap;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
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
    Call<ResponseBody> cekTimeZone(@Query("location") String coordinate,
                                   @Query("timestamp") String timestamp,
                                   @Query("key") String keyApi);

    @GET("https://maps.googleapis.com/maps/api/geocode/json")
    Call<ResponseBody> cekLocationName(@Query("latlng") String coordinate,
                                       @Query("key") String keyApi);

    // Geolocation
    @GET("https://www.googleapis.com/geolocation/v1/geolocate")
    Call<ResponseBody> cekGeolocation(@Query("key") String keyApi,
                                      @Body String body);

    @GET("https://maps.googleapis.com/maps/api/place/details/json")
    Call<ResponseBody> cekLocationNamePlaces(@Query("placeid") String placeId,
                                             @Query("key") String keyApi);


    @FormUrlEncoded
    @POST("attCheckin")
    Observable<ResponseBody> checkin(@FieldMap HashMap<String, String> paramsCheckin);

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

    @GET("server-time")
    Observable<ResponseBody> realtime();

    @FormUrlEncoded
    @POST("insert-livetracking")
    Observable<ResponseBody> liveTracking(@FieldMap HashMap<String, String> paramsLiveTracking);

//    @Multipart
//    @FormUrlEncoded
//    @POST("member-change-photo")
//    Observable<ResponseBody> addProfile(@FieldMap HashMap<String, String> params, Par);

    @Multipart
    @POST("member-change-photo")
    Observable<ResponseBody> addProfile(@Part("token") RequestBody token, @Part MultipartBody.Part file);

    @Multipart
    @POST("attCheckin")
    Observable<ResponseBody> recordWithPhoto(@Part("token") RequestBody token,
                                             @Part("location") RequestBody location_name,
                                             @Part("timezone") RequestBody timezone,
                                             @Part("timezone_id") RequestBody timezone_id,
                                             @Part("in_area") RequestBody in_area,
                                             @Part("remark") RequestBody remark,
                                             @Part("site_id") RequestBody site_id,
                                             @Part("lat") RequestBody lat,
                                             @Part("long") RequestBody lng,
                                             @Part MultipartBody.Part file);



    @FormUrlEncoded
    @POST("getProfile")
    Observable<ResponseBody> readProfile(@Field("token") String token);

    @FormUrlEncoded
    @POST("contract-check")
    Observable<ResponseBody> readContract(@Field("token") String token);
}
