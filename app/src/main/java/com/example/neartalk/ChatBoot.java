package com.example.neartalk;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatBoot extends AppCompatActivity {

    EditText et_prompt;
    ImageButton btnSend;
    RecyclerView chatRecycler;

    ChatBootAdapter adapter;
    List<ChatBootMessage> messages = new ArrayList<>();

    FirebaseFirestore db;
    RequestQueue queue;
    ProgressDialog dialog;

    private static final String API_KEY = "AIzaSyDY7vCYw9LmBKlWtYzBfd_v3em2-dd8D5w";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_boot);

        et_prompt = findViewById(R.id.et_prompt);
        btnSend = findViewById(R.id.btnSend);
        chatRecycler = findViewById(R.id.chatRecycler);

        dialog = new ProgressDialog(this);
        queue = Volley.newRequestQueue(this);
        db = FirebaseFirestore.getInstance();

        adapter = new ChatBootAdapter(messages);
        chatRecycler.setLayoutManager(new LinearLayoutManager(this));
        chatRecycler.setAdapter(adapter);

        loadOldChats();

        btnSend.setOnClickListener(v -> {
            String text = et_prompt.getText().toString().trim();
            if (text.isEmpty()) return;

            ChatBootMessage userMsg = new ChatBootMessage(text, true);
            messages.add(userMsg);
            adapter.notifyItemInserted(messages.size() - 1);
            chatRecycler.scrollToPosition(messages.size() - 1);
            saveMessage(userMsg);

            et_prompt.setText("");
            sendRequestToGemini(text);
        });
    }

    private void loadOldChats() {
        db.collection("chats")
                .orderBy("timestamp")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        messages.clear();
                        messages.addAll(value.toObjects(ChatBootMessage.class));
                        adapter.notifyDataSetChanged();
                        chatRecycler.scrollToPosition(messages.size() - 1);
                    }
                });
    }

    private void saveMessage(ChatBootMessage msg) {
        db.collection("chats").add(msg);
    }

    private void sendRequestToGemini(String userPrompt) {

        dialog.setMessage("Bot is typing...");
        dialog.setCancelable(false);
        dialog.show();

        String url =
                "https://generativelanguage.googleapis.com/v1beta/models/" +
                        "gemini-2.5-flash:generateContent?key=" + API_KEY;

        try {
            JSONObject jsonBody = new JSONObject();
            JSONArray contentsArray = new JSONArray();
            JSONObject contentObject = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject textPart = new JSONObject();

            textPart.put("text", userPrompt);
            partsArray.put(textPart);
            contentObject.put("parts", partsArray);
            contentsArray.put(contentObject);
            jsonBody.put("contents", contentsArray);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        dialog.dismiss();
                        try {
                            String botReply = response.getJSONArray("candidates")
                                    .getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text");

                            ChatBootMessage botMsg = new ChatBootMessage(botReply, false);
                            messages.add(botMsg);
                            adapter.notifyItemInserted(messages.size() - 1);
                            chatRecycler.scrollToPosition(messages.size() - 1);
                            saveMessage(botMsg);

                        } catch (JSONException e) {
                            addErrorMessage(e.getMessage());
                        }
                    },
//                    error -> {
//                        dialog.dismiss();
//                        addErrorMessage("Network Error");
//                    }
                    error -> {
                        dialog.dismiss();

                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                String errorBody = new String(error.networkResponse.data, "UTF-8");
                                addErrorMessage("Gemini Error:\n" + errorBody);
                            } catch (Exception e) {
                                addErrorMessage("Error: " + e.getMessage());
                            }
                        } else {
                            addErrorMessage("No Internet or SSL Error");
                        }
                    }) {

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            queue.add(request);

        } catch (JSONException e) {
            dialog.dismiss();
            addErrorMessage(e.getMessage());
        }
    }

    private void addErrorMessage(String error) {
        ChatBootMessage errorMsg = new ChatBootMessage("Error: " + error, false);
        messages.add(errorMsg);
        adapter.notifyItemInserted(messages.size() - 1);
        chatRecycler.scrollToPosition(messages.size() - 1);
    }
}
