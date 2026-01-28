package com.example.neartalk;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddPostFragment extends Fragment {

    private static final int MAX_IMAGES = 10;

    private TextInputEditText etTitle, etDescription, etPrice;
    private TextView tvWordCount, tvImageCount;
    private MaterialButton btnPost, btnSelling, btnLost, btnFound;
    private MaterialCardView cvPrice;
    private RecyclerView rvImages;

    private final List<Uri> imageUris = new ArrayList<>();
    private final List<String> uploadedUrls = new ArrayList<>();
    private String selectedType = "Post";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private ActivityResultLauncher<String[]> imagePicker;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_post, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etTitle = view.findViewById(R.id.etTitle);
        etDescription = view.findViewById(R.id.etDescription);
        etPrice = view.findViewById(R.id.etPrice);
        tvWordCount = view.findViewById(R.id.tvWordCount);
        tvImageCount = view.findViewById(R.id.tvImageCount);
        btnPost = view.findViewById(R.id.btnPost);
        btnSelling = view.findViewById(R.id.btnSelling);
        btnLost = view.findViewById(R.id.btnLost);
        btnFound = view.findViewById(R.id.btnFound);
        cvPrice = view.findViewById(R.id.cvPrice);
        rvImages = view.findViewById(R.id.rvSelectedImages);

        rvImages.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvImages.setAdapter(new SelectedImageAdapter(imageUris));

        setupTypeButtons();
        setupWordCounter();
        setupImagePicker(view);

        view.findViewById(R.id.btnSelectImages).setOnClickListener(v -> {
            if (imageUris.size() >= MAX_IMAGES) {
                Toast.makeText(getContext(), "Max 10 images allowed", Toast.LENGTH_SHORT).show();
                return;
            }
            imagePicker.launch(new String[]{"image/*"});
        });

        view.findViewById(R.id.btnSubmit).setOnClickListener(v -> uploadImagesAndSavePost());

        return view;
    }

    private void setupTypeButtons() {
        MaterialButton[] buttons = {btnPost, btnSelling, btnLost, btnFound};
        for (MaterialButton btn : buttons) {
            btn.setOnClickListener(v -> {
                for (MaterialButton b : buttons) {
                    b.setChecked(false);
                    b.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                    b.setTextColor(getResources().getColor(android.R.color.white));
                }
                MaterialButton selectedBtn = (MaterialButton) v;
                selectedBtn.setChecked(true);
                selectedBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                selectedBtn.setTextColor(getResources().getColor(android.R.color.white));
                selectedType = selectedBtn.getText().toString();
                cvPrice.setVisibility(selectedType.equals("Selling") ? View.VISIBLE : View.GONE);
            });
        }
        btnPost.performClick();
    }

    private void setupWordCounter() {
        etDescription.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                int words = s.toString().trim().isEmpty()
                        ? 0 : s.toString().trim().split("\\s+").length;
                if (getContext() != null) {
                    tvWordCount.setText(words + "/150 words");
                }
            }
        });
    }

    private void setupImagePicker(View view) {
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> {
                    for (Uri uri : uris) {
                        if (imageUris.size() < MAX_IMAGES) imageUris.add(uri);
                    }
                    if (getContext() != null) {
                        tvImageCount.setText(imageUris.size() + "/10 images");
                        rvImages.getAdapter().notifyDataSetChanged();
                    }
                });
    }

    private void uploadImagesAndSavePost() {
        if (etTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Title required", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadedUrls.clear();
        if (imageUris.isEmpty()) {
            savePost();
            return;
        }

        String postId = UUID.randomUUID().toString();
        for (Uri uri : imageUris) {
            StorageReference ref = FirebaseStorage.getInstance()
                    .getReference("post_images")
                    .child(postId)
                    .child(UUID.randomUUID().toString() + ".jpg");

            ref.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(url -> {
                        uploadedUrls.add(url.toString());
                        if (uploadedUrls.size() == imageUris.size()) savePost();
                    }))
                    .addOnFailureListener(e -> {
                        if (getContext() != null)
                            Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void savePost() {
        if (getContext() == null || getActivity() == null) return;

        String uid = auth.getUid();
        if (uid == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Creating post...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userName = "";
                    String userProfileImage = "";
                    String neighbourhood = "";

                    if (documentSnapshot.exists()) {
                        UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                        if (userProfile != null) {
                            userName = userProfile.getUserName();
                            userProfileImage = userProfile.getProfileImageUrl();
                            neighbourhood = userProfile.getNeighbourhood(); // ✅ Important
                        }
                    }

                    // Fallbacks
                    if (userName.isEmpty()) {
                        userName = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "Unknown";
                    }
                    if (userProfileImage == null) userProfileImage = "";
                    if (neighbourhood == null) neighbourhood = "";

                    // Create Post object
                    Post post = new Post(
                            null,
                            uid,
                            userName,
                            userProfileImage,
                            selectedType,
                            etTitle.getText().toString().trim(),
                            etDescription.getText().toString().trim(),
                            etPrice.getText().toString().trim(),
                            new ArrayList<>(uploadedUrls),
                            System.currentTimeMillis(),
                            neighbourhood
                    );

                    // Save to Firestore
                    db.collection("posts")
                            .add(post)
                            .addOnSuccessListener(doc -> {
                                if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
                                Toast.makeText(getContext(), "Post Created", Toast.LENGTH_SHORT).show();
                                getActivity().getSupportFragmentManager().popBackStack();
                            })
                            .addOnFailureListener(e -> {
                                if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
                                Toast.makeText(getContext(), "Failed to create post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    // Fallback if user profile can't be fetched
                    if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();

                    // Use safe fallback values
                    String userName = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "Unknown";
                    String userProfileImage = ""; // No profile image
                    String neighbourhood = "";     // No neighbourhood

                    Post post = new Post(
                            null,
                            uid,
                            userName,
                            userProfileImage,
                            selectedType,
                            etTitle.getText().toString().trim(),
                            etDescription.getText().toString().trim(),
                            etPrice.getText().toString().trim(),
                            new ArrayList<>(uploadedUrls),
                            System.currentTimeMillis(),
                            neighbourhood
                    );

                    db.collection("posts")
                            .add(post)
                            .addOnSuccessListener(doc -> {
                                Toast.makeText(getContext(), "Post Created", Toast.LENGTH_SHORT).show();
                                getActivity().getSupportFragmentManager().popBackStack();
                            })
                            .addOnFailureListener(e2 -> {
                                Toast.makeText(getContext(), "Failed to create post: " + e2.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
    }


}