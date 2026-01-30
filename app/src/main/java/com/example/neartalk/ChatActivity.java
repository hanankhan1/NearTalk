package com.example.neartalk;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.*;

public class ChatActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private TextView tvUserName;
    private RecyclerView recyclerMessages;
    private EditText etMessage;
    private ImageButton btnSend;

    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private String otherUserId;
    private String otherUserName;
    private String chatId;

    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        imgProfile = findViewById(R.id.imgProfile);
        tvUserName = findViewById(R.id.tvUserName);
        recyclerMessages = findViewById(R.id.recyclerMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        otherUserId = getIntent().getStringExtra("otherUserId");
        otherUserName = getIntent().getStringExtra("otherUsername");

        tvUserName.setText(otherUserName);

        // Load profile image
        db.collection("users").document(otherUserId).get()
                .addOnSuccessListener(doc -> {
                    String img = doc.getString("profileImageUrl");
                    if (img != null) Glide.with(this).load(img).circleCrop().into(imgProfile);
                });

        chatId = generateChatId(currentUser.getUid(), otherUserId);

        adapter = new MessageAdapter(this, messageList, currentUser.getUid());
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(adapter);

        loadMessages();

        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(msg)) {
                sendMessage(msg);
                etMessage.setText("");
            }
        });
    }

    private String generateChatId(String u1, String u2) {
        return u1.compareTo(u2) < 0 ? u1 + "_" + u2 : u2 + "_" + u1;
    }

    private void loadMessages() {
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;
                    messageList.clear();
                    for (DocumentSnapshot d : snap) {
                        Message m = d.toObject(Message.class);
                        if (m != null) messageList.add(m);
                    }
                    adapter.notifyDataSetChanged();
                    recyclerMessages.scrollToPosition(messageList.size() - 1);
                });
    }

    private void sendMessage(String text) {
        DocumentReference chatRef = db.collection("chats").document(chatId);

        // Update last message & users in chat
        chatRef.set(new HashMap<String, Object>() {{
            put("users", Arrays.asList(currentUser.getUid(), otherUserId));
            put("lastMessage", text);
            put("lastTimestamp", System.currentTimeMillis());
        }}, SetOptions.merge());

        // Add actual message
        Map<String, Object> msg = new HashMap<>();
        msg.put("senderId", currentUser.getUid());
        msg.put("receiverId", otherUserId);
        msg.put("text", text);
        msg.put("timestamp", System.currentTimeMillis());

        chatRef.collection("messages").add(msg);
    }
}
