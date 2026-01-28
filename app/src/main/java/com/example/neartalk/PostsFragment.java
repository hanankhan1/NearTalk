package com.example.neartalk;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PostsFragment extends Fragment {

    private RecyclerView postRecyclerView;
    private FloatingActionButton fabAddPost, fabChatbot;
    private TextInputEditText etSearch;
    private FrameLayout loadingOverlay;

    private FirebaseFirestore firestore;
    private PostAdapter adapter;
    private final List<Post> postList = new ArrayList<>();
    private final List<Post> filteredList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_posts, container, false);

        postRecyclerView = view.findViewById(R.id.postRecyclerView);
        fabAddPost = view.findViewById(R.id.fabAddPost);
        fabChatbot = view.findViewById(R.id.fabChatBot);
        etSearch = view.findViewById(R.id.etSearch);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        postRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostAdapter(getContext(), filteredList);
        postRecyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();

        fabAddPost.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new AddPostFragment())
                        .addToBackStack(null)
                        .commit()
        );

        fabChatbot.setOnClickListener(v -> {
            Intent i = new Intent(requireActivity(), ChatBoot.class);
            startActivity(i);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                filterPosts(s.toString().trim());
            }
        });

        loadPosts(); // Load posts with overlay

        return view;
    }

    private void loadPosts() {
        showLoading(true);

        firestore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Post post = doc.toObject(Post.class);

                        // Fetch user profile to get profile image
                        firestore.collection("users")
                                .document(post.getUserId())
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        UserProfile userProfile = userDoc.toObject(UserProfile.class);
                                        if (userProfile != null) {
                                            post.setUserProfileImage(userProfile.getProfileImageUrl());
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                });

                        postList.add(post);
                    }

                    filteredList.clear();
                    filteredList.addAll(postList);
                    adapter.notifyDataSetChanged();

                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Failed to load posts", Toast.LENGTH_SHORT).show();
                });
    }

    private void filterPosts(String category) {
        filteredList.clear();
        if (category.isEmpty()) {
            filteredList.addAll(postList);
        } else {
            for (Post post : postList) {
                if (post.getType().equalsIgnoreCase(category)) {
                    filteredList.add(post);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    // Helper method to show/hide loading overlay
    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
