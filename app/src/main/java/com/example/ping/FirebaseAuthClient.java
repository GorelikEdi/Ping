package com.example.ping;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Button;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.auth.UserProfileChangeRequest;
import java.util.Objects;

public class FirebaseAuthClient {

    private static FirebaseAuth firebaseAuth = null;
    private static FirebaseAuthClientListener firebaseAuthClientListener;
    private static ConnectivityManager cm;

    interface FirebaseAuthClientListener {
        void onComplete(String errorMessage);
        void networkError();
    }

    public static void setFirebaseAuthClientListener(Context context){
        firebaseAuthClientListener = (FirebaseAuthClientListener) context;
    }

    public static void setConnectivityManager(ConnectivityManager cm){
        FirebaseAuthClient.cm = cm;
    }

    private static void setFirebaseAuth() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private static FirebaseAuth getFirebaseAuth(){
        if (firebaseAuth == null) {
            setFirebaseAuth();
        }
        return firebaseAuth;
    }

    private static boolean isNetworkAvailable() {
        if (cm.getActiveNetworkInfo() == null) {
            firebaseAuthClientListener.networkError();
            return false;
        }
        else
            return true;
    }

    public static void checkEmail(String email, final TextInputLayout emailTIL,
                                  final Button positiveButton){
        if (isNetworkAvailable()) {
            getFirebaseAuth().fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {

                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                            boolean isUserExists = !Objects.requireNonNull(Objects
                                    .requireNonNull(task.getResult()).getSignInMethods()).isEmpty();
                            if (positiveButton == null)
                                isUserExists = !isUserExists;
                            if (isUserExists) {
                                emailTIL.setError(null);
                                emailTIL.setErrorEnabled(false);
                                if (positiveButton != null)
                                    positiveButton.setEnabled(true);
                                else
                                    firebaseAuthClientListener.onComplete(null);
                            } else {
                                emailTIL.setErrorEnabled(true);
                                if (positiveButton != null) {
                                    positiveButton.setEnabled(false);
                                    emailTIL.setError("No user associated with this email address");
                                }
                                else {
                                    emailTIL.setError("User with this email address already exists");
                                    firebaseAuthClientListener.onComplete("exist");
                                }
                            }
                        }
                    });
        }
    }

    public static void resetEmail(String email){
        getFirebaseAuth().sendPasswordResetEmail(email);
    }

    public static FirebaseUser getCurrentUser(){
        if (isNetworkAvailable())
            return getFirebaseAuth().getCurrentUser();
        else
            return null;
    }

    public static void signOut(){
        getFirebaseAuth().signOut();
    }

    public static boolean isUserLoggedIn(){
        return getCurrentUser() != null;
    }

    public static void createUser(final User user, String password){
        if (isNetworkAvailable()) {
            getFirebaseAuth().createUserWithEmailAndPassword(user.getEmail(), password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(user.getUsername()).build();

                        getCurrentUser().updateProfile(profileUpdates);
                        FirebaseRDBClient.uploadUser(user);
                    }
                }
            });

        }
    }

    public static void signIn(String email, String password) {
        if (isNetworkAvailable()) {
            getFirebaseAuth().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                firebaseAuthClientListener.onComplete(null);
                            } else {
                                String errorMessage = null;
                                try {
                                    throw Objects.requireNonNull(task.getException());
                                } catch (FirebaseAuthInvalidUserException e) {
                                    errorMessage = "email";
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    errorMessage = "password";
                                } catch (Exception e) {
                                    errorMessage = e.toString();
                                } finally {
                                    firebaseAuthClientListener.onComplete(errorMessage);
                                }
                            }
                        }
                    });
        }
    }

}
