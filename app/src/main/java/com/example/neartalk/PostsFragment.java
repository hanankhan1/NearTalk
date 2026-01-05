package com.example.neartalk;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PostsFragment extends Fragment {

    private RecyclerView postRecyclerView;
    private FloatingActionButton fabAddPost;
    private TextInputEditText etSearch;

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
        etSearch = view.findViewById(R.id.etSearch);

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

        loadPosts();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                filterPosts(s.toString().trim());
            }
        });

        return view;
    }

    private void loadPosts() {
        firestore.collection("posts")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();

                    // For each post, fetch the user's profile image
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
                })
                .addOnFailureListener(e -> {
                    // Handle error
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
}