package com.example.ping;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import java.util.Objects;

public class ImageDialog extends DialogFragment {

    private Uri uri;
    public static int REQUEST_STORAGE_PERMISSION = 5;
    private Context context;
    private Activity activity;
    private Bitmap imageBitmap;
    private ContentResolver contentResolver;
    private ImageDialogListener imageDialogListener;
    private Dialog dialog;


    interface ImageDialogListener{
        void applyResponse(String text);
    }

    public void setUri(Uri uri){
        this.uri = uri;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.image_dialog, null);

        context = getContext();
        activity = getActivity();

        imageDialogListener = (ImageDialogListener) activity;
        contentResolver = context.getContentResolver();
        ImageButton save = view.findViewById(R.id.save);
        ImageButton close = view.findViewById(R.id.close);
        ImageView imageView = view.findViewById(R.id.image_dialog);

        Glide.with(Objects.requireNonNull(getContext())).load(uri).into(imageView);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(Objects.requireNonNull(getContext()))
                        .asBitmap()
                        .load(uri)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap bitmap, Transition<? super Bitmap> transition) {
                                imageBitmap = bitmap;
                                requestStoragePermission();
                            }
                            @Override
                            public void onLoadCleared(Drawable placeholder) {
                            }
                        });
            }
        });


        return builder.setView(view).create();
    }

    private void saveToGallery(){
        MediaStore.Images.Media.insertImage(contentResolver, imageBitmap, "Ping" , "Ping");
        imageDialogListener.applyResponse("Image saved!");
        dialog.dismiss();
    }

    private void requestStoragePermission() {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        else{
            saveToGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveToGallery();
            }
            else {
                imageDialogListener.applyResponse("Allow gallery access");
                dialog.dismiss();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        dialog = getDialog();
        Window window = dialog.getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
    }
}
