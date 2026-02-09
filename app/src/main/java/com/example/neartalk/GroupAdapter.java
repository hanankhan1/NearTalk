package com.example.neartalk;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupVH> {

    private Context context;
    private List<AreaGroup> groupList;
    private String currentUserId;

    public GroupAdapter(Context context, List<AreaGroup> groupList) {
        this.context = context;
        this.groupList = groupList;
        this.currentUserId = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public GroupVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.addgroupitem, parent, false);
        return new GroupVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupVH holder, int position) {
        AreaGroup group = groupList.get(position);

        holder.tvGroupName.setText(group.getAreaName());
        holder.tvMembers.setText(group.getMemberCount() + " members");

        boolean isMember = group.getMembers() != null
                && group.getMembers().contains(currentUserId);

        if (isMember) {
            holder.btnJoin.setText("Joined");
            holder.btnJoin.setEnabled(true);
        } else {
            holder.btnJoin.setText("Join");
            holder.btnJoin.setEnabled(true);
        }

        holder.btnJoin.setOnClickListener(v -> {
            if (isMember) {
                showLeaveDialog(group, holder);
            } else {
                joinGroup(group, holder);
            }
        });
    }

    private void joinGroup(AreaGroup group, GroupVH holder) {
        if (group.getMembers().contains(currentUserId)) return;

        holder.btnJoin.setEnabled(false);

        group.getMembers().add(currentUserId);
        group.setMemberCount(group.getMemberCount() + 1);
        notifyItemChanged(holder.getAdapterPosition());

        FirebaseFirestore.getInstance()
                .collection("areaGroups")
                .document(group.getId())
                .update(
                        "members", FieldValue.arrayUnion(currentUserId),
                        "memberCount", FieldValue.increment(1)
                )
                .addOnFailureListener(e -> {
                    group.getMembers().remove(currentUserId);
                    group.setMemberCount(group.getMemberCount() - 1);
                    notifyItemChanged(holder.getAdapterPosition());
                    Toast.makeText(context, "Join failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void showLeaveDialog(AreaGroup group, GroupVH holder) {
        new AlertDialog.Builder(context)
                .setTitle("Leave Group")
                .setMessage("Are you sure you want to leave this group?")
                .setPositiveButton("Leave", (d, w) -> leaveGroup(group, holder))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void leaveGroup(AreaGroup group, GroupVH holder) {
        holder.btnJoin.setEnabled(false);


        group.getMembers().remove(currentUserId);
        group.setMemberCount(group.getMemberCount() - 1);
        notifyItemChanged(holder.getAdapterPosition());

        FirebaseFirestore.getInstance()
                .collection("areaGroups")
                .document(group.getId())
                .update(
                        "members", FieldValue.arrayRemove(currentUserId),
                        "memberCount", FieldValue.increment(-1)
                )
                .addOnFailureListener(e -> {
                    group.getMembers().add(currentUserId);
                    group.setMemberCount(group.getMemberCount() + 1);
                    notifyItemChanged(holder.getAdapterPosition());
                    Toast.makeText(context, "Leave failed", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    static class GroupVH extends RecyclerView.ViewHolder {
        TextView tvGroupName, tvMembers;
        Button btnJoin;

        GroupVH(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvMembers = itemView.findViewById(R.id.tvMembers);
            btnJoin = itemView.findViewById(R.id.btnJoin);
        }
    }
}
