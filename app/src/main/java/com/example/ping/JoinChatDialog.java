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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.Objects;

public class JoinChatDialog extends DialogFragment {

    private ArrayList<String> chatNames = new ArrayList<>();
    private Button positiveButton;
    private JoinChatDialogListener joinChatDialogListener;
    private Chat chat;
    private TextInputLayout passwordTIL;
    private AutoCompleteTextView aCTV;
    private TextView chatMessage;
    private String userToken;

    interface JoinChatDialogListener{
        void joinChat(String chatName, String chatKey, boolean isNew);
    }

    public void setChatNames(ArrayList<String> chatNames){
        this.chatNames = chatNames;
    }

    public JoinChatDialog(String userToken){
        this.userToken = userToken;
    }

    public void setChat(Chat chat){
        if (chatMessage != null) {
            ViewGroup.LayoutParams paramsTV = chatMessage.getLayoutParams();
            ViewGroup.LayoutParams params = passwordTIL.getLayoutParams();
            this.chat = chat;
            if (chat.getType() == -1) {
                positiveButton.setEnabled(true);
                chatMessage.setText("You are already a member of this chat");
                paramsTV.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            } else {
                paramsTV.height = 0;
                passwordTIL.setError(null);
                passwordTIL.setErrorEnabled(false);
                if (chat.getPassword() != null) {
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                } else {
                    params.height = 0;
                    passwordTIL.getEditText().setText("");
                    positiveButton.setEnabled(true);
                }

            }
            passwordTIL.setLayoutParams(params);
            chatMessage.setLayoutParams(paramsTV);
        }
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.join_chat_dialog, null);

        joinChatDialogListener = (JoinChatDialogListener) getActivity();
        chatMessage = view.findViewById(R.id.chat_message);
        aCTV = view.findViewById(R.id.autoCompleteTV);
        passwordTIL = view.findViewById(R.id.password);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.autocomplete_item, R.id.suggest_tv, chatNames);
        aCTV.setThreshold(2);
        aCTV.setAdapter(adapter);

        aCTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FirebaseRDBClient.getSingleChatToJoin(aCTV.getText().toString());
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
                if (Validator.validatePassword(passwordTIL)){
                    if (passwordTIL.getEditText().getText().toString().equals(chat.getPassword())) {
                        positiveButton.setEnabled(true);
                        passwordTIL.setError(null);
                        passwordTIL.setErrorEnabled(false);
                    }
                    else{
                        positiveButton.setEnabled(false);
                        passwordTIL.setErrorEnabled(true);
                        passwordTIL.setError("Wrong password");
                    }
                }
            }
        });


        return builder
                .setView(view)
                .setTitle("Join Group Chat")
                .setPositiveButton("Join", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        joinChatDialogListener.joinChat(chat.getName(), chat.getKey(), chat.getType() != -1);
                        if (chat.getType() != -1) {
                            PingMessagingService.addUserToGroupChat(userToken, getContext(), chat);
                        }
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
}
