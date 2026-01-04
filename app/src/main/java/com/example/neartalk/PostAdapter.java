package com.example.neartalk;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostVH> {

    private final List<Post> postList;
    private final Context context;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.post_list, parent, false);
        return new PostVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostVH holder, int position) {
        Post post = postList.get(position);

        holder.tvName.setText(post.getUserName());
        holder.tvTitle.setText(post.getTitle());
        holder.tvDesc.setText(post.getDescription());
        holder.tvPrice.setText(post.getPrice().isEmpty() ? "" : "$" + post.getPrice());
        holder.tvStatus.setText(post.getType().toUpperCase());
        holder.tvPic.setText(post.getUserName().isEmpty() ? "U" :
                post.getUserName().substring(0,1).toUpperCase());

        // Horizontal images
        holder.imageRecyclerView.setLayoutManager(
                new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.imageRecyclerView.setAdapter(new ImageAdapter(context, post.getImageUrls()));

        holder.btnMessage.setOnClickListener(v -> {
            // TODO: Open chat with seller
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostVH extends RecyclerView.ViewHolder {
        TextView tvName, tvTitle, tvDesc, tvPrice, tvStatus, tvPic;
        RecyclerView imageRecyclerView;
        Button btnMessage;

        public PostVH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPic = itemView.findViewById(R.id.tvPic);
            imageRecyclerView = itemView.findViewById(R.id.imageRecyclerView);
            btnMessage = itemView.findViewById(R.id.btnMessage);
        }
    }

    // ---------------- IMAGE ADAPTER ----------------
    public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageVH> {
        private final List<String> images;
        private final Context context;

        public ImageAdapter(Context context, List<String> images) {
            this.context = context;
            this.images = images;
        }

        @NonNull
        @Override
        public ImageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_post_image, parent, false);
            return new ImageVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageVH holder, int position) {
            Glide.with(context)
                    .load(images.get(position))
                    .centerCrop()
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        class ImageVH extends RecyclerView.ViewHolder {
            ImageView imageView;

            public ImageVH(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.ivPostImage);
            }
        }
    }
}
