package com.example.neartalk;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProfilePagerAdapter extends FragmentStateAdapter {

    public ProfilePagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new UserPostsFragment();
        } else {
            return new UserEventsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Two tabs: Posts and Events
    }
}
