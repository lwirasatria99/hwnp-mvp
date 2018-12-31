package com.elabram.lm.wmshwnp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.elabram.lm.wmshwnp.rest.ApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import static com.elabram.lm.wmshwnp.utilities.AppInfo.PREFS_LOGIN;
import static com.elabram.lm.wmshwnp.utilities.AppInfo.token;

public class FeedbackActivity extends AppCompatActivity {

    private static final String TAG = FeedbackActivity.class.getSimpleName();
    @BindView(R.id.etFeedback)
    EditText etFeedback;

    @BindView(R.id.buttonSubmit)
    Button buttonSubmit;

    @BindView(R.id.rootView)
    LinearLayout rootView;

    @BindView(R.id.iv_back)
    ImageView iv_back;

    @BindView(R.id.et_subject)
    EditText et_subject;

    private ProgressDialog progressDialog;

    private Disposable disposable;
    private String message;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        ButterKnife.bind(this);
        getSharedUserDetail();

        progressDialog = new ProgressDialog(this, R.style.AppThemeLoading);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Please wait...");

        submitClick();
        backClick();
    }

    private void backClick() {
        iv_back.setOnClickListener(view -> finish());
    }

    private void submitClick() {
        buttonSubmit.setOnClickListener(view -> {
            hideKeyboard();
            if (!getTextFeedback().isEmpty() && !getTextSubject().isEmpty()) {
                retrofitFeedback();
            } else {
                Snackbar.make(rootView, "Fill your empty form", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void hideKeyboard() {
        View view2 = getCurrentFocus();
        if (view2 != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(view2.getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null)
            disposable.dispose();
    }

    private void retrofitFeedback() {
        progressDialog.show();

        Observable<ResponseBody> call = new ApiClient().getApiService().feedback(getParamsFeedback());
        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        Log.e(TAG, "onNext: ");
                        if (responseBody != null) {
                            try {
                                String response = responseBody.string();

                                JSONObject jsonObject = new JSONObject(response);
                                String response_code = jsonObject.getString("response_code");
                                if (response_code.equals("200")) {
                                    //message = jsonObject.getString("message");
                                    message = "Your feedback already recorded. Thanks very much.";
                                    etFeedback.setText("");
                                    et_subject.setText("");
                                } else {
                                    message = jsonObject.getString("message");
                                }
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        progressDialog.dismiss();
                        Snackbar.make(rootView, R.string.check_connection, Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onComplete() {
                        progressDialog.dismiss();
                        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void getSharedUserDetail() {
        SharedPreferences preferences = getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
    }

    private HashMap<String, String> getParamsFeedback() {
        HashMap<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("subject", getTextSubject());
        params.put("issue", getTextFeedback());
        params.put("document", "");
        return params;
    }

    private String getTextSubject() {
        return et_subject.getText().toString();
    }

    private String getTextFeedback() {
        return etFeedback.getText().toString();
    }
}
