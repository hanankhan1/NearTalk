package com.example.neartalk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> eventList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
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
        holder.tvStatus.setText("Going"); // can be dynamic later
        holder.tvAttendees.setText(event.getAttendees() + " attending");

        // Show userName if available, fallback to "User"
        String organizerName = event.getUserName() != null && !event.getUserName().isEmpty()
                ? event.getUserName()
                : "User";
        holder.tvOrganizer.setText("Organized by " + organizerName);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvStatus, tvCategory, tvDescription,
                tvDate, tvLocation, tvAttendees, tvOrganizer;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvStatus = itemView.findViewById(R.id.tvEventStatus);
            tvCategory = itemView.findViewById(R.id.tvEventCategory);
            tvDescription = itemView.findViewById(R.id.tvEventDescription);
            tvDate = itemView.findViewById(R.id.tvEventDate);
            tvLocation = itemView.findViewById(R.id.tvEventLocation);
            tvAttendees = itemView.findViewById(R.id.tvEventAttendees);
            tvOrganizer = itemView.findViewById(R.id.tvEventOrganizer);

            // Item click
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(eventList.get(pos));
                }
            });
        }
    }
}
