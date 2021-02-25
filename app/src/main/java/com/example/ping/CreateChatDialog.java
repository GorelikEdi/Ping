package com.example.ping;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.Objects;

public class CreateChatDialog extends DialogFragment {

    private Button positiveButton;
    private final String token;
    private ArrayList<String> chatNames;

    public void setChatNames(ArrayList<String> chatNames){
        this.chatNames = chatNames;
    }

    public CreateChatDialog(String token){
        this.token = token;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.create_chat_dialog, null);

        final TextInputEditText chatNameTIET = view.findViewById(R.id.chat_name);
        final TextInputLayout passwordTIL = view.findViewById(R.id.password);
        final SwitchMaterial passwordSwitch = view.findViewById(R.id.password_switch);
        final TextInputLayout chatNameTIL = view.findViewById(R.id.chat_name_til);

        chatNameTIET.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

             }

             @Override
             public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

             }

             @Override
             public void afterTextChanged(Editable editable) {
                 if (passwordSwitch.isChecked()) {
                     if (Validator.validatePassword(passwordTIL)) {
                         if (Validator.validateUsername(chatNameTIL, "Chat name")) {
                             if (!checkChatName(chatNameTIET.getText().toString())) {
                                 positiveButton.setEnabled(true);
                                 chatNameTIL.setError(null);
                                 chatNameTIL.setErrorEnabled(false);
                             }
                             else{
                                 positiveButton.setEnabled(false);
                                 chatNameTIL.setErrorEnabled(true);
                                 chatNameTIL.setError("Chat name already exists");
                             }
                         }
                         else
                             positiveButton.setEnabled(false);
                     }
                 }
                 else{
                     if (Validator.validateUsername(chatNameTIL, "Chat name")) {
                         if (!checkChatName(chatNameTIET.getText().toString())) {
                             positiveButton.setEnabled(true);
                             chatNameTIL.setError(null);
                             chatNameTIL.setErrorEnabled(false);
                         }
                         else{
                             positiveButton.setEnabled(false);
                             chatNameTIL.setErrorEnabled(true);
                             chatNameTIL.setError("Chat name already exists");
                         }
                     }
                     else
                         positiveButton.setEnabled(false);
                 }
             }
         });

        passwordTIL.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (Validator.validatePassword(passwordTIL)) {
                    if (Validator.validateUsername(chatNameTIL, "Chat name")) {
                        if (!checkChatName(chatNameTIET.getText().toString())) {
                            positiveButton.setEnabled(true);
                            chatNameTIL.setError(null);
                            chatNameTIL.setErrorEnabled(false);
                        } else {
                            positiveButton.setEnabled(false);
                            chatNameTIL.setErrorEnabled(true);
                            chatNameTIL.setError("Chat name already exists");
                        }
                    }
                    else
                        positiveButton.setEnabled(false);
                }
                else
                    positiveButton.setEnabled(false);
            }
        });

        passwordSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                ViewGroup.LayoutParams params = passwordTIL.getLayoutParams();
                positiveButton.setEnabled(false);
                if (isChecked){
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                }
                else{
                    params.height = 0;
                    passwordTIL.getEditText().setText("");
                    if (Validator.validateUsername(chatNameTIL, "Chat name")) {
                        if (!checkChatName(chatNameTIET.getText().toString())) {
                            positiveButton.setEnabled(true);
                            chatNameTIL.setError(null);
                            chatNameTIL.setErrorEnabled(false);
                        }
                        else{
                            positiveButton.setEnabled(false);
                            chatNameTIL.setErrorEnabled(true);
                            chatNameTIL.setError("Chat name already exists");
                        }
                    }
                    else
                        positiveButton.setEnabled(false);
                }
                passwordTIL.setLayoutParams(params);

            }
        });

        return builder
                .setView(view)
                .setTitle("Create Group Chat")
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String password = null;
                        if (passwordSwitch.isChecked())
                            password = passwordTIL.getEditText().getText().toString();
                        PingMessagingService.createChat(chatNameTIET.getText().toString(), password,
                                getContext(), token);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();
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

    private boolean checkChatName(String chatName){
        for (String chatname : chatNames){
            if (chatname.equals(chatName))
                return true;
        }
        return false;
    }
}
