package com.example.ping;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class FirebaseStorageClient {
    private static FirebaseStorage firebaseStorage = null;
    private static FirebaseStorageClient.FirebaseStorageClientListener firebaseStorageClientListener;
    private static StorageReference storageReference = null;

    interface FirebaseStorageClientListener {
        void applyPhotoUri(Uri uri, boolean firstLoad);
    }

    public static void setFirebaseStorageClientListener(Context context){
        firebaseStorageClientListener = (FirebaseStorageClientListener) context;
    }

    private static void setFirebaseStorage() {
        firebaseStorage = FirebaseStorage.getInstance();
    }

    private static FirebaseStorage getFirebaseStorage(){
        if (firebaseStorage == null) {
            setFirebaseStorage();
        }
        return firebaseStorage;
    }

    private static void setStorageReference(){
        storageReference = getFirebaseStorage().getReference();
    }

    private static StorageReference getStorageReference(){
        setStorageReference();
        return storageReference;
    }

    public static void getPhoto(String filename, final boolean firstLoad){
        if (filename.length() == 72)
            filename = "images/" + filename;
        getStorageReference().child(filename + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                firebaseStorageClientListener.applyPhotoUri(uri, firstLoad);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                firebaseStorageClientListener.applyPhotoUri(null, firstLoad);
            }
        });
    }

    public static void uploadPhoto(Uri uri, final String filename){
        String filenameNew;
        if (filename.length() == 72)
            filenameNew = "images/" + filename;
        else
            filenameNew = "users/" + filename;
       getStorageReference().child(filenameNew + ".jpg")
               .putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
           @Override
           public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
               String newFilename;
               if (filename.length() == 72)
                   newFilename = "images/" + filename;
               else
                   newFilename = "users/" + filename;
               getPhoto(newFilename, false);
           }
       });
    }

}
