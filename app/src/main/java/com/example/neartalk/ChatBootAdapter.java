package com.example.neartalk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatBootAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final int USER = 1;
    private static final int BOT = 2;
    private final List<ChatBootMessage> list;
    public ChatBootAdapter(List<ChatBootMessage> list) {
        this.list = list;
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).isUser ? USER : BOT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == USER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user, parent, false);
            return new Holder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bot, parent, false);
            return new Holder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).text.setText(list.get(position).message);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView text;
        Holder(View itemView) {
            super(itemView);
            text = (TextView) itemView;
        }
    }
}
