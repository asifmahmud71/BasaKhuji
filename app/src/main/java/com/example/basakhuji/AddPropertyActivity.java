package com.example.basakhuji;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AddPropertyActivity extends AppCompatActivity {


    private EditText divisionEditText, bedsEditText, bathsEditText, priceEditText, descriptionEditText, districtEditText, areaEditText, subAreaEditText;
    private TextView addedDateTextView;
    private Spinner categorySpinner, flatTypeSpinner, availableMonthSpinner;
    private FirebaseFirestore db;
    private Calendar calendar;

    StorageReference storageReference;
    Uri imageUri;
    private LinearLayout imagePreview;
    private ArrayList<Uri> imageUris = new ArrayList<>();
    private static final int PICK_IMAGES_REQUEST = 1;
    private static final int MAX_IMAGES = 4;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_property);

        db = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();

        ImageView backButton = findViewById(R.id.backButton);
        storageReference = FirebaseStorage.getInstance().getReference();

        divisionEditText = findViewById(R.id.divisionEditText);
        districtEditText = findViewById(R.id.districtEditText);
        areaEditText = findViewById(R.id.areaEditText);
        subAreaEditText = findViewById(R.id.subAreaEditText);
        bedsEditText = findViewById(R.id.bedsEditText);
        bathsEditText = findViewById(R.id.bathsEditText);
        availableMonthSpinner = findViewById(R.id.availableMonthSpinner);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        addedDateTextView = findViewById(R.id.addedDateTextView);
        priceEditText = findViewById(R.id.priceEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        flatTypeSpinner = findViewById(R.id.flatTypeSpinner);
        //imagePreview = findViewById(R.id.imagePreview);
        imagePreview = findViewById(R.id.imagePreview);


        findViewById(R.id.flatTypeLabel).setVisibility(View.GONE);
        flatTypeSpinner.setVisibility(View.GONE);


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

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Check which item is selected
                String selectedCategory = parent.getItemAtPosition(position).toString();
                if (selectedCategory.equals("Select Category")) {
                    findViewById(R.id.flatTypeLabel).setVisibility(View.GONE);
                    flatTypeSpinner.setVisibility(View.GONE);
                } else {
                    findViewById(R.id.flatTypeLabel).setVisibility(View.VISIBLE);
                    flatTypeSpinner.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        AppCompatButton submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputFields()) {
                    if (!imageUris.isEmpty()) {
                        uploadImagesAndSaveDetails();
                    } else {
                        Toast.makeText(AddPropertyActivity.this, "Please select images", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        Button imageUploadButton = findViewById(R.id.imageUploadButton);
        imageUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUris.size() < 5) {
                    openImagePicker();
                } else {
                    Toast.makeText(AddPropertyActivity.this, "You can select up to 5 images", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addedDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

    }

    private boolean validateInputFields() {
        if (divisionEditText.getText().toString().trim().isEmpty() ||
                districtEditText.getText().toString().trim().isEmpty() ||
                areaEditText.getText().toString().trim().isEmpty() ||
                subAreaEditText.getText().toString().trim().isEmpty() ||
                bedsEditText.getText().toString().trim().isEmpty() ||
                bathsEditText.getText().toString().trim().isEmpty() ||
                addedDateTextView.getText().toString().trim().isEmpty() ||
                priceEditText.getText().toString().trim().isEmpty() ||
                descriptionEditText.getText().toString().trim().isEmpty() ||
                categorySpinner.getSelectedItem().toString().equals("Select Category") ||
                flatTypeSpinner.getSelectedItem().toString().equals("Select Flat Type") ||
                availableMonthSpinner.getSelectedItem().toString().equals("Select Month")) {
            Toast.makeText(AddPropertyActivity.this, "Please fill in all property details", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    private void uploadImagesAndSaveDetails() {
        final List<String> imageUrls = new ArrayList<>();
        for (Uri uri : imageUris) {
            StorageReference reference = storageReference.child("images/" + UUID.randomUUID().toString());
            reference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                reference.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    imageUrls.add(downloadUri.toString());
                    if (imageUrls.size() == imageUris.size()) {
                        savePropertyDetails(imageUrls);
                    }
                }).addOnFailureListener(e -> Toast.makeText(AddPropertyActivity.this, "Failed to get image URL", Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e -> Toast.makeText(AddPropertyActivity.this, "Image uploading failed!", Toast.LENGTH_SHORT).show());
        }
    }


    private void savePropertyDetails(List<String> imageUrls) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(userId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String phoneNumber = documentSnapshot.getString("phoneNumber");

                    // Create property object
                    Map<String, Object> property = new HashMap<>();
                    property.put("division", divisionEditText.getText().toString().trim());
                    property.put("district", districtEditText.getText().toString().trim());
                    property.put("area", areaEditText.getText().toString().trim());
                    property.put("subArea", subAreaEditText.getText().toString().trim());
                    property.put("beds", bedsEditText.getText().toString().trim());
                    property.put("baths", bathsEditText.getText().toString().trim());
                    property.put("category", categorySpinner.getSelectedItem().toString());
                    property.put("addedDate", addedDateTextView.getText().toString().trim());
                    property.put("flatType", flatTypeSpinner.getSelectedItem().toString());
                    property.put("availableMonth", availableMonthSpinner.getSelectedItem().toString());
                    property.put("price", priceEditText.getText().toString().trim() + " BDT");
                    property.put("description", descriptionEditText.getText().toString().trim());
                    property.put("imageUrl", imageUrls); // Store the list of image URLs
                    property.put("userPhone", phoneNumber);
                    property.put("likedBy", new ArrayList<>());
                    property.put("dislikedBy", new ArrayList<>());

                    // Save the property details
                    db.collection("properties")
                            .add(property)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(AddPropertyActivity.this, "Property details added successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(AddPropertyActivity.this, "Failed to add property details", Toast.LENGTH_SHORT).show());
                }
            }).addOnFailureListener(e -> Toast.makeText(AddPropertyActivity.this, "Failed to get user phone number", Toast.LENGTH_SHORT).show());
        }
    }



    // Method to open image picker
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGES_REQUEST);
    }

    private void updateButton() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        addedDateTextView.setText(sdf.format(calendar.getTime()));
    }

    // Method to handle image selection result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        if (imageUris.size() < 5) {
                            imageUris.add(imageUri);
                        }
                    }
                } else if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    if (imageUris.size() < 5) {
                        imageUris.add(imageUri);
                    }
                }
                displaySelectedImages();
            }
        }
    }

    private void displaySelectedImages() {
        imagePreview.removeAllViews();
        for (Uri uri : imageUris) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                    200, 200));
            imageView.setImageURI(uri);
            imagePreview.addView(imageView);
        }
    }
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(AddPropertyActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateAddedDateTextView();
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void updateAddedDateTextView() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        addedDateTextView.setText(dateFormat.format(calendar.getTime()));
    }
}