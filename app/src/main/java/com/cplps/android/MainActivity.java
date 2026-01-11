package com.cplps.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.cplps.android.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        setupClickListeners();
        setupLogout();
    }

    private void setupClickListeners() {
        // 1. Profile
        CardView cardProfile = findViewById(R.id.card_profile);
        cardProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        // 2. Problems
        CardView cardProblems = findViewById(R.id.card_problems);
        cardProblems.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProblemsActivity.class)));

        // 3. Solved Problems
        CardView cardSolved = findViewById(R.id.card_solved);
        cardSolved.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SolvedProblemsActivity.class)));

        // 4. Bookmarks
        CardView cardBookmarks = findViewById(R.id.card_bookmarks);
        cardBookmarks.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, BookmarksActivity.class)));

        // 5. Learning
        CardView cardLearning = findViewById(R.id.card_learning);
        cardLearning.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LearningActivity.class)));

        // 6. Notes
        CardView cardNotes = findViewById(R.id.card_notes);
        cardNotes.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, NotesActivity.class)));
    }

    private void setupLogout() {
        ImageButton btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            redirectToLogin();
        });
    }

    private void redirectToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
