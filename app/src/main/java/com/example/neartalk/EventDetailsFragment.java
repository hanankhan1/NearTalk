package com.example.neartalk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EventDetailsFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";

    private String eventId;
    private FirebaseFirestore db;

    private TextView tvTitle, tvCategory, tvDescription, tvDateTime,
            tvLocation, tvOrganizer, tvAttendees;
    private Button btnJoinEvent, btnSetReminder;

    public static EventDetailsFragment newInstance(String eventId) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        // Views
        tvTitle = view.findViewById(R.id.tvEventTitle);
        tvCategory = view.findViewById(R.id.tvEventCategory);
        tvDescription = view.findViewById(R.id.tvEventDescription);
        tvDateTime = view.findViewById(R.id.tvEventDateTime);
        tvLocation = view.findViewById(R.id.tvEventLocation);
        tvOrganizer = view.findViewById(R.id.tvEventOrganizer);
        tvAttendees = view.findViewById(R.id.tvEventAttendees);

        btnJoinEvent = view.findViewById(R.id.btnJoinEvent);
        btnSetReminder = view.findViewById(R.id.btnSetReminder);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            loadEventDetails();
        }

        btnJoinEvent.setOnClickListener(v -> joinEvent());
        btnSetReminder.setOnClickListener(v ->
                Toast.makeText(getContext(), "Reminder Set!", Toast.LENGTH_SHORT).show()
        );

        return view;
    }

    private void loadEventDetails() {
        if (eventId == null) return;

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference docRef = db.collection("events").document(eventId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) return;

            String title = documentSnapshot.getString("title");
            String category = documentSnapshot.getString("category");
            String description = documentSnapshot.getString("description");
            String date = documentSnapshot.getString("date");
            String time = documentSnapshot.getString("time");
            String location = documentSnapshot.getString("location");
            String organizerId = documentSnapshot.getString("userId"); // fetch userId
            Long attendees = documentSnapshot.getLong("attendees");
            List<String> attendeesList = (List<String>) documentSnapshot.get("attendeesList");
            if (attendeesList == null) attendeesList = new ArrayList<>();

            tvTitle.setText(title);
            tvCategory.setText(category);
            tvDescription.setText(description);
            tvDateTime.setText(date + " at " + time);
            tvLocation.setText(location);
            tvAttendees.setText(attendees != null ? attendees + " attending" : "0 attending");

            // Fetch organizer name from Firestore
            if (organizerId != null) {
                db.collection("users").document(organizerId)
                        .get()
                        .addOnSuccessListener(userDoc -> {
                            String userName = "User";
                            if (userDoc.exists()) {
                                String name = userDoc.getString("userName");
                                if (name != null && !name.isEmpty()) userName = name;
                            }
                            tvOrganizer.setText("Organized by " + userName);
                        });
            } else {
                tvOrganizer.setText("Organized by User");
            }

            // Disable join if user already joined
            if (attendeesList.contains(currentUserId)) {
                btnJoinEvent.setEnabled(false);
                btnJoinEvent.setText("Joined");
            } else {
                btnJoinEvent.setEnabled(true);
                btnJoinEvent.setText("Join Event");
            }
        });
    }

    private void joinEvent() {
        if (eventId == null) return;

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference docRef = db.collection("events").document(eventId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(docRef);

            List<String> attendeesList = (List<String>) snapshot.get("attendeesList");
            if (attendeesList == null) attendeesList = new ArrayList<>();

            if (attendeesList.contains(currentUserId)) {
                return "ALREADY_JOINED";
            }

            attendeesList.add(currentUserId);
            transaction.update(docRef, "attendeesList", attendeesList);
            transaction.update(docRef, "attendees", attendeesList.size());

            return "JOINED";
        }).addOnSuccessListener(result -> {
            if ("ALREADY_JOINED".equals(result)) {
                Toast.makeText(getContext(), "You already joined this event!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "You joined the event!", Toast.LENGTH_SHORT).show();
                loadEventDetails();
            }
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Failed to join event.", Toast.LENGTH_SHORT).show()
        );
    }

}