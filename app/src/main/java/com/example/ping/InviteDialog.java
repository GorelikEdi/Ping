package com.example.ping;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.util.ArrayList;
import java.util.Objects;

public class InviteDialog extends DialogFragment {

    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<Message> messages;
    private Button positiveButton;
    private String chatKey;
    private ChatFragment groupFragment;
    private String chatName;

    public void setChatKey(String chatKey){
        this.chatKey = chatKey;
    }

    public void setNames(ArrayList<String> names){
        names.remove(FirebaseAuthClient.getCurrentUser().getDisplayName());
        this.names = names;
    }

    public void setGroupMessages(ArrayList<Message> messages){ this.messages = messages;}

    public void setGroupFragment(ChatFragment groupFragment){ this.groupFragment = groupFragment;}

    public void setChatName(String chatName) {this.chatName = chatName;}

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.invite_dialog, null);
        final AutoCompleteTextView aCTV = view.findViewById(R.id.autoCompleteTV);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.autocomplete_item, R.id.suggest_tv, names);
        aCTV.setThreshold(2);
        aCTV.setAdapter(adapter);
        aCTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                positiveButton.setEnabled(true);
            }
        });

        return builder
                .setView(view)
                .setTitle("Invite user")
                .setPositiveButton("Invite", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseRDBClient.addUserToGroupChatUsers(chatKey, aCTV.getText().toString());
                        FirebaseRDBClient.inviteUserToGroupChat(aCTV.getText().toString(), getContext(), chatKey, chatName);
                        Message message = new Message(AppConfig.getInstance().getLabel_prefix() + aCTV.getText().toString()+ " has added to chat",
                                FirebaseAuthClient.getCurrentUser().getDisplayName(), chatKey,null);
                        PingMessagingService.sendMessage(message, getContext());
                        FirebaseRDBClient.uploadMessage(message, Chat.CURRENT_CHAT);
                        messages.add(message);
                        groupFragment.notifyAdapter();

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
