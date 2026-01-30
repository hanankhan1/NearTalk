package com.example.neartalk;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class UserSearch extends Fragment {

    private TextInputEditText etSearchUser;
    private RecyclerView recyclerUsers;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private List<UserProfile> userList = new ArrayList<>();
    private UserSearchAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user_search, container, false);

        etSearchUser = view.findViewById(R.id.etSearchUser);
        recyclerUsers = view.findViewById(R.id.recyclerUsers);

        recyclerUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserSearchAdapter(requireActivity(), userList);
        recyclerUsers.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().trim().toLowerCase());
            }
        });

        return view;
    }

    private void searchUsers(String keyword) {

        if (keyword.isEmpty()) {
            userList.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        db.collection("users")
                .orderBy("userName")
                .startAt(keyword)
                .endAt(keyword + "\uf8ff")
                .limit(20)
                .get()
                .addOnSuccessListener(snapshot -> {

                    userList.clear();

                    for (DocumentSnapshot doc : snapshot) {

                        UserProfile user = doc.toObject(UserProfile.class);
                        if (user == null) continue;

                        user.setUserId(doc.getId());

                        if (!user.getUserId().equals(currentUser.getUid())) {
                            userList.add(user);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
