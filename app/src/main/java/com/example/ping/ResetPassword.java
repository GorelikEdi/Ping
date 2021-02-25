package com.example.ping;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Objects;

public class ResetPassword extends DialogFragment implements FirebaseAuthClient.FirebaseAuthClientListener {

    private TextInputLayout emailTIL;
    private Button positiveButton;

    @NonNull
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        final View view = inflater.inflate(R.layout.reset_password_dialog, null);

        emailTIL = view.findViewById(R.id.email);

        Objects.requireNonNull(emailTIL.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (Validator.validateEmail(emailTIL)){
                    String email = emailTIL.getEditText().getText().toString();
                    FirebaseAuthClient.checkEmail(email, emailTIL, positiveButton);
                }
            }
        });

        builder.setView(view)
                .setTitle("Reset Password")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String email = emailTIL.getEditText().getText().toString();
                        FirebaseAuthClient.resetEmail(email);
                    }
                });
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null){
            positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setEnabled(false);
        }
    }

    @Override
    public void networkError() {
        Toast.makeText(getContext(), "Check your internet connection", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onComplete(String errorMessage) {

    }
}
