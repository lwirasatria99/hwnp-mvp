package com.elabram.lm.wmshwnp.change_password;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.elabram.lm.wmshwnp.R;
import com.elabram.lm.wmshwnp.rest.ApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.elabram.lm.wmshwnp.utilities.AppInfo.PREFS_LOGIN;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.token;

public class ChangePasswordActivity extends AppCompatActivity implements ChangePasswordView {

    private static final String TAG = ChangePasswordActivity.class.getSimpleName();
    @BindView(R.id.etNew)
    EditText etNew;

    @BindView(R.id.etConfirm)
    EditText etConfirm;

    @BindView(R.id.buttonSubmit)
    Button buttonSubmit;

    @BindView(R.id.rootView)
    LinearLayout rootView;

    @BindView(R.id.iv_back)
    ImageView iv_back;

    private ProgressDialog progressDialog;
    private String password;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ButterKnife.bind(this);
        getSharedUserDetail();

        progressDialog = new ProgressDialog(this, R.style.AppThemeLoading);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Please wait...");

        iv_back.setOnClickListener(view -> finish());

        submitNewPassword();
    }

    private void submitNewPassword() {
        buttonSubmit.setOnClickListener(view -> {

            // Hide Keyboard
            View view2 = this.getCurrentFocus();
            if (view2 != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(view2.getWindowToken(), 0);
            }

            if (getTextNew().equals(getTextConfirm())) {
                if (!getTextNew().isEmpty() && !getTextConfirm().isEmpty()) {
                    password = getTextNew();
                    retrofitChangePassword();
                } else {
                    Snackbar.make(rootView, "Make sure no empty form", Snackbar.LENGTH_LONG).show();
                }
            } else {
                Snackbar.make(rootView, "Your password doesn't match", Snackbar.LENGTH_LONG).show();
            }

        });
    }

    private String getTextNew() {
        return etNew.getText().toString();
    }

    private String getTextConfirm() {
        return etConfirm.getText().toString();
    }

    private void getSharedUserDetail() {
        SharedPreferences preferences = getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
    }

    private void showProgress() {
        progressDialog.show();
    }

    private void retrofitChangePassword() {
        showProgress();
        Call<ResponseBody> call = new ApiClient().getApiService().changePassword(getParamsChange());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                dismissProgress();
                if (response.body() != null) {
                    try {
                        //noinspection ConstantConditions
                        String contentResponse = new String(response.body().bytes());
                        Log.e(TAG, "onResponse ChangedPassword: " + contentResponse);
                        JSONObject jsonObject = new JSONObject(contentResponse);
                        String message = jsonObject.getString("message");
                        String response_code = jsonObject.getString("response_code");
                        if (response_code.equals("200")) {
                            showSnackBar(message);
                            etNew.setText("");
                            etConfirm.setText("");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                dismissProgress();
                Snackbar.make(rootView, "Please try again", Snackbar.LENGTH_LONG).show();
                Log.e(TAG, "onFailure: Change Password " + t.getCause());
            }
        });
    }

    private HashMap<String, String> getParamsChange() {
        HashMap<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("password", password);
        return params;
    }

    @Override
    public void dismissProgress() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    @Override
    public void showSnackBar(String message) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
    }

}
