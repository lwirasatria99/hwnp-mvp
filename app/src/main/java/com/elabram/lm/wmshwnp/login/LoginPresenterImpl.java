package com.elabram.lm.wmshwnp.login;

import android.support.annotation.NonNull;
import android.util.Log;

import com.elabram.lm.wmshwnp.rest.ApiClient;
import com.elabram.lm.wmshwnp.rest.ApiClientLogin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.elabram.lm.wmshwnp.utilities.AppInfo.PREFS_LOGGED;

public class LoginPresenterImpl implements LoginContract.Presenter {

    private static final String TAG = LoginPresenterImpl.class.getSimpleName();
    private LoginView view;
    private LoginRepository loginRepository;

    LoginPresenterImpl(LoginView view, LoginRepository loginRepository) {
        this.view = view;
        this.loginRepository = loginRepository;
    }

    @Override
    public void retrofitLogin() {
        view.showDialog();
        Call<ResponseBody> call = new ApiClientLogin().getApiService().login(
                view.getTextUser(),
                view.getTextPass(),
                "1",
                view.getImei());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                view.dismissDialog();
                try {
                    if (response.body() != null) {
                        //noinspection ConstantConditions
                        String responseContent = response.body().string();
                        Log.e(TAG, "onResponse: Login -> " + responseContent);

                        try {
                            JSONObject jsonObject = new JSONObject(responseContent);
                            String message = jsonObject.getString("message");

                            if (message.equalsIgnoreCase("success")) {
                                // data
                                JSONObject jsonData = jsonObject.getJSONObject("data");
                                String token = jsonData.getString("token");
                                String user_id = jsonData.getString("user_id");
                                String user_name = jsonData.getString("user_name");
                                String user_email = jsonData.getString("user_email");
                                String user_type = jsonData.getString("user_type");
                                // user_data
                                JSONObject jsonUserData = jsonData.getJSONObject("user_data");
                                String userdata_name = jsonUserData.getString("name");
                                String mem_nip = jsonUserData.getString("mem_nip");
                                String mem_id = jsonUserData.getString("mem_id");
                                String mem_mobile = jsonUserData.getString("mem_mobile");
                                String mem_phone = jsonUserData.getString("mem_phone");
                                String mem_address = jsonUserData.getString("mem_address");
                                String mem_image = jsonUserData.getString("mem_image");
                                String position = jsonUserData.getString("position");

                                loginRepository.putBoolean(PREFS_LOGGED, true);
                                loginRepository.putString("email", view.getTextUser());
                                loginRepository.putString("password", view.getTextPass());
                                loginRepository.putString("user_id", user_id);
                                loginRepository.putString("user_name", user_name);
                                loginRepository.putString("user_email", user_email);
                                loginRepository.putString("user_type", user_type);
                                loginRepository.putString("token", token);

                                loginRepository.putString("name", userdata_name);
                                loginRepository.putString("mem_id", mem_id);
                                loginRepository.putString("mem_nip", mem_nip);
                                loginRepository.putString("mem_mobile", mem_mobile);
                                loginRepository.putString("mem_phone", mem_phone);
                                loginRepository.putString("mem_image", mem_image);
                                loginRepository.putString("mem_address", mem_address);
                                loginRepository.putString("position", position);

                                view.gotoCheckinV1Activity();
                            } else {
                                loginRepository.putBoolean(PREFS_LOGGED, false);

                                if (view.getImei() != null) {
                                    view.showSnackbar(message);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
                if (view.getImei() != null) {
                    view.showSnackbar("Check your network / please try again");
                } else {
                    view.showSnackbar("Please allow the permission to get your permission id");
                }
                view.dismissDialog();
            }
        });
    }

    @Override
    public void retrofitCheckVersion() {
        Call<ResponseBody> call = new ApiClient().getApiService().checkVersion();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.body() != null) {
                        //noinspection ConstantConditions
                        String mResponse = new String(response.body().bytes());
                        //Log.e(TAG, "onResponse: CheckVersion " + mResponse);
                        JSONObject jsonObject = new JSONObject(mResponse);
                        String response_code = jsonObject.getString("response_code");
                        String message = jsonObject.getString("message");

                        switch (response_code) {
                            case "401":
                                view.showSnackbar(message);
                                break;
                            case "200":
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    String s_version = jsonObject1.getString("version");
                                    if (!s_version.equals(view.getVersion())) {
                                        view.dialogCheckVersion();
                                    }
                                }

                                break;
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: Version " + t.getMessage());
            }
        });
    }

    @Override
    public void onDestroy() {
        view = null;
    }

    @Override
    public void login() {
        if (view.getImei() != null) {
            retrofitLogin();
        } else {
            view.getImei();
            retrofitLogin();
        }
    }

}
