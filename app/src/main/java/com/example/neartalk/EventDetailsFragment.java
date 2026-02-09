package com.example.neartalk;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
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
        btnSetReminder.setOnClickListener(v -> setReminder());

        return view;
    }

    private void loadEventDetails() {
        if (eventId == null) return;

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference docRef = db.collection("events").document(eventId);

        docRef.get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;

            // Deserialize Firestore document into Event object
            Event event = doc.toObject(Event.class);
            if (event == null) return;

            tvTitle.setText(event.getTitle());
            tvCategory.setText(event.getCategory());
            tvDescription.setText(event.getDescription());
            tvDateTime.setText(event.getDate() + " at " + event.getTime());
            tvLocation.setText(event.getLocation());
            tvAttendees.setText(event.getAttendees() + " attending");

            // Fetch organizer name
            String organizerId = event.getUserId();
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

            // Disable join button if already joined
            List<String> attendeesList = (List<String>) doc.get("attendeesList");
            if (attendeesList == null) attendeesList = new ArrayList<>();

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
            Event event = transaction.get(docRef).toObject(Event.class);
            if (event == null) return "FAILED";

            List<String> attendeesList = (List<String>) transaction.get(docRef).get("attendeesList");
            if (attendeesList == null) attendeesList = new ArrayList<>();

            if (attendeesList.contains(currentUserId)) return "ALREADY_JOINED";

            attendeesList.add(currentUserId);
            transaction.update(docRef, "attendeesList", attendeesList);
            transaction.update(docRef, "attendees", attendeesList.size());

            return "JOINED";
        }).addOnSuccessListener(result -> {
            if ("ALREADY_JOINED".equals(result)) {
                Toast.makeText(getContext(), "You already joined this event!", Toast.LENGTH_SHORT).show();
            } else if ("JOINED".equals(result)) {
                Toast.makeText(getContext(), "You joined the event!", Toast.LENGTH_SHORT).show();
                loadEventDetails();
            }
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Failed to join event.", Toast.LENGTH_SHORT).show()
        );
    }
// foreground is here
    private void setReminder() {
        if (eventId == null) return;

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    Event event = doc.toObject(Event.class);
                    if (event == null) return;

                    String date = event.getDate();
                    String time = event.getTime(); 
                    String title = event.getTitle();
                    if (title == null || title.isEmpty()) title = "Event Reminder";

                    if (date == null || time == null) {
                        Toast.makeText(getContext(), "Event date/time not set", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {

                        String dateTimeStr = date + " " + time;
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.getDefault());
                        java.util.Date eventDate = sdf.parse(dateTimeStr);

                        if (eventDate == null) {
                            Toast.makeText(getContext(), "Invalid event date/time", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(eventDate);

                        long triggerTime = calendar.getTimeInMillis();

                        if (triggerTime < System.currentTimeMillis()) {

                            triggerTime = System.currentTimeMillis() + 1000;
                            Toast.makeText(getContext(), "Event already started, reminder will trigger now", Toast.LENGTH_SHORT).show();
                        }

                        scheduleReminder(triggerTime, title);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to parse event date/time", Toast.LENGTH_SHORT).show();
                    }

                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to fetch event time", Toast.LENGTH_SHORT).show()
                );
    }


    private void scheduleReminder(long triggerAtMillis, String eventTitle) {
        Intent intent = new Intent(requireContext(), MyService.class);

        int requestCode = eventId.hashCode();

        intent.putExtra("eventTitle", eventTitle);
        intent.putExtra("requestCode", requestCode);

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pendingIntent = PendingIntent.getForegroundService(
                    requireContext(),
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        } else {
            pendingIntent = PendingIntent.getService(
                    requireContext(),
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        }

        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
            );
            Toast.makeText(requireContext(), "Reminder set successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Failed to access AlarmManager", Toast.LENGTH_SHORT).show();
        }
    }



}
