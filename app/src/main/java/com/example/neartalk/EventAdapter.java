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

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> eventList;
    private OnEventActionListener listener;

    public interface OnEventActionListener {
        void onEdit(Event event);
        void onDelete(Event event, int position);
    }

    public void setOnEventActionListener(OnEventActionListener listener) {
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
        holder.tvStatus.setText("Going");
        holder.tvAttendees.setText(event.getAttendees() + " attending");

        String organizer = event.getUserName() != null && !event.getUserName().isEmpty()
                ? event.getUserName()
                : "User";
        holder.tvOrganizer.setText("Organized by " + organizer);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener {

        TextView tvTitle, tvStatus, tvCategory, tvDescription,
                tvDate, tvLocation, tvAttendees, tvOrganizer;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvStatus = itemView.findViewById(R.id.tvEventStatus);
            tvCategory = itemView.findViewById(R.id.tvEventCategory);
            tvDescription = itemView.findViewById(R.id.tvEventDescription);
            tvDate = itemView.findViewById(R.id.tvEventDate);
            tvLocation = itemView.findViewById(R.id.tvEventLocation);
            tvAttendees = itemView.findViewById(R.id.tvEventAttendees);
            tvOrganizer = itemView.findViewById(R.id.tvEventOrganizer);

            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {

            menu.setHeaderTitle("Event Options");

            MenuItem edit = menu.add(0, 0, getAdapterPosition(), "Edit");
            MenuItem delete = menu.add(0, 1, getAdapterPosition(), "Delete");

            edit.setOnMenuItemClickListener(menuClickListener);
            delete.setOnMenuItemClickListener(menuClickListener);
        }

        private final MenuItem.OnMenuItemClickListener menuClickListener = item -> {
            int position = item.getOrder();
            Event event = eventList.get(position);

            if (listener == null) return false;

            if (item.getItemId() == 0) {
                listener.onEdit(event);
            } else {
                listener.onDelete(event, position);
            }
            return true;
        };
    }
}
