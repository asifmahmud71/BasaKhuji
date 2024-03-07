package com.example.basakhuji;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AddPropertyActivity extends AppCompatActivity {


    private EditText locationEditText, bedsEditText, bathsEditText, priceEditText, descriptionEditText;
    private TextView addedDateTextView;
    private Spinner categorySpinner, flatTypeSpinner, availableMonthSpinner;
    private FirebaseFirestore db;
    private Calendar calendar;

    private static final int PICK_IMAGES_REQUEST = 1;
    private ArrayList<Uri> selectedImages;
    private LinearLayout imagePreviewLayout;
    private ImageView imagePreview;
    StorageReference storageReference;
    Uri imageUri;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_property);

        db = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();

        storageReference = FirebaseStorage.getInstance().getReference();
        locationEditText = findViewById(R.id.locationEditText);
        bedsEditText = findViewById(R.id.bedsEditText);
        bathsEditText = findViewById(R.id.bathsEditText);
        availableMonthSpinner = findViewById(R.id.availableMonthSpinner);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        addedDateTextView = findViewById(R.id.addedDateTextView);
        priceEditText = findViewById(R.id.priceEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        flatTypeSpinner = findViewById(R.id.flatTypeSpinner);
        imagePreview = findViewById(R.id.imagePreview);

        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        availableMonthSpinner.setAdapter(monthAdapter);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.category_options, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        ArrayAdapter<CharSequence> flatTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.flat_type_options, android.R.layout.simple_spinner_item);
        flatTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        flatTypeSpinner.setAdapter(flatTypeAdapter);

        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri != null) {
                    saveImageStorage(imageUri);
                } else {
                    Toast.makeText(AddPropertyActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button imageUploadButton = findViewById(R.id.imageUploadButton);
        imageUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImagePicker();
            }
        });

        addedDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

    private void savePropertyDetails(String imageUrl) {
        String location = locationEditText.getText().toString().trim();
        String beds = bedsEditText.getText().toString().trim();
        String baths = bathsEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        String flatType = flatTypeSpinner.getSelectedItem().toString();
        String availableMonth = availableMonthSpinner.getSelectedItem().toString();
        String description = descriptionEditText.getText().toString().trim();
        String addedDate = addedDateTextView.getText().toString().trim();
        String price = priceEditText.getText().toString().trim();


        Map<String, Object> property = new HashMap<>();
        property.put("location", location);
        property.put("beds", beds);
        property.put("baths", baths);
        property.put("category", category);
        property.put("addedDate", addedDate);
        property.put("flatType", flatType);
        property.put("availableMonth", availableMonth);
        property.put("price", price + " BDT");
        property.put("description", description);

        // Store the image URL
        property.put("imageUrl", imageUrl);

        // Initialize likedBy field as empty list
        property.put("likedBy", new ArrayList<>());

        // Initialize dislikedBy field as empty list
        property.put("dislikedBy", new ArrayList<>());

        db.collection("properties")
                .add(property)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddPropertyActivity.this, "Property details added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(AddPropertyActivity.this, "Error adding property details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Method to open image picker
    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGES_REQUEST);
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateButton();
            }
        };

        new DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateButton() {
        String myFormat = "dd/MM/yyyy"; // Change this format as needed
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        addedDateTextView.setText(sdf.format(calendar.getTime()));
    }

    // Method to handle image selection result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_REQUEST && data != null ) {

                imageUri = data.getData();
                imagePreview.setImageURI(imageUri);

        }
    }

    // Method to save image to storage
    private void saveImageStorage(Uri imageUri){
        StorageReference reference = storageReference.child("images/" + UUID.randomUUID().toString());
        reference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Get the download URL of the uploaded image
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Image uploaded successfully, now save property details with image URL
                        savePropertyDetails(uri.toString());
                        Toast.makeText(AddPropertyActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddPropertyActivity.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddPropertyActivity.this, "Image uploading failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
