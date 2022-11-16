package com.opsc.guideio;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class TempSelectOptionAct extends AppCompatActivity {

    // Firebase Variables (Firebase, 2022)
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private StorageTask mUploadTask;

    // Image uri variable
    private Uri imageUri = null;

    private static final int CAMERA_PERMISSION_CODE = 100;

    // Tag for debugging
    private static final String TAG = "Add_Photo_Tag";

    Button btnTakePic, btnViewPics;
    ImageView iconIv;
    String x = "", y = "";

    // Bitmap Variable
    Bitmap imageBitmap;

    ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_select_option);
        initViews();

        // Init firebaseAuth (Firebase, 2022)
        firebaseAuth = FirebaseAuth.getInstance();
        // Firebase Storage Reference (Firebase, 2022)
        storageReference = FirebaseStorage.getInstance().getReference("ImagesST10119434");
        // Firebase Database Reference (Firebase, 2022)
        databaseReference = FirebaseDatabase.getInstance().getReference("ImagesST10119434");

        // Activity Result for camera intent (GeeksforGeeks, 2019)
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            // Getting the image (Java2s.com, 2016)
            Bundle extras = result.getData().getExtras();
            // Converting image to bitmap (Java2s.com, 2016)
            imageBitmap = (Bitmap) extras.get("data");

            // Setting Bitmap properties (Java2s.com, 2016)
            WeakReference<Bitmap> result1 = new WeakReference<>(Bitmap.createScaledBitmap(imageBitmap,
                    imageBitmap.getHeight(), imageBitmap.getWidth(), false).copy(
                    Bitmap.Config.RGB_565, true));

            // Getting the final bitmap (Java2s.com, 2016)
            Bitmap bm = result1.get();
            // Passing the bitmap to the saveImage method
            imageUri = saveImage(bm, TempSelectOptionAct.this);
            uploadFile(imageUri);
            // Setting the image in the Image View
            iconIv.setImageURI(imageUri);
        });

        btnTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
            }
        });

        btnViewPics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TempSelectOptionAct.this, ViewImagesActivity.class));
            }
        });
    }

    //region Save Image Method
    private Uri saveImage(Bitmap image, Context context) {
        // Saving image temporarily (Java2s.com, 2016)
        File imagesFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            // (Java2s.com, 2016)
            imagesFolder.mkdir();
            File file = new File(imagesFolder, "captured_image.jpg");
            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context.getApplicationContext(), "com.opsc.guideio" + ".provider", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uri;
    }
    //endregion

    //region Upload book to DB method
    private void uploadBookToDatabase(String uploadedImageUrl, String timestamp) {
        Log.d(TAG, "uploadBookToDatabase: Uploading Book To Storage");
        // Getting current users Unique id
        String uid = firebaseAuth.getUid();

        // Unique ID for book upload
        String uploadId = "" + timestamp;

        PlacesModel place = new PlacesModel(uploadId, uid, uploadedImageUrl, x, y, timestamp);

        // Creating a new reference with unique id (Firebase, 2022)
        databaseReference.child(uploadId)
                // Passing new object (Firebase, 2022)
                .setValue(place)
                // On Successful upload (Firebase, 2022)
                .addOnSuccessListener(unused -> {
                    // Log
                    Log.d(TAG, "onSuccess: Successfully added");
                    // Toast to display when item is successfully added
                    Toast.makeText(TempSelectOptionAct.this, "Item Successfully added!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Log
                    Log.d(TAG, "onFailure: Failed to upload due to " + e.getMessage());
                    // Toast to display when item has Failed!
                    Toast.makeText(TempSelectOptionAct.this, "Failed to upload due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    //endregion

    //region Camera Intent
    private void cameraImageIntent() {
        // Capture Image Intent (GeeksforGeeks, 2019)
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Activity Result Launcher (GeeksforGeeks, 2019)
        activityResultLauncher.launch(takePictureIntent);
    }
    //endregion

    //region Get File Extension method
    private String getFileExtension(Uri uri) {
        // (Java2s.com, 2016)
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    //endregion

    //region Upload File Method
    private void uploadFile(Uri imageUri) {
        // Timestamp for unique id
        String timestamp = Long.toString(System.currentTimeMillis());

        if (imageUri != null) {
            // Storage reference to upload Image to Firebase Storage (Firebase, 2022)
            StorageReference fileReference = storageReference.child(timestamp
                    + "." + getFileExtension(imageUri));
            mUploadTask = fileReference.putFile(imageUri)
                    // OnSuccess Listener (Firebase, 2022)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Getting the download url of the uploaded image (Firebase, 2022)
                        fileReference.getDownloadUrl().addOnCompleteListener(task -> {
                                    // Display a Toast when upload is successful
                                    Toast.makeText(TempSelectOptionAct.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                                    // Image url
                                    String uploadedImageUrl = task.getResult().toString();
                                    // Passing image url and unique id to uploadBookToDatabase
                                    uploadBookToDatabase(uploadedImageUrl, timestamp);
                                })
                                // Displaying a toast for OnFailureListener
                                .addOnFailureListener(e -> Toast.makeText(TempSelectOptionAct.this, "Failure due to: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                    })
                    // Toast for OnFailureListener
                    .addOnFailureListener(e -> Toast.makeText(TempSelectOptionAct.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // Displaying a Toast if user hasn't selected an image
            Toast.makeText(this, "No image Selected!", Toast.LENGTH_SHORT).show();
        }
    }
    //endregion

    private void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(TempSelectOptionAct.this, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission (GeeksforGeeks, 2019)
            ActivityCompat.requestPermissions(TempSelectOptionAct.this, new String[]{permission}, requestCode);
        } else {
            // calling cameraImageIntent (GeeksforGeeks, 2019)
            cameraImageIntent();
            Toast.makeText(TempSelectOptionAct.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        // (GeeksforGeeks, 2019)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Displays toast if camera permission have been granted
                Toast.makeText(TempSelectOptionAct.this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                // Displays toast if camera permission have been denied
                Toast.makeText(TempSelectOptionAct.this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViews() {
        btnTakePic = findViewById(R.id.btnTakePic);
        btnViewPics = findViewById(R.id.btnViewPics);
        iconIv = findViewById(R.id.iconIv);
    }
}