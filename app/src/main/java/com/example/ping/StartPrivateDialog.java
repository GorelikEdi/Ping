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

public class StartPrivateDialog extends DialogFragment {

    private ArrayList<String> names = new ArrayList<>();
    private Button positiveButton;
    private StartPrivateDialogListener startPrivateDialogListener;

    interface StartPrivateDialogListener{
        void startPrivateChat(String username);
    }

    public void setNames(ArrayList<String> names){
        names.remove(FirebaseAuthClient.getCurrentUser().getDisplayName());
        this.names = names;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.start_private_chat_dialog, null);
        final AutoCompleteTextView aCTV = view.findViewById(R.id.autoCompleteTV);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.autocomplete_item, R.id.suggest_tv, names);
        aCTV.setThreshold(2);
        aCTV.setAdapter(adapter);
        startPrivateDialogListener = (StartPrivateDialogListener) getActivity();
        aCTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                positiveButton.setEnabled(true);
            }
        });

        return builder
                .setView(view)
                .setTitle("Start Private Chat")
                .setPositiveButton("Start", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startPrivateDialogListener.startPrivateChat(aCTV.getText().toString());
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
