package com.cplps.android;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_problems) {
                // Navigate to Problems screen
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Navigate to Profile screen
                return true;
            } else if (itemId == R.id.nav_bookmarks) {
                // Navigate to Bookmarks screen
                return true;
            } else if (itemId == R.id.nav_learning) {
                // Navigate to Learning screen
                return true;
            }

            return false;
        });
    }
}
