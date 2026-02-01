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
        if (otherUserId == null) finish();

        chatId = generateChatId(currentUser.getUid(), otherUserId);

        adapter = new MessageAdapter(this, messageList, currentUser.getUid());
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(adapter);

        loadOtherUserInfo();
        loadMessages();

        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(msg)) sendMessage(msg);
            etMessage.setText("");
        });
    }

    private void loadOtherUserInfo() {
        db.collection("users").document(otherUserId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        tvUserName.setText(doc.getString("userName"));
                        String url = doc.getString("profileImageUrl");
                        if (url != null && !url.isEmpty())
                            Glide.with(this).load(url).circleCrop().into(imgProfile);
                    }
                });
    }

    private String generateChatId(String u1, String u2) {
        return u1.compareTo(u2) < 0 ? u1 + "_" + u2 : u2 + "_" + u1;
    }

    private void loadMessages() {
        db.collection("user_chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener((snap, e) -> {
                    if (snap == null) return;
                    messageList.clear();
                    for (DocumentSnapshot d : snap.getDocuments())
                        messageList.add(d.toObject(Message.class));
                    adapter.notifyDataSetChanged();
                    if (!messageList.isEmpty())
                        recyclerMessages.scrollToPosition(messageList.size() - 1);
                });
    }

    private void sendMessage(String text) {
        DocumentReference chatRef = db.collection("user_chats").document(chatId);

        chatRef.set(new HashMap<String, Object>() {{
            put("users", Arrays.asList(currentUser.getUid(), otherUserId));
            put("lastMessage", text);
            put("lastTimestamp", System.currentTimeMillis());
        }}, SetOptions.merge());

        chatRef.collection("messages").add(new HashMap<String, Object>() {{
            put("senderId", currentUser.getUid());
            put("receiverId", otherUserId);
            put("text", text);
            put("timestamp", System.currentTimeMillis());
        }});
    }
}
