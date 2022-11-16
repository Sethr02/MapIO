package com.opsc.guideio;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class PlacesActivity extends AppCompatActivity {

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

    RecyclerView placesRv;

    String x = "", y = "";

    // Bitmap Variable
    Bitmap imageBitmap;

    ActivityResultLauncher<Intent> activityResultLauncher;

    // Places Model ArrayList
    private ArrayList<PlacesModel> placesArrayList;

    // Instance of PlacesAdapter Class
    private PlacesAdapter placesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_select_option);
        initViews();

        // Getting Intent (GeeksforGeeks, 2019)
        Intent intent = getIntent();
        // Getting x intent (GeeksforGeeks, 2019)
        x = intent.getStringExtra("x");
        // Getting y intent (GeeksforGeeks, 2019)
        y = intent.getStringExtra("y");

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
            imageUri = saveImage(bm, PlacesActivity.this);
            uploadFile(imageUri);
        });

        loadPlaces();
    }

    private void loadPlaces() {
        // init array before adding data
        placesArrayList = new ArrayList<>();

        // Places Database Reference (Firebase, 2022)
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ImagesST10119434");
        // addValueEventListener (Firebase, 2022)
        ref.orderByChild("uid").equalTo(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Clear placesArrayList
                        placesArrayList.clear();
                        // DataSnapshot for loop (Firebase, 2022)
                        for (DataSnapshot ds: snapshot.getChildren()){
                            // Creating a PlacesModel with object from database
                            PlacesModel model = ds.getValue(PlacesModel.class);
                            // Adding model object to placesArrayList
                            placesArrayList.add(model);
                        }

                        // Setting Recycler View Layout Manager
                        placesRv.setLayoutManager(new LinearLayoutManager(PlacesActivity.this, LinearLayoutManager.VERTICAL, false));
                        // Setup Adapter
                        placesAdapter = new PlacesAdapter(PlacesActivity.this, placesArrayList);
                        // Set adapter to recyclerview
                        placesRv.setAdapter(placesAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
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

    //region Upload place to DB method
    private void uploadPlaceToDatabase(String uploadedImageUrl, String timestamp) {
        Log.d(TAG, "uploadPlaceToDatabase: Uploading Place To Storage");
        // Getting current users Unique id
        String uid = firebaseAuth.getUid();

        // Unique ID for place upload
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
                    Toast.makeText(PlacesActivity.this, "Item Successfully added!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Log
                    Log.d(TAG, "onFailure: Failed to upload due to " + e.getMessage());
                    // Toast to display when item has Failed!
                    Toast.makeText(PlacesActivity.this, "Failed to upload due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(PlacesActivity.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                                    // Image url
                                    String uploadedImageUrl = task.getResult().toString();
                                    // Passing image url and unique id to uploadPlaceToDatabase
                                    uploadPlaceToDatabase(uploadedImageUrl, timestamp);
                                })
                                // Displaying a toast for OnFailureListener
                                .addOnFailureListener(e -> Toast.makeText(PlacesActivity.this, "Failure due to: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                    })
                    // Toast for OnFailureListener
                    .addOnFailureListener(e -> Toast.makeText(PlacesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // Displaying a Toast if user hasn't selected an image
            Toast.makeText(this, "No image Selected!", Toast.LENGTH_SHORT).show();
        }
    }
    //endregion

    private void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(PlacesActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission (GeeksforGeeks, 2019)
            ActivityCompat.requestPermissions(PlacesActivity.this, new String[]{permission}, requestCode);
        } else {
            // calling cameraImageIntent (GeeksforGeeks, 2019)
            cameraImageIntent();
            Toast.makeText(PlacesActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(PlacesActivity.this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                // Displays toast if camera permission have been denied
                Toast.makeText(PlacesActivity.this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViews() {
        placesRv = findViewById(R.id.placesRv);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Places");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Menu Inflater
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_item_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // onOptionsItemSelected for menuCamera
        if (item.getItemId() == R.id.menuCamera) {
            //cameraImageIntent() (GeeksforGeeks, 2019)
            checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
        }
        return super.onOptionsItemSelected(item);
    }
}