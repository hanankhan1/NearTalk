package com.example.neartalk;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profile extends AppCompatActivity {

    private ImageView imgProfile;
    private EditText etName, etEmail, etNeighbourhood, etAbout;
    private MaterialButton btnSave;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        db.collection("users").document(auth.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        startActivity(new Intent(this, DashBoard.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {

                });

        imgProfile = findViewById(R.id.imgProfile);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etNeighbourhood = findViewById(R.id.etNeighbourhood);
        etAbout = findViewById(R.id.etAbout);
        btnSave = findViewById(R.id.btnSaveProfile);
        TextView btnAddPhoto = findViewById(R.id.btnAddPhoto);

        etEmail.setText(auth.getCurrentUser().getEmail());

        ActivityResultLauncher<String> imagePicker =
                registerForActivityResult(new ActivityResultContracts.GetContent(),
                        uri -> {
                            if (uri != null) {
                                imageUri = uri;
                                imgProfile.setImageURI(uri);
                            }
                        });

        btnAddPhoto.setOnClickListener(v -> imagePicker.launch("image/*"));
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String neighbourhood = etNeighbourhood.getText().toString().trim();
        String about = etAbout.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(neighbourhood) || TextUtils.isEmpty(about)) {
            Snackbar.make(btnSave, "All fields are required", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (about.split("\\s+").length > 150) {
            Snackbar.make(btnSave, "About section exceeds 150 words", Snackbar.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("userName", name)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean isUnique = true;

                    for (DocumentSnapshot doc : querySnapshot) {
                        if (!doc.getId().equals(auth.getUid())) {
                            isUnique = false;
                            break;
                        }
                    }

                    if (!isUnique) {
                        Snackbar.make(btnSave, "Username already taken, choose another", Snackbar.LENGTH_SHORT).show();
                    } else {
                        if (imageUri != null) {
                            uploadImageAndSaveProfile(name, email, neighbourhood, about);
                        } else {
                            saveProfileToFirestore(name, email, neighbourhood, about, "");
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Snackbar.make(btnSave, "Error checking username uniqueness", Snackbar.LENGTH_LONG).show()
                );
    }


    private void uploadImageAndSaveProfile(String name, String email, String neighbourhood, String about) {
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("profile_images/" + auth.getUid() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(uri ->
                                saveProfileToFirestore(
                                        name,
                                        email,
                                        neighbourhood,
                                        about,
                                        uri.toString()
                                )
                        ))
                .addOnFailureListener(e ->
                        Snackbar.make(btnSave, "Image upload failed", Snackbar.LENGTH_LONG).show());
    }

    private void saveProfileToFirestore(String name, String email, String neighbourhood,
                                        String about, String imageUrl) {
        String uid = auth.getUid();
        UserProfile userProfile = new UserProfile(
                uid,
                name,
                email,
                neighbourhood,
                about,
                imageUrl
        );

        db.collection("users")
                .document(uid)
                .set(userProfile)
                .addOnSuccessListener(unused -> {
                    Snackbar.make(btnSave, "Profile saved successfully", Snackbar.LENGTH_LONG).show();
                    createAreaGroupIfNotExists(neighbourhood);
                    startActivity(new Intent(this, DashBoard.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Snackbar.make(btnSave, "Failed to save profile", Snackbar.LENGTH_LONG).show());
    }
    private String normalizeArea(String area) {
        return area.trim().toLowerCase().replaceAll("\\s+", "_");
    }
    private void createAreaGroupIfNotExists(String neighbourhood) {
        if (TextUtils.isEmpty(neighbourhood)) return;

        String areaId = normalizeArea(neighbourhood);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = auth.getUid();

        db.collection("areaGroups").document(areaId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        List<String> initialMembers = new ArrayList<>();
                        initialMembers.add(uid);

                        AreaGroup newGroup = new AreaGroup(
                                areaId,
                                neighbourhood,
                                System.currentTimeMillis(),
                                1,
                                initialMembers,
                                "",
                                0L,
                                ""
                        );

                        db.collection("areaGroups").document(areaId)
                                .set(newGroup)
                                .addOnSuccessListener(aVoid ->
                                        Snackbar.make(btnSave, "Area group created: " + neighbourhood, Snackbar.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e ->
                                        Snackbar.make(btnSave, "Failed to create area group", Snackbar.LENGTH_SHORT).show()
                                );
                    }
                })
                .addOnFailureListener(e ->
                        Snackbar.make(btnSave, "Error checking group existence", Snackbar.LENGTH_SHORT).show()
                );
    }

}