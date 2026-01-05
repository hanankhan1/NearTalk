package com.example.neartalk;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashBoard extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Default fragment
        loadFragment(new PostsFragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new PostsFragment();
            } else if (item.getItemId() == R.id.nav_chats) {
                selectedFragment = new ChatsFragment();
            } else if (item.getItemId() == R.id.nav_events) {
                selectedFragment = new EventsFragment();
            } else if (item.getItemId() == R.id.nav_groups) {
                selectedFragment = new GroupsFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }

            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}