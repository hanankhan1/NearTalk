package com.example.neartalk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserEventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private EventAdapter adapter;
    private List<Event> eventList;

    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user_events, container, false);

        recyclerView = view.findViewById(R.id.recyclerEvents);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        eventList = new ArrayList<>();
        adapter = new EventAdapter(eventList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        adapter.setOnEventActionListener(new EventAdapter.OnEventActionListener() {
            @Override
            public void onEdit(Event event) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("event", event);

                AddEventFrgment fragment = new AddEventFrgment();
                fragment.setArguments(bundle);

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onDelete(Event event, int position) {
                db.collection("events").document(event.getId())
                        .delete()
                        .addOnSuccessListener(unused -> {
                            eventList.remove(position);
                            adapter.notifyItemRemoved(position);
                            Toast.makeText(getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        loadUserEvents();
        return view;
    }

    private void loadUserEvents() {
        if (currentUser == null) return;

        db.collection("events")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(query -> {
                    eventList.clear();

                    if (!query.isEmpty()) {
                        for (var doc : query.getDocuments()) {
                            Event event = doc.toObject(Event.class);
                            event.setId(doc.getId()); // 🔥 CRITICAL
                            eventList.add(event);
                        }
                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }
}
