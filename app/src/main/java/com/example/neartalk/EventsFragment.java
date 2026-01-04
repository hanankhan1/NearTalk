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
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class EventsFragment extends Fragment {

    private FloatingActionButton fabAddEvent;
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;
    private List<Event> filteredList;
    private FirebaseFirestore db;
    private TextInputEditText etSearch;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_events, container, false);

        fabAddEvent = view.findViewById(R.id.fabAddEvent);
        recyclerView = view.findViewById(R.id.eventRecyclerView);
        etSearch = view.findViewById(R.id.etSearch);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new EventAdapter(filteredList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadEvents();

        fabAddEvent.setOnClickListener(v -> openAddEventFragment());

        // Real-time search by category
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEventsByCategory(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        adapter.setOnItemClickListener(event -> requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, EventDetailsFragment.newInstance(event.getId()))
                .addToBackStack(null)
                .commit()
        );

        return view;
    }

    private void openAddEventFragment() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new AddEventFrgment())
                .addToBackStack(null)
                .commit();
    }

    private void loadEvents() {
        db.collection("events")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    eventList.clear();

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setId(doc.getId());
                            Long attendees = doc.getLong("attendees");
                            event.setAttendees(attendees != null ? attendees : 0);

                            // Fetch organizer name
                            String userId = event.getUserId();
                            if (userId != null && !userId.isEmpty()) {
                                db.collection("users").document(userId)
                                        .get()
                                        .addOnSuccessListener(userDoc -> {
                                            if (userDoc.exists()) {
                                                String userName = userDoc.getString("userName");
                                                event.setUserName(userName != null ? userName : "User");
                                                adapter.notifyDataSetChanged();
                                            }
                                        });
                            }

                            eventList.add(event);
                        }
                    }

                    // Initially show all events
                    filteredList.clear();
                    filteredList.addAll(eventList);
                    adapter.notifyDataSetChanged();
                });
    }

    private void filterEventsByCategory(String category) {
        filteredList.clear();
        if (category.isEmpty()) {
            filteredList.addAll(eventList);
        } else {
            for (Event event : eventList) {
                if (event.getCategory() != null &&
                        event.getCategory().toLowerCase().contains(category.toLowerCase())) {
                    filteredList.add(event);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
