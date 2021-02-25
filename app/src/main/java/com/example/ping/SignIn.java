package com.example.ping;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Objects;

public class SignIn extends AppCompatActivity implements FirebaseAuthClient.FirebaseAuthClientListener {

    private ImageView logoIV;
    private TextView welcomeTV;
    private TextView signInTV;
    private Button loginBtn;
    private Button signUpBtn;
    private Button forgotPassword;
    private TextInputLayout emailTIL;
    private TextInputLayout passTIL;
    private RelativeLayout layout;
    private PingSnackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        layout = findViewById(R.id.sign_in_layout);
        logoIV = findViewById(R.id.logo);
        welcomeTV = findViewById(R.id.welcome);
        signInTV = findViewById(R.id.signin);
        emailTIL = findViewById(R.id.email);
        passTIL = findViewById(R.id.password);
        loginBtn = findViewById(R.id.login_btn);
        signUpBtn = findViewById(R.id.signup_btn);
        forgotPassword = findViewById(R.id.forgot_password);

        snackbar = new PingSnackbar(getResources(), getTheme(), layout);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                emailTIL.setVisibility(View.VISIBLE);
                passTIL.setVisibility(View.VISIBLE);
                loginBtn.setVisibility(View.VISIBLE);
                signUpBtn.setVisibility(View.VISIBLE);
                forgotPassword.setVisibility(View.VISIBLE);
            }
        }, 400);

        Objects.requireNonNull(emailTIL.getEditText()).addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

             @Override
             public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

             @Override
             public void afterTextChanged(Editable editable) {
                 Validator.validateEmail(emailTIL);
             }
        });

        Objects.requireNonNull(passTIL.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                Validator.validatePassword(passTIL);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseAuthClient.setFirebaseAuthClientListener(this);
    }

    public void forgotPass(View view){
        ResetPassword resetPassword = new ResetPassword();
        resetPassword.show(getSupportFragmentManager(), "RESET_PASSWORD_TAG");
    }

    public void login(View view){
        if (Validator.validateAll(emailTIL, passTIL, null, null)) {
            String email = Objects.requireNonNull(emailTIL.getEditText()).getText().toString();
            String password = Objects.requireNonNull(passTIL.getEditText()).getText().toString();
            FirebaseAuthClient.signIn(email, password);
        }
    }

    public void signUp(View view){
        Pair[] pairs = new Pair[7];
        pairs[0] = new Pair<View, String>(logoIV, "logo");
        pairs[1] = new Pair<View, String>(welcomeTV, "title");
        pairs[2] = new Pair<View, String>(signInTV, "subtitle");
        pairs[3] = new Pair<View, String>(emailTIL, "email");
        pairs[4] = new Pair<View, String>(passTIL, "password");
        pairs[5] = new Pair<View, String>(loginBtn, "proceed_btn");
        pairs[6] = new Pair<View, String>(signUpBtn, "account_btn");
        ActivityOptions options = ActivityOptions
                .makeSceneTransitionAnimation(SignIn.this, pairs);
        Intent intent = new Intent(SignIn.this, SignUp.class);
        startActivity(intent, options.toBundle());
    }

    @Override
    public void onComplete(String errorMessage) {

        if (errorMessage == null) {
            emailTIL.setError(null);
            emailTIL.setErrorEnabled(false);
            passTIL.setError(null);
            passTIL.setErrorEnabled(false);
            Pair[] pairs = new Pair[2];
            pairs[0] = new Pair<View, String>(logoIV, "logo");
            pairs[1] = new Pair<View, String>(welcomeTV, "title");
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(SignIn.this, pairs);
            Intent intent = new Intent(SignIn.this, MainActivity.class);
            startActivity(intent, options.toBundle());
            signInTV.setVisibility(View.INVISIBLE);
            finishActivity();
        }
        else {
            emailTIL.setError(null);
            emailTIL.setErrorEnabled(false);
            passTIL.setError(null);
            passTIL.setErrorEnabled(false);

            switch (errorMessage) {
                case "email":
                    emailTIL.setErrorEnabled(true);
                    emailTIL.setError("Wrong email address");
                    break;
                case "password":
                    passTIL.setErrorEnabled(true);
                    passTIL.setError("Wrong password");
            }
        }
    }

    @Override
    public void networkError() {
        snackbar.set("Check your internet connection", R.drawable.network_icon, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void finishActivity(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finishAfterTransition();
            }
        }, 1000);
    }
}