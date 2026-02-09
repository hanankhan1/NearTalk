package com.example.neartalk;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserEventAdapter extends RecyclerView.Adapter<UserEventAdapter.UserEventViewHolder> {

    private final List<Event> eventList;
    private OnUserEventActionListener listener;

    public interface OnUserEventActionListener {
        void onEdit(Event event);
        void onDelete(Event event, int position);
    }

    public void setOnUserEventActionListener(OnUserEventActionListener listener) {
        this.listener = listener;
    }

    public UserEventAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public UserEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_list, parent, false);
        return new UserEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserEventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.tvTitle.setText(event.getTitle());
        holder.tvCategory.setText(event.getCategory());
        holder.tvDescription.setText(event.getDescription());
        holder.tvDate.setText(event.getDate() + " at " + event.getTime());
        holder.tvLocation.setText(event.getLocation());
        holder.tvAttendees.setText(event.getAttendees() + " attending");

        String organizer = event.getUserName() != null ? event.getUserName() : "User";
        holder.tvOrganizer.setText("Organized by " + organizer);

        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    class UserEventViewHolder extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener {

        TextView tvTitle, tvCategory, tvDescription, tvDate,
                tvLocation, tvAttendees, tvOrganizer;
        Event currentEvent;

        UserEventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvCategory = itemView.findViewById(R.id.tvEventCategory);
            tvDescription = itemView.findViewById(R.id.tvEventDescription);
            tvDate = itemView.findViewById(R.id.tvEventDate);
            tvLocation = itemView.findViewById(R.id.tvEventLocation);
            tvAttendees = itemView.findViewById(R.id.tvEventAttendees);
            tvOrganizer = itemView.findViewById(R.id.tvEventOrganizer);

            itemView.setOnCreateContextMenuListener(this);
        }

        void bind(Event event) {
            this.currentEvent = event;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {

            MenuInflater inflater = new MenuInflater(v.getContext());
            inflater.inflate(R.menu.event_context_menu, menu);

            menu.findItem(R.id.menu_edit).setOnMenuItemClickListener(item -> {
                if (listener != null) listener.onEdit(currentEvent);
                return true;
            });

            menu.findItem(R.id.menu_delete).setOnMenuItemClickListener(item -> {
                if (listener != null) listener.onDelete(currentEvent, getAdapterPosition());
                return true;
            });
        }
    }
}
