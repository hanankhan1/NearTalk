package com.example.neartalk;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {

    private ImageView imgProfile;
    private TextView tvName, tvEmail, tvNeighbourhood, tvAbout;
    private EditText etName, etNeighbourhood, etAbout;
    private MaterialButton btnEdit, btnSave, btnCancel, btnLogout;
    private Button btnChangePhoto;
    private ViewGroup viewContainer;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private Uri imageUri;
    private boolean isEditMode = false;

    // Image picker
    private ActivityResultLauncher<String> imagePicker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        // Initialize views
        imgProfile = view.findViewById(R.id.imgProfile);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvNeighbourhood = view.findViewById(R.id.tvNeighbourhood);
        tvAbout = view.findViewById(R.id.tvAbout);
        etName = view.findViewById(R.id.etName);
        etNeighbourhood = view.findViewById(R.id.etNeighbourhood);
        etAbout = view.findViewById(R.id.etAbout);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnChangePhoto = view.findViewById(R.id.btnChangePhoto);
        viewContainer = view.findViewById(R.id.viewContainer);

        // Initialize image picker
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        imgProfile.setImageURI(uri);
                    }
                }
        );

        // Load user profile data
        loadUserProfile();

        // Set up click listeners
        btnEdit.setOnClickListener(v -> enableEditMode());
        btnSave.setOnClickListener(v -> saveProfile());
        btnCancel.setOnClickListener(v -> disableEditMode());
        btnLogout.setOnClickListener(v -> logoutUser());
        btnChangePhoto.setOnClickListener(v -> changeProfilePicture());

        // Initially hide edit fields
        disableEditMode();

        return view;
    }

    private void loadUserProfile() {
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                        if (userProfile != null) {
                            // Display data
                            tvName.setText(userProfile.getUserName());
                            tvEmail.setText(userProfile.getEmail());
                            tvNeighbourhood.setText(userProfile.getNeighbourhood());
                            tvAbout.setText(userProfile.getAbout());

                            // Also set edit fields
                            etName.setText(userProfile.getUserName());
                            etNeighbourhood.setText(userProfile.getNeighbourhood());
                            etAbout.setText(userProfile.getAbout());

                            // Load profile image
                            if (userProfile.getProfileImageUrl() != null &&
                                    !userProfile.getProfileImageUrl().isEmpty()) {
                                Glide.with(requireContext())
                                        .load(userProfile.getProfileImageUrl())
                                        .placeholder(R.drawable.ic_user)
                                        .into(imgProfile);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void enableEditMode() {
        isEditMode = true;
        // Hide display TextViews
        tvName.setVisibility(View.GONE);
        tvNeighbourhood.setVisibility(View.GONE);
        tvAbout.setVisibility(View.GONE);

        // Show EditTexts
        etName.setVisibility(View.VISIBLE);
        etNeighbourhood.setVisibility(View.VISIBLE);
        etAbout.setVisibility(View.VISIBLE);
        btnChangePhoto.setVisibility(View.VISIBLE);

        // Show Save and Cancel buttons, hide Edit button
        btnEdit.setVisibility(View.GONE);
        btnSave.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);
    }

    private void disableEditMode() {
        isEditMode = false;
        // Show display TextViews
        tvName.setVisibility(View.VISIBLE);
        tvNeighbourhood.setVisibility(View.VISIBLE);
        tvAbout.setVisibility(View.VISIBLE);

        // Hide EditTexts
        etName.setVisibility(View.GONE);
        etNeighbourhood.setVisibility(View.GONE);
        etAbout.setVisibility(View.GONE);
        btnChangePhoto.setVisibility(View.GONE);

        // Show Edit button, hide Save and Cancel
        btnEdit.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
    }

    private void changeProfilePicture() {
        imagePicker.launch("image/*");
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String neighbourhood = etNeighbourhood.getText().toString().trim();
        String about = etAbout.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(neighbourhood) ||
                TextUtils.isEmpty(about)) {
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (about.split("\\s+").length > 150) {
            Toast.makeText(getContext(), "About section exceeds 150 words", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update TextViews immediately
        tvName.setText(name);
        tvNeighbourhood.setText(neighbourhood);
        tvAbout.setText(about);

        if (imageUri != null) {
            uploadImageAndSaveProfile(name, neighbourhood, about);
        } else {
            saveProfileToFirestore(name, neighbourhood, about, null);
        }
    }

    private void uploadImageAndSaveProfile(String name, String neighbourhood, String about) {
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("profile_images/" + currentUser.getUid() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(uri ->
                                saveProfileToFirestore(name, neighbourhood, about, uri.toString())
                        ))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                    saveProfileToFirestore(name, neighbourhood, about, null);
                });
    }

    private void saveProfileToFirestore(String name, String neighbourhood,
                                        String about, String imageUrl) {
        UserProfile userProfile = new UserProfile(
                currentUser.getUid(),
                name,
                currentUser.getEmail(),
                neighbourhood,
                about,
                imageUrl
        );

        db.collection("users")
                .document(currentUser.getUid())
                .set(userProfile)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Profile updated successfully",
                            Toast.LENGTH_SHORT).show();
                    disableEditMode();

                    // Update image if new one was uploaded
                    if (imageUrl != null) {
                        Glide.with(requireContext())
                                .load(imageUrl)
                                .into(imgProfile);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update profile",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}