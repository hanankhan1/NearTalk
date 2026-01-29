package com.example.neartalk;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class UserSearchAdapter
        extends RecyclerView.Adapter<UserSearchAdapter.UserVH> {

    private Context context;
    private List<UserProfile> userList;

    public UserSearchAdapter(Context context, List<UserProfile> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_user_search, parent, false);
        return new UserVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserVH holder, int position) {
        UserProfile user = userList.get(position);

        holder.tvUsername.setText(user.getUserName());
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.ic_user)
                    .circleCrop()
                    .into(holder.imgProfile);
        } else {
            holder.imgProfile.setImageResource(R.drawable.ic_user);
        }


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("otherUserId", user.getUserId());
            intent.putExtra("otherUsername", user.getUserName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserVH extends RecyclerView.ViewHolder {
        TextView tvUsername;
        ImageView imgProfile;

        public UserVH(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            imgProfile = itemView.findViewById(R.id.imgProfile);
        }
    }
}
