package com.example.neartalk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int LEFT = 0;
    private static final int RIGHT = 1;

    private List<GroupMessage> messages;
    private String currentUid;
    private FirebaseFirestore db;

    private final Map<String, String> nameCache = new HashMap<>();

    public GroupChatAdapter(List<GroupMessage> messages) {
        this.messages = messages;
        currentUid = FirebaseAuth.getInstance().getUid();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSenderId().equals(currentUid) ? RIGHT : LEFT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == RIGHT) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_group_msg_right, parent, false);
            return new RightVH(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_group_msg_left, parent, false);
            return new LeftVH(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int pos) {
        GroupMessage msg = messages.get(pos);

        if (holder instanceof RightVH) {
            ((RightVH) holder).msg.setText(msg.getText());
            ((RightVH) holder).sender.setText("You");
        } else {
            ((LeftVH) holder).msg.setText(msg.getText());

            String senderId = msg.getSenderId();

            if (nameCache.containsKey(senderId)) {
                ((LeftVH) holder).sender.setText(nameCache.get(senderId));
            } else {

                db.collection("users").document(senderId).get()
                        .addOnSuccessListener(doc -> {
                            String name = doc.exists() ? doc.getString("userName") : "User";
                            nameCache.put(senderId, name);
                            ((LeftVH) holder).sender.setText(name);
                        });
            }
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class RightVH extends RecyclerView.ViewHolder {
        TextView sender, msg;
        RightVH(View v) {
            super(v);
            sender = v.findViewById(R.id.tvSender);
            msg = v.findViewById(R.id.tvMessage);
        }
    }

    static class LeftVH extends RecyclerView.ViewHolder {
        TextView sender, msg;
        LeftVH(View v) {
            super(v);
            sender = v.findViewById(R.id.tvSender);
            msg = v.findViewById(R.id.tvMessage);
        }
    }
}
