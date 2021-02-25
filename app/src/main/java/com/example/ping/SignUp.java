package com.example.ping;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class SignUp extends AppCompatActivity implements FirebaseAuthClient.FirebaseAuthClientListener, FirebaseRDBClient.FirebaseRDBClientListener {

    private ScrollView scrollView;
    private TextView birthDateTV;
    private TextInputLayout emailTIL;
    private TextInputLayout passTIL;
    private TextInputLayout confirmPassTIL;
    private TextInputLayout usernameTIL;
    private boolean isUsernameExists = true;
    private boolean isEmailExists = true;
    private PingSnackbar snackbar;
    private ConnectivityManager connectivityManager;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        scrollView = findViewById(R.id.scroll_view);
        birthDateTV = findViewById(R.id.birth_date);
        emailTIL = findViewById(R.id.email);
        passTIL = findViewById(R.id.password);
        confirmPassTIL = findViewById(R.id.confirm_password);
        usernameTIL = findViewById(R.id.username);

        connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);


        snackbar = new PingSnackbar(getResources(), getTheme(), scrollView);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                confirmPassTIL.setVisibility(View.VISIBLE);
                usernameTIL.setVisibility(View.VISIBLE);
                birthDateTV.setVisibility(View.VISIBLE);
            }
        }, 300);


        Objects.requireNonNull(emailTIL.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (Validator.validateEmail(emailTIL)){
                    FirebaseAuthClient.checkEmail(emailTIL.getEditText().getText().toString(),
                            emailTIL,
                            null);
                }
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

        Objects.requireNonNull(confirmPassTIL.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                Validator.validateConfirmPassword(passTIL, confirmPassTIL);
            }
        });

        Objects.requireNonNull(usernameTIL.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (Validator.validateUsername(usernameTIL, "Username")){
                    FirebaseRDBClient.checkUsername(usernameTIL.getEditText().getText().toString());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseAuthClient.setFirebaseAuthClientListener(this);
        FirebaseRDBClient.setFirebaseRDBClientListener(this);
    }

    public void birthDate(View view){
        MaterialDatePicker.Builder<Long> materialDateBuilder =
                MaterialDatePicker.Builder.datePicker();
        materialDateBuilder.setTitleText("Birthdate");
        final MaterialDatePicker<Long>  materialDatePicker = materialDateBuilder.build();
        materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
        materialDatePicker.addOnPositiveButtonClickListener(
                new MaterialPickerOnPositiveButtonClickListener<Long> () {
                    @Override
                    public void onPositiveButtonClick(Long selection) {
                        Date date = new Date(selection);
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat simpleDateFormat =
                                new SimpleDateFormat("dd/MM/yyyy");
                        birthDateTV.setText(simpleDateFormat.format(date));
                        birthDateTV.setTextColor(Color.parseColor("#000000"));
                    }
                });
    }

    public void signIn(View view){
        returnToSignIn();
    }

    public void signUp(View view) {
        if (connectivityManager.getActiveNetworkInfo() != null) {
            if (Validator.validateAll(emailTIL, passTIL, confirmPassTIL, usernameTIL)
                    && !isUsernameExists && !isEmailExists) {
                String birthDate = birthDateTV.getText().toString();
                if (birthDate.equals("Birth date"))
                    birthDate = null;
                String username = Objects.requireNonNull(usernameTIL.getEditText()).getText().toString();
                String email = Objects.requireNonNull(emailTIL.getEditText()).getText().toString();
                String password = Objects.requireNonNull(passTIL.getEditText()).getText().toString();
                SharedPreferences sharedPreferences = getSharedPreferences("pingApp", MODE_PRIVATE);
                String token = sharedPreferences.getString("token", null);
                FirebaseAuthClient.createUser(
                        new User(birthDate, username, null, email, token, null), password
                );

                returnToSignIn();
            }
        }
        else{
            snackbar.set("Check your internet connection", R.drawable.network_icon, Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }

    private void returnToSignIn(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                confirmPassTIL.setVisibility(View.INVISIBLE);
                usernameTIL.setVisibility(View.INVISIBLE);
                birthDateTV.setVisibility(View.INVISIBLE);
            }
        }, 100);
        finishAfterTransition();
    }

    @Override
    public void onComplete(String errorMessage) {
        isEmailExists = errorMessage != null;
    }

    @Override
    public void networkError() {
        snackbar.set("Check your internet connection", R.drawable.network_icon, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        returnToSignIn();
    }

    @Override
    public void onUserExists(boolean isExists) {
        isUsernameExists = isExists;
        if (!isExists){
            usernameTIL.setError(null);
            usernameTIL.setErrorEnabled(false);
        }
        else {
            usernameTIL.setErrorEnabled(true);
            usernameTIL.setError("User with this username already exists");
        }
    }

    @Override
    public void applyUser(User user) {

    }


    @Override
    public void applyChat(Chat chat, boolean isFirstLoad) {

    }

    @Override
    public void applyUsers(ArrayList<String> users, boolean isStart) {

    }

    @Override
    public void chatCreated(String chatName, String chatKey) {

    }

    @Override
    public void applyGroupChat(Chat chat) {

    }

    @Override
    public void privateChatCreated(String key) {

    }

    @Override
    public void applyChatNames(ArrayList<String> chatNames, boolean isCreate) {

    }

    @Override
    public void applyChatToJoin(Chat chat) {

    }

}