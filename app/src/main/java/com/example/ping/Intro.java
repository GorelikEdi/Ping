package com.example.ping;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class Intro extends AppCompatActivity implements FirebaseAuthClient.FirebaseAuthClientListener {

    private ImageView logoIV;
    private TextView appNameTV;
    private TextView appTagLineTV;
    private LinearLayout layout;
    private PingSnackbar snackbar;
    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        PingMessagingService.APP_IS_UP = true;

        Animation introTopAnim = AnimationUtils.loadAnimation(this, R.anim.intro_top_anim);
        Animation introBottomAnim = AnimationUtils.loadAnimation(this, R.anim.intro_bottom_anim);

        layout = findViewById(R.id.activity_intro);
        logoIV = findViewById(R.id.logo);
        appNameTV = findViewById(R.id.title);
        appTagLineTV = findViewById(R.id.subtitle);

        logoIV.setAnimation(introTopAnim);
        appNameTV.setAnimation(introBottomAnim);
        appTagLineTV.setAnimation(introBottomAnim);

        snackbar = new PingSnackbar(getResources(), getTheme(), layout);
        connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        checkConnectivity();
    }

    private void checkConnectivity(){
        if (connectivityManager.getActiveNetworkInfo() == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            snackbar.set("Check your internet connection", R.drawable.network_icon, Snackbar.LENGTH_INDEFINITE);
            snackbar.getSnackbar().setAction("Retry", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkConnectivity();
                }
            });
            snackbar.show();
        }
        else
            startPingApp();
    }

    private void startPingApp(){
        FirebaseAuthClient.setFirebaseAuthClientListener(this);
        FirebaseAuthClient.setConnectivityManager(connectivityManager);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Pair[] pairs;
                Intent intent;

                if (FirebaseAuthClient.isUserLoggedIn())
                    pairs = new Pair[2];
                else
                    pairs = new Pair[3];

                pairs[0] = new Pair<View, String>(logoIV, "logo");
                pairs[1] = new Pair<View, String>(appNameTV, "title");

                if (FirebaseAuthClient.isUserLoggedIn()) {
                    intent = new Intent(Intro.this, MainActivity.class);
                }
                else {
                    pairs[2] = new Pair<View, String>(appTagLineTV, "subtitle");
                    intent = new Intent(Intro.this, SignIn.class);
                }

                ActivityOptions options = ActivityOptions
                        .makeSceneTransitionAnimation(Intro.this, pairs);

                if (getIntent().hasExtra("sender")) {
                    intent.putExtra("sender", getIntent().getStringExtra("sender"));
                    intent.putExtra("receiver", getIntent().getStringExtra("receiver"));
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent, options.toBundle());
                finishActivity();
            }
        }, 3000);
    }

    private void finishActivity(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finishAfterTransition();
            }
        }, 1500);
    }

    @Override
    public void onComplete(String errorMessage) {}

    @Override
    public void networkError() {}

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}