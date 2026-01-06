package com.example.neartalk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProfilePostAdapter extends RecyclerView.Adapter<ProfilePostAdapter.VH> {

    public interface OnPostLongClickListener {
        void onLongClick(Post post);
    }

    private OnPostLongClickListener longClickListener;
    private final List<Post> postList;

    public ProfilePostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    public void setOnLongClickListener(OnPostLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile_post, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Post post = postList.get(position);

        holder.tvTitle.setText(post.getTitle());
        holder.tvDesc.setText(post.getDescription());
        holder.tvType.setText(post.getType().toUpperCase());

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onLongClick(post);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvType;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvPostTitle);
            tvDesc = itemView.findViewById(R.id.tvPostDesc);
            tvType = itemView.findViewById(R.id.tvPostType);
        }
    }
}
