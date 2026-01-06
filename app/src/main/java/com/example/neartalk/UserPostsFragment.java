package com.example.neartalk;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserPostsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProfilePostAdapter adapter;
    private final List<Post> postList = new ArrayList<>();

    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_user_posts, container, false);

        recyclerView = view.findViewById(R.id.recyclerPosts); // ✅ FIXED ID
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ProfilePostAdapter(postList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        loadUserPosts();

        adapter.setOnLongClickListener(this::showDeleteDialog);

        return view;
    }

    private void loadUserPosts() {
        if (currentUserId == null) return;

        db.collection("posts")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(query -> {
                    postList.clear();

                    for (QueryDocumentSnapshot doc : query) {
                        Post post = doc.toObject(Post.class);
                        post.setPostId(doc.getId()); // ✅ REQUIRED
                        postList.add(post);
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void showDeleteDialog(Post post) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (d, w) -> deletePost(post))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePost(Post post) {
        db.collection("posts")
                .document(post.getPostId())
                .delete()
                .addOnSuccessListener(unused -> {
                    postList.remove(post);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Post deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show()
                );
    }
}
