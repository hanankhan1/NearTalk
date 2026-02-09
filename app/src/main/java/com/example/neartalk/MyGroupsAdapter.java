package com.example.neartalk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyGroupsAdapter extends RecyclerView.Adapter<MyGroupsAdapter.GroupVH> {

    public interface OnGroupClickListener {
        void onGroupClick(AreaGroup group);
    }

    private Context context;
    private List<AreaGroup> list;
    private OnGroupClickListener listener;

    public MyGroupsAdapter(Context context,
                           List<AreaGroup> list,
                           OnGroupClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_my_group, parent, false);
        return new GroupVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupVH h, int position) {
        AreaGroup g = list.get(position);

        h.tvName.setText(g.getAreaName());
        h.tvLastMessage.setText(
                g.getLastMessage() == null ? "No messages yet" : g.getLastMessage()
        );

        h.tvTime.setText(formatTime(g.getLastMessageTime()));

        h.itemView.setOnClickListener(v -> listener.onGroupClick(g));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class GroupVH extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMessage, tvTime;
        ImageView img;

        GroupVH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvGroupName);
            tvLastMessage = v.findViewById(R.id.tvLastMessage);
            tvTime = v.findViewById(R.id.tvTime);
            img = v.findViewById(R.id.imgGroup);
        }
    }

    private String formatTime(long time) {
        if (time == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(time));
    }
}
