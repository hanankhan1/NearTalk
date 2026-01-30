package com.example.neartalk;

import android.content.Context;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int LEFT = 0, RIGHT = 1;

    private Context context;
    private List<Message> messages;
    private String currentUid;

    public MessageAdapter(Context context, List<Message> messages, String uid) {
        this.context = context;
        this.messages = messages;
        this.currentUid = uid;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSenderId().equals(currentUid) ? RIGHT : LEFT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(viewType == RIGHT ? R.layout.item_message_right : R.layout.item_message_left, parent, false);
        return viewType == RIGHT ? new RightVH(view) : new LeftVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message m = messages.get(position);
        if (holder instanceof RightVH) ((RightVH) holder).txt.setText(m.getText());
        else ((LeftVH) holder).txt.setText(m.getText());
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class RightVH extends RecyclerView.ViewHolder {
        TextView txt;
        RightVH(View v) { super(v); txt = v.findViewById(R.id.txtMessage); }
    }

    static class LeftVH extends RecyclerView.ViewHolder {
        TextView txt;
        LeftVH(View v) { super(v); txt = v.findViewById(R.id.txtMessage); }
    }
}
