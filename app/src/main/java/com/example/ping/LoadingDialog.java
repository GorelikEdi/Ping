package com.example.ping;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.airbnb.lottie.Lottie;

import java.util.Objects;

public class LoadingDialog extends DialogFragment {

    private Dialog dialog;

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.loading_dialog, null);

        return builder.setView(view).create();
    }

    public void dismiss(){
        dialog.dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        dialog = getDialog();
        Window window = dialog.getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
    }
}
