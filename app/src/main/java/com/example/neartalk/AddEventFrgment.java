package com.example.neartalk;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class AddEventFrgment extends Fragment {

    private TextInputEditText etTitle, etDescription, etDate, etTime, etLocation;
    private Button btnCreateEvent;
    private String selectedCategory = "Social";
    private FirebaseFirestore db;
    private Event editingEvent;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.add_event, container, false);
        db = FirebaseFirestore.getInstance();

        etTitle = view.findViewById(R.id.etEventTitle);
        etDescription = view.findViewById(R.id.etDescription);
        etDate = view.findViewById(R.id.etDate);
        etTime = view.findViewById(R.id.etTime);
        etLocation = view.findViewById(R.id.etLocation);
        btnCreateEvent = view.findViewById(R.id.btnCreateEvent);

        MaterialButton btnSocial = view.findViewById(R.id.btnSocial);
        MaterialButton btnCommunity = view.findViewById(R.id.btnCommunity);
        MaterialButton btnSports = view.findViewById(R.id.btnSports);
        MaterialButton btnWorkshops = view.findViewById(R.id.btnWorkshops);

        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());

        btnSocial.setOnClickListener(v -> selectedCategory = "Social");
        btnCommunity.setOnClickListener(v -> selectedCategory = "Community");
        btnSports.setOnClickListener(v -> selectedCategory = "Sports");
        btnWorkshops.setOnClickListener(v -> selectedCategory = "Workshops");

        btnCreateEvent.setOnClickListener(v -> createEvent());
        if (getArguments() != null) {
            editingEvent = (Event) getArguments().getSerializable("event");

            if (editingEvent != null) {
                etTitle.setText(editingEvent.getTitle());
                etDescription.setText(editingEvent.getDescription());
                etDate.setText(editingEvent.getDate());
                etTime.setText(editingEvent.getTime());
                etLocation.setText(editingEvent.getLocation());
                btnCreateEvent.setText("Update Event");
            }
        }

        btnCreateEvent.setOnClickListener(v -> {
            if (editingEvent == null) {
                createEvent();
            } else {
                updateEvent();
            }
        });


        return view;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(requireContext(), (view, year1, month1, dayOfMonth) ->
                etDate.setText((month1 + 1) + "/" + dayOfMonth + "/" + year1),
                year, month, day).show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(requireContext(), (view, hourOfDay, minute1) -> {
            String amPm = hourOfDay >= 12 ? "PM" : "AM";
            int hourFormatted = hourOfDay % 12;
            if (hourFormatted == 0) hourFormatted = 12;
            etTime.setText(String.format("%02d:%02d %s", hourFormatted, minute1, amPm));
        }, hour, minute, false).show();
    }

    private void createEvent() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(date) || TextUtils.isEmpty(time)) {
            Toast.makeText(getContext(), "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch current user's name from Firestore
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userName = "";
                    if (documentSnapshot.exists()) {
                        userName = documentSnapshot.getString("userName"); // field from UserProfile
                    }

                    Event event = new Event(
                            null, // id will be set by Firestore
                            currentUserId,
                            userName,
                            selectedCategory,
                            title,
                            description,
                            date,
                            time,
                            location,
                            System.currentTimeMillis(),
                            0 // attendees start at 0
                    );

                    db.collection("events")
                            .add(event)
                            .addOnSuccessListener(doc -> {
                                // After adding, update the id field in the event document
                                doc.update("id", doc.getId());
                                Toast.makeText(getContext(), "Event created successfully", Toast.LENGTH_SHORT).show();
                                requireActivity().getSupportFragmentManager().popBackStack();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Failed to create event: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );

                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to get user info: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
    private void updateEvent() {
        db.collection("events").document(editingEvent.getId())
                .update(
                        "title", etTitle.getText().toString(),
                        "description", etDescription.getText().toString(),
                        "date", etDate.getText().toString(),
                        "time", etTime.getText().toString(),
                        "location", etLocation.getText().toString()
                )
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Event updated", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                });
    }

}