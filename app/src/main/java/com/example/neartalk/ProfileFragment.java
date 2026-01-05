package com.example.neartalk;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private ImageView ivProfilePicture;
    private TextView tvUsername, tvEmail, tvLocation, tvBio, tvPostCount, tvEventCount, tvNeighbourhood;
    private MaterialButton btnEditProfile, btnLogout;

    private MaterialButton btnChangePhotoDialog;
    private TextInputEditText etEditName, etEditNeighbourhood, etEditAbout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private Uri imageUri;

    private ActivityResultLauncher<String> imagePicker;
    private androidx.viewpager2.widget.ViewPager2 viewPager;
    private com.google.android.material.tabs.TabLayout tabLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvLocation = view.findViewById(R.id.tvLocation);
        tvBio = view.findViewById(R.id.tvBio);
        tvPostCount = view.findViewById(R.id.tvPostCount);
        tvEventCount = view.findViewById(R.id.tvEventCount);
        tvNeighbourhood = view.findViewById(R.id.tvNeighbourhood);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        uploadProfilePicture();
                    }
                }
        );

        btnEditProfile.setOnClickListener(v -> showEditDialog());
        btnLogout.setOnClickListener(v -> logoutUser());
        ivProfilePicture.setOnClickListener(v -> changeProfilePicture());

        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);

        ProfilePagerAdapter adapter = new ProfilePagerAdapter(this);
        viewPager.setAdapter(adapter);

        new com.google.android.material.tabs.TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? "Posts" : "Events")
        ).attach();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentUser != null) {
            loadUserProfile();
            loadUserStats();
        }
    }

    private void loadUserProfile() {
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = getFieldValue(documentSnapshot, "userName", "username", "name");
                        String email = getFieldValue(documentSnapshot, "email");
                        String about = getFieldValue(documentSnapshot, "about", "bio", "description");
                        String location = getFieldValue(documentSnapshot, "location", "city", "area");
                        String neighbourhood = getFieldValue(documentSnapshot, "neighbourhood", "neighborhood", "locality");
                        String profileImageUrl = getFieldValue(documentSnapshot, "profileImageUrl", "photoUrl", "imageUrl");

                        // Update UI with data
                        tvUsername.setText(!TextUtils.isEmpty(username) ? username : "Username");
                        tvEmail.setText(!TextUtils.isEmpty(email) ? "Email: " + email : "Email: Not specified");

                        // Handle bio/about section
                        if (!TextUtils.isEmpty(about)) {
                            tvBio.setText(about);
                        } else {
                            tvBio.setText("Bio section will appear here...");
                        }

                        // Handle location
                        if (!TextUtils.isEmpty(location)) {
                            tvLocation.setText("Location: " + location);
                        } else {
                            tvLocation.setText("Location: Not specified");
                        }

                        // Handle neighbourhood (this is the one in the stats area)
                        if (!TextUtils.isEmpty(neighbourhood)) {
                            tvNeighbourhood.setText(neighbourhood);
                        } else {
                            tvNeighbourhood.setText("--");
                        }

                        // Load profile image
                        if (!TextUtils.isEmpty(profileImageUrl)) {
                            Glide.with(requireContext())
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_user)
                                    .circleCrop()
                                    .into(ivProfilePicture);
                        } else {
                            ivProfilePicture.setImageResource(R.drawable.ic_user);
                        }
                    } else {
                        // Create default profile if document doesn't exist
                        createDefaultProfile();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    // Helper method to get field value with multiple possible field names
    private String getFieldValue(DocumentSnapshot documentSnapshot, String... fieldNames) {
        for (String fieldName : fieldNames) {
            String value = documentSnapshot.getString(fieldName);
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        }
        return null;
    }

    private void loadUserStats() {
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        // Load post count
        db.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tvPostCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                })
                .addOnFailureListener(e -> {
                    tvPostCount.setText("0");
                });

        // Load event count
        db.collection("events")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tvEventCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                })
                .addOnFailureListener(e -> {
                    tvEventCount.setText("0");
                });
    }

    private void showEditDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Profile");
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);

        btnChangePhotoDialog = dialogView.findViewById(R.id.btnChangePhoto);
        etEditName = dialogView.findViewById(R.id.etEditName);
        etEditNeighbourhood = dialogView.findViewById(R.id.etEditNeighbourhood);
        etEditAbout = dialogView.findViewById(R.id.etEditAbout);

        String currentName = tvUsername.getText().toString();
        if (currentName.equals("Username")) {
            currentName = "";
        }

        String currentNeighbourhood = tvNeighbourhood.getText().toString();
        if (currentNeighbourhood.equals("--")) {
            currentNeighbourhood = "";
        }

        String currentBio = tvBio.getText().toString();
        if (currentBio.equals("Bio section will appear here...")) {
            currentBio = "";
        }

        etEditName.setText(currentName);
        etEditNeighbourhood.setText(currentNeighbourhood);
        etEditAbout.setText(currentBio);

        btnChangePhotoDialog.setOnClickListener(v -> {
            changeProfilePicture();
        });

        builder.setPositiveButton("Save", (dialog, which) -> {
            saveProfileChanges();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveProfileChanges() {
        String newName = etEditName.getText().toString().trim();
        String newNeighbourhood = etEditNeighbourhood.getText().toString().trim();
        String newAbout = etEditAbout.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(newName)) {
            Toast.makeText(getContext(), "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(newNeighbourhood)) {
            Toast.makeText(getContext(), "Neighbourhood is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check word count for about/bio (max 150 words)
        if (!TextUtils.isEmpty(newAbout)) {
            String[] words = newAbout.split("\\s+");
            if (words.length > 150) {
                Toast.makeText(getContext(), "About section exceeds 150 words", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Get current location from TextView (we're not editing location in dialog)
        String currentLocation = tvLocation.getText().toString();
        if (currentLocation.startsWith("Location: ")) {
            currentLocation = currentLocation.substring(10);
            if (currentLocation.equals("Not specified")) {
                currentLocation = "";
            }
        }

        // Update UI immediately
        tvUsername.setText(newName);
        tvNeighbourhood.setText(newNeighbourhood);
        tvBio.setText(TextUtils.isEmpty(newAbout) ? "Bio section will appear here..." : newAbout);

        // Update in Firestore
        updateProfileInFirestore(newName, newAbout, currentLocation, newNeighbourhood);
    }

    private void updateProfileInFirestore(String name, String about, String location, String neighbourhood) {
        if (currentUser == null) return;

        // Create update map
        Map<String, Object> updates = new HashMap<>();
        updates.put("userName", name);
        updates.put("about", about);
        updates.put("location", location);
        updates.put("neighbourhood", neighbourhood);
        updates.put("email", currentUser.getEmail()); // Keep email updated

        db.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Profile updated successfully",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update profile",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void changeProfilePicture() {
        imagePicker.launch("image/*");
    }

    private void uploadProfilePicture() {
        if (imageUri == null || currentUser == null) return;

        // Show loading
        Toast.makeText(getContext(), "Uploading image...", Toast.LENGTH_SHORT).show();

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("profile_images/" + currentUser.getUid() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Update Firestore with new image URL
                            db.collection("users").document(currentUser.getUid())
                                    .update("profileImageUrl", uri.toString())
                                    .addOnSuccessListener(unused -> {
                                        // Update ImageView with new image
                                        Glide.with(requireContext())
                                                .load(uri.toString())
                                                .placeholder(R.drawable.ic_user)
                                                .circleCrop()
                                                .into(ivProfilePicture);

                                        Toast.makeText(getContext(),
                                                "Profile picture updated",
                                                Toast.LENGTH_SHORT).show();
                                    });
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to upload image: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void createDefaultProfile() {
        if (currentUser == null) return;

        // Create a default user profile
        Map<String, Object> defaultProfile = new HashMap<>();
        defaultProfile.put("userName", currentUser.getDisplayName() != null ?
                currentUser.getDisplayName() : "New User");
        defaultProfile.put("email", currentUser.getEmail());
        defaultProfile.put("about", "");
        defaultProfile.put("location", "");
        defaultProfile.put("neighbourhood", "");
        defaultProfile.put("profileImageUrl", "");
        defaultProfile.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(currentUser.getUid())
                .set(defaultProfile)
                .addOnSuccessListener(unused -> {
                    // Reload profile after creation
                    loadUserProfile();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to create profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}