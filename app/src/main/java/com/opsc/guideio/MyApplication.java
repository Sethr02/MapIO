package com.opsc.guideio;

import static com.opsc.guideio.Constants.ONE_MEGABYTE;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MyApplication {

    public static void loadImageFromURL(String imageUrl, ImageView image) {
        // Log variable
        String TAG = "LOAD_IMAGE_FROM_URL_TAG";

        // Storage reference for place images
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        ref.getBytes(ONE_MEGABYTE)
                // OnSuccess listener
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Decoding bitmap from url
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        // Setting the bitmap to the Image View
                        image.setImageBitmap(bmp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) { }
                });
    }

    public static void deletePlace(Context context, String placeId, String url) {
        // Log TAG
        String TAG = "DELETE_place_TAGE";

        // Log
        Log.d(TAG, "deletePlace: Deleting.....");

        // Log
        Log.d(TAG, "deletePlace: Deleting from Storage...");

        // Storage reference for place image
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
        // Delete image
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Log
                        Log.d(TAG, "onSuccess: Deleted from Storage.");

                        // Log
                        Log.d(TAG, "onSuccess: Now deleting info from DB");

                        // Database Reference for places
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ImagesST10119434");

                        // Removing selected place from database
                        reference.child(placeId)
                                .removeValue()
                                // OnSuccess Listener
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        // Log
                                        Log.d(TAG, "onSuccess: Deleted from Database.");
                                        // display toast on success
                                        Toast.makeText(context, "Place Deleted Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                // on failure listener
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Log
                                        Log.d(TAG, "onSuccess: Failed to delete from Database due to " + e.getMessage());
                                        // display toast on failure
                                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Log
                        Log.d(TAG, "onSuccess: Failed to delete from Storage due to " + e.getMessage());
                        // display toast on failure
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
