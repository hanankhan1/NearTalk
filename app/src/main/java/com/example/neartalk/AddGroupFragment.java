package com.example.neartalk;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddGroupFragment extends Fragment {

    private RecyclerView rvGroups;
    private GroupAdapter adapter;
    private List<AreaGroup> allGroups = new ArrayList<>();
    private List<AreaGroup> filteredGroups = new ArrayList<>();
    private TextInputEditText etSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.addgroupfragment, container, false);

        rvGroups = view.findViewById(R.id.rvGroups);
        etSearch = view.findViewById(R.id.etSearch);

        rvGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new GroupAdapter(requireContext(), filteredGroups);
        rvGroups.setAdapter(adapter);

        listenForGroups();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
        });

        return view;
    }

    private void listenForGroups() {
        FirebaseFirestore.getInstance()
                .collection("areaGroups")
                .addSnapshotListener((snapshots, e) -> {
                    if (snapshots == null) return;

                    allGroups.clear();
                    for (var doc : snapshots) {
                        AreaGroup group = doc.toObject(AreaGroup.class);
                        group.setId(doc.getId());
                        allGroups.add(group);
                    }
                    filter(etSearch.getText() == null ? "" : etSearch.getText().toString());
                });
    }

    private void filter(String query) {
        query = query.toLowerCase(Locale.ROOT);
        filteredGroups.clear();

        for (AreaGroup g : allGroups) {
            if (g.getAreaName().toLowerCase(Locale.ROOT).contains(query)) {
                filteredGroups.add(g);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
