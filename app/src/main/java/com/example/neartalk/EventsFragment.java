package com.example.neartalk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class EventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;
    private FirebaseFirestore db;
    private FloatingActionButton fabeventadd;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_events, container, false);
        recyclerView = view.findViewById(R.id.eventRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        eventList = new ArrayList<>();
        adapter = new EventAdapter(eventList);
        recyclerView.setAdapter(adapter);
        fabeventadd = view.findViewById(R.id.fabAddEvent);
        fabeventadd.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new AddEventFrgment())
                        .addToBackStack(null)
                        .commit()
        );

        db = FirebaseFirestore.getInstance();

        // Click listener for opening EventDetailsFragment
        adapter.setOnEventClickListener(event -> {
            EventDetailsFragment fragment = EventDetailsFragment.newInstance(event.getId());
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        loadEvents();

        return view;
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
                            if (userId != null) {
                                db.collection("users")
                                        .document(userId)
                                        .get()
                                        .addOnSuccessListener(userDoc -> {
                                            if (userDoc.exists()) {
                                                String name = userDoc.getString("userName");
                                                event.setUserName(name != null ? name : "User");
                                                adapter.notifyDataSetChanged();
                                            }
                                        });
                            }

                            eventList.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
