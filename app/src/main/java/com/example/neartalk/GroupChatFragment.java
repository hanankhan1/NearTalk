package com.example.neartalk;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupChatFragment extends Fragment {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;

    private GroupChatAdapter adapter;
    private final List<GroupMessage> messageList = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String groupId;
    private String groupName;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_group_chat, container, false);

        rvMessages = view.findViewById(R.id.rvMessages);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            groupId = getArguments().getString("groupId");
            groupName = getArguments().getString("groupName");
        }

        requireActivity().setTitle(groupName);

        adapter = new GroupChatAdapter(messageList);
        rvMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMessages.setAdapter(adapter);

        listenForMessages();

        btnSend.setOnClickListener(v -> sendMessage());

        return view;
    }


    private void listenForMessages() {
        db.collection("areaGroups")
                .document(groupId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    messageList.clear();
                    snapshots.forEach(doc ->
                            messageList.add(doc.toObject(GroupMessage.class))
                    );

                    adapter.notifyDataSetChanged();
                    rvMessages.scrollToPosition(messageList.size() - 1);
                });
    }


    private void sendMessage() {
        String msg = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(msg)) return;

        String uid = auth.getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    String userName = doc.exists() ? doc.getString("userName") : "User";

                    GroupMessage message = new GroupMessage(
                            uid,
                            userName,
                            msg,
                            System.currentTimeMillis()
                    );

                    db.collection("areaGroups")
                            .document(groupId)
                            .collection("messages")
                            .add(message);

                    Map<String, Object> update = new HashMap<>();
                    update.put("lastMessage", msg);
                    update.put("lastMessageTime", System.currentTimeMillis());
                    update.put("lastMessageSenderId", uid);

                    db.collection("areaGroups")
                            .document(groupId)
                            .update(update);

                    etMessage.setText("");
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) bottomNav.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
    }

}
