package com.example.neartalk;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.*;

public class ChatsFragment extends Fragment {

    private RecyclerView recyclerChats;
    private ChatListAdapter adapter;
    private List<ChatItem> chatList = new ArrayList<>();
    private List<ChatItem> filteredList = new ArrayList<>();
    private TextInputEditText etSearch;
    private TextView tvEmptyChats;

    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerChats = view.findViewById(R.id.recyclerChats);
        etSearch = view.findViewById(R.id.etSearch);
        tvEmptyChats = view.findViewById(R.id.tvEmptyChats);

        recyclerChats.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatListAdapter(getContext(), filteredList);
        recyclerChats.setAdapter(adapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        loadChats();
        setupSearch();

        return view;
    }

    private void loadChats() {
        if (currentUser == null) return;

        db.collection("user_chats")
                .whereArrayContains("users", currentUser.getUid())
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;

                    chatList.clear();
                    Map<String, ChatItem> tempMap = new HashMap<>();
                    Set<String> otherUserIds = new HashSet<>();

                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        List<String> users = (List<String>) doc.get("users");
                        if (users == null || users.size() < 2) continue;

                        String otherId = users.get(0).equals(currentUser.getUid())
                                ? users.get(1)
                                : users.get(0);

                        ChatItem chat = new ChatItem();
                        chat.setChatId(doc.getId());
                        chat.setOtherUserId(otherId);
                        chat.setLastMessage(doc.getString("lastMessage"));
                        chat.setLastTimestamp(
                                doc.getLong("lastTimestamp") != null
                                        ? doc.getLong("lastTimestamp")
                                        : 0
                        );

                        tempMap.put(otherId, chat);
                        otherUserIds.add(otherId);
                    }

                    if (otherUserIds.isEmpty()) {
                        updateFilteredList();
                        return;
                    }

                    db.collection("users")
                            .whereIn(FieldPath.documentId(), new ArrayList<>(otherUserIds))
                            .get()
                            .addOnSuccessListener(usersSnap -> {
                                chatList.clear();
                                for (DocumentSnapshot userDoc : usersSnap.getDocuments()) {
                                    ChatItem chat = tempMap.get(userDoc.getId());
                                    if (chat != null) {
                                        chat.setOtherUsername(userDoc.getString("userName"));
                                        chat.setProfileImageUrl(userDoc.getString("profileImageUrl"));
                                        chatList.add(chat);
                                    }
                                }
                                updateFilteredList();
                            });
                });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase();
                filteredList.clear();
                for (ChatItem chat : chatList) {
                    if (chat.getOtherUsername() != null &&
                            chat.getOtherUsername().toLowerCase().contains(query)) {
                        filteredList.add(chat);
                    }
                }
                adapter.notifyDataSetChanged();
                tvEmptyChats.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private void updateFilteredList() {
        // Show all chats by default or filter based on search query
        String query = etSearch.getText() != null ? etSearch.getText().toString().toLowerCase() : "";
        filteredList.clear();
        for (ChatItem chat : chatList) {
            if (chat.getOtherUsername() == null || chat.getOtherUsername().toLowerCase().contains(query))
                filteredList.add(chat);
        }
        adapter.notifyDataSetChanged();
        tvEmptyChats.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
