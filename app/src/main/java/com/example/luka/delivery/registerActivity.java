package com.example.luka.delivery;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import com.example.luka.delivery.entities.AccessToken;
import com.example.luka.delivery.entities.ApiError;
import com.example.luka.delivery.network.ApiService;
import com.example.luka.delivery.network.RetrofitBuilder;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;*/

public class registerActivity extends AppCompatActivity {

    public static final String TAG = "mapActivity";

    @BindView(R.id.editTextNameRegister) EditText editTextNameRegister;
    @BindView(R.id.editTextEmailRegister) EditText editTextEmailRegister;
    @BindView(R.id.editTextPasswordRegister) EditText editTextPasswordRegister;

    ApiService service;
    Call<AccessToken> call;
    //AwesomeValidation validator;
    TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

        service = RetrofitBuilder.createService(ApiService.class);
        //validator = new AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT);

        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        //setupRules();
        /*Switch sw = (Switch)findViewById(R.id.switch1);

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){ //ON

                }else{ //OFF

                }
            }
        });*/

        /*if(tokenManager.getToken().getAccessToken() != null){
            startActivity(new Intent(registerActivity.this, postActivity.class));
            finish();
        }*/

    }

    @OnClick(R.id.buttonRegisterRegister)
    void register(){

        String name = editTextNameRegister.getText().toString();
        String email = editTextEmailRegister.getText().toString();
        String password = editTextPasswordRegister.getText().toString();
        String requiredRole = "Driver";

        editTextNameRegister.setError(null);
        editTextEmailRegister.setError(null);
        editTextPasswordRegister.setError(null);

        call = service.register(name, email, password, requiredRole);
            call.enqueue(new Callback<AccessToken>() {

                @Override
                public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                    if (response.isSuccessful()) {
                        Log.w(TAG, "onResponse " + response.body());
                        tokenManager.saveToken(response.body());
                        startActivity(new Intent(registerActivity.this, loginActivity.class));
                        finish();
                    } else {
                        handleErrors(response.errorBody());
                    }
                }

                @Override
                public void onFailure(Call<AccessToken> call, Throwable t) {
                    Log.w(TAG, "onFailure: " + t.getMessage());
                }
            });
    }

    private void handleErrors(ResponseBody response){

        ApiError apiError = Utils.converErrors(response);

        for(Map.Entry<String, List<String>> error : apiError.getErrors().entrySet()){

            if(error.getKey().equals("name")){
                editTextNameRegister.setError(error.getValue().get(0));
            }
            if(error.getKey().equals("email")){
                editTextEmailRegister.setError(error.getValue().get(0));
            }
            if(error.getKey().equals("password")){
                editTextPasswordRegister.setError(error.getValue().get(0));
            }
        }

    }

    /*public void setupRules(){

        validator.addValidation(registerActivity.this, R.id.editTextNameRegister, RegexTemplate.NOT_EMPTY, R.string.err_name);
        validator.addValidation(registerActivity.this, R.id.editTextEmailRegister, Patterns.EMAIL_ADDRESS, R.string.err_email);
        validator.addValidation(registerActivity.this, R.id.editTextPasswordRegister, "[a-zA-Z0-9]{6,}", R.string.err_password);

    }*/

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(call != null) {
            call.cancel();
            call = null;
        }
    }
}
