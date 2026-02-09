package com.example.neartalk;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GroupsFragment extends Fragment {

    private FloatingActionButton fabAddGroup;
    private RecyclerView rvGroups;
    private MyGroupsAdapter adapter;

    private final List<AreaGroup> groupList = new ArrayList<>();
    private final List<AreaGroup> filteredList = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextInputEditText etSearch;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        fabAddGroup = view.findViewById(R.id.fabAddGroup);
        rvGroups = view.findViewById(R.id.rvGroups);
        etSearch = view.findViewById(R.id.etSearch);

        rvGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MyGroupsAdapter(
                requireContext(),
                filteredList,
                this::openGroupChat
        );
        rvGroups.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        listenForMyGroups();

        fabAddGroup.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new AddGroupFragment())
                        .addToBackStack(null)
                        .commit()
        );

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterGroups(s.toString());
            }
        });

        return view;
    }

    private void listenForMyGroups() {
        String uid = auth.getUid();
        if (uid == null) return;

        db.collection("areaGroups")
                .whereArrayContains("members", uid)
                .orderBy("lastMessageTime")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    groupList.clear();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        AreaGroup g = doc.toObject(AreaGroup.class);
                        g.setId(doc.getId());
                        groupList.add(g);
                    }

                    filteredList.clear();
                    filteredList.addAll(groupList);
                    adapter.notifyDataSetChanged();

                });
    }

    private void filterGroups(String query) {
        query = query.toLowerCase();
        filteredList.clear();

        for (AreaGroup g : groupList) {
            if (g.getAreaName().toLowerCase().contains(query)) {
                filteredList.add(g);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void openGroupChat(AreaGroup group) {
        GroupChatFragment fragment = new GroupChatFragment();
        Bundle b = new Bundle();
        b.putString("groupId", group.getId());
        b.putString("groupName", group.getAreaName());
        fragment.setArguments(b);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}

