package com.example.luka.delivery;

import android.content.Intent;
import android.media.session.MediaSession;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

/*import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;*/
import com.example.luka.delivery.entities.AccessToken;
import com.example.luka.delivery.entities.ApiError;
import com.example.luka.delivery.network.ApiService;
import com.example.luka.delivery.network.RetrofitBuilder;
import com.facebook.stetho.Stetho;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class loginActivity extends AppCompatActivity {

    ApiService service;
    TokenManager tokenManager;
    //AwesomeValidation validator;
    Call<AccessToken> call;

    private static final String TAG = "loginActivity";
    @BindView(R.id.editTextEmailLogin) EditText editTextEmailLogin;
    @BindView(R.id.editTextPasswordLogin) EditText editTextPasswordLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Stetho.initializeWithDefaults(this);

        ButterKnife.bind(this);

        service = RetrofitBuilder.createService(ApiService.class);

        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));

        Log.i("tokenLog",String.valueOf(tokenManager.getToken().getAccessToken()));


        if(tokenManager.getToken().getAccessToken() != null){
            startActivity(new Intent(loginActivity.this, mapActivity.class));
            finish();
        }

        //validator = new AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT);
        //setupRules();

/*        if(tokenManager.getToken().getAccessToken() != null){
            startActivity(new Intent(loginActivity.this, postActivity.class));
            finish();
        }*/

        editTextEmailLogin.setText("luka@luka.com"); //testing purposes
        editTextPasswordLogin.setText("lukaluka"); //testing purposes

    }

    @OnClick(R.id.buttonRegisterLogin)
    void go_to_register(){
        startActivity(new Intent(loginActivity.this, registerActivity.class));
    }

    @OnClick(R.id.ButtonLogin)
    void login(){
        final String email = editTextEmailLogin.getText().toString();
        String password = editTextPasswordLogin.getText().toString();

        call = service.login(email,password);
        call.enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {

                Log.w(TAG, "onResponse: " + response);

                if (response.isSuccessful()) {
                    tokenManager.saveToken(response.body());
                    Log.w(TAG, "onResponse: " + response.body());
                    Intent intent = new Intent (loginActivity.this, mapActivity.class);
                    intent.putExtra("usernameMail", email);
                    startActivity(intent);

                } else{
                    if(response.code() == 422){
                        handleErrors(response.errorBody());
                    }
                    if(response.code() == 401){
                        ApiError apiError = Utils.converErrors((response.errorBody()));
                        Toast.makeText(loginActivity.this, apiError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AccessToken> call, Throwable t) {

            }
        });

    }

    public void handleErrors(ResponseBody response){

        ApiError apiError = Utils.converErrors(response);

        apiError.getErrors();

        for(Map.Entry<String, List<String>> error : apiError.getErrors().entrySet()){

            if(error.getKey().equals("username")){
                editTextEmailLogin.setError(error.getValue().get(0));
            }
            if(error.getKey().equals("password")){
                editTextPasswordLogin.setError(error.getValue().get(0));
            }
        }

    }

    /*private void setupRules() {

        validator.addValidation(loginActivity.this, R.id.editTextEmailLogin, Patterns.EMAIL_ADDRESS, R.string.err_email);
        validator.addValidation(loginActivity.this, R.id.editTextPasswordLogin, RegexTemplate.NOT_EMPTY, R.string.err_password);

    }*/

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(call != null) {
            call.cancel();
            call = null;
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}
