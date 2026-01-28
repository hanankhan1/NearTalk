package com.example.neartalk;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> eventList;
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }

    public EventAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_list, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.tvTitle.setText(event.getTitle());
        holder.tvCategory.setText(event.getCategory());
        holder.tvDescription.setText(event.getDescription());
        holder.tvDate.setText(event.getDate() + " at " + event.getTime());
        holder.tvLocation.setText(event.getLocation());
        holder.tvAttendees.setText(event.getAttendees() + " attending");

        String organizer = event.getUserName() != null && !event.getUserName().isEmpty()
                ? event.getUserName()
                : "User";
        holder.tvOrganizer.setText("Organized by " + organizer);

        // Click listener to open detail screen
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvDescription, tvDate,
                tvLocation, tvAttendees, tvOrganizer;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvCategory = itemView.findViewById(R.id.tvEventCategory);
            tvDescription = itemView.findViewById(R.id.tvEventDescription);
            tvDate = itemView.findViewById(R.id.tvEventDate);
            tvLocation = itemView.findViewById(R.id.tvEventLocation);
            tvAttendees = itemView.findViewById(R.id.tvEventAttendees);
            tvOrganizer = itemView.findViewById(R.id.tvEventOrganizer);
        }
    }
}
